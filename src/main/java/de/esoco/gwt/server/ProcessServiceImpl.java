//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.gwt.server;

import de.esoco.data.SessionData;
import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;
import de.esoco.data.process.ProcessDescription;
import de.esoco.data.process.ProcessState;
import de.esoco.data.process.ProcessState.ProcessExecutionMode;
import de.esoco.data.process.ProcessState.ProcessStateFlag;

import de.esoco.entity.ConcurrentEntityModificationException;
import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;
import de.esoco.entity.EntityRelationTypes;

import de.esoco.gwt.shared.AuthenticationException;
import de.esoco.gwt.shared.Command;
import de.esoco.gwt.shared.ProcessService;
import de.esoco.gwt.shared.ServiceException;

import de.esoco.lib.collection.CollectionUtil;
import de.esoco.lib.logging.Log;
import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.ViewDisplayType;

import de.esoco.process.FragmentInteraction;
import de.esoco.process.InvalidParametersException;
import de.esoco.process.Process;
import de.esoco.process.ProcessDefinition;
import de.esoco.process.ProcessException;
import de.esoco.process.ProcessExecutor;
import de.esoco.process.ProcessFragment;
import de.esoco.process.ProcessManager;
import de.esoco.process.ProcessRelationTypes;
import de.esoco.process.ProcessStep;
import de.esoco.process.ViewFragment;
import de.esoco.process.step.EditInteractionParameters;

import de.esoco.storage.StorageException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.obrel.core.ObjectRelations;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;

import static de.esoco.data.DataRelationTypes.SESSION_MANAGER;
import static de.esoco.data.DataRelationTypes.STORAGE_ADAPTER_REGISTRY;

import static de.esoco.process.ProcessRelationTypes.AUTO_CONTINUE;
import static de.esoco.process.ProcessRelationTypes.AUTO_UPDATE;
import static de.esoco.process.ProcessRelationTypes.CLIENT_HEIGHT;
import static de.esoco.process.ProcessRelationTypes.CLIENT_INFO;
import static de.esoco.process.ProcessRelationTypes.CLIENT_LOCALE;
import static de.esoco.process.ProcessRelationTypes.CLIENT_WIDTH;
import static de.esoco.process.ProcessRelationTypes.DETAIL_STEP;
import static de.esoco.process.ProcessRelationTypes.EXTERNAL_SERVICE_ACCESS;
import static de.esoco.process.ProcessRelationTypes.FINAL_STEP;
import static de.esoco.process.ProcessRelationTypes.IMMEDIATE_INTERACTION;
import static de.esoco.process.ProcessRelationTypes.INTERACTION_PARAMS;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_ACTION_EVENT;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_EVENT_TYPE;
import static de.esoco.process.ProcessRelationTypes.INTERACTIVE_INPUT_PARAM;
import static de.esoco.process.ProcessRelationTypes.OPTIONAL_PROCESS_INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.PROCESS_ID;
import static de.esoco.process.ProcessRelationTypes.PROCESS_INFO;
import static de.esoco.process.ProcessRelationTypes.PROCESS_LIST;
import static de.esoco.process.ProcessRelationTypes.PROCESS_LOCALE;
import static de.esoco.process.ProcessRelationTypes.PROCESS_STEP_STYLE;
import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;
import static de.esoco.process.ProcessRelationTypes.RELOAD_CURRENT_STEP;
import static de.esoco.process.ProcessRelationTypes.REQUIRED_PROCESS_INPUT_PARAMS;
import static de.esoco.process.ProcessRelationTypes.SPAWN_PROCESSES;
import static de.esoco.process.ProcessRelationTypes.VIEW_PARAMS;

import static org.obrel.core.RelationTypes.newMapType;
import static org.obrel.type.StandardTypes.DESCRIPTION;
import static org.obrel.type.StandardTypes.NAME;


/********************************************************************
 * The base class of {@link ProcessService} for service implementations that
 * provide process-related functionality.
 *
 * @author eso
 */
public abstract class ProcessServiceImpl<E extends Entity>
	extends StorageServiceImpl<E> implements ProcessService, ProcessExecutor
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * The process map will be stored in the {@link SessionData} object for a
	 * user's session.
	 */
	private static final RelationType<Map<Integer, Process>> USER_PROCESS_MAP =
		newMapType(false);

	private static List<ProcessDefinition> aProcessDefinitions =
		new ArrayList<ProcessDefinition>();

	private static Locale rDefaultLocale = Locale.GERMANY;

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a process description to be used by client code for a certain
	 * process definition and registers the definition internally so that it can
	 * be associated with the index stored in the description.
	 *
	 * @param  rDefClass The process definition class
	 *
	 * @return A new process description of the process definition
	 */
	public static ProcessDescription createProcessDescription(
		Class<? extends ProcessDefinition> rDefClass)
	{
		return createProcessDescriptions(rDefClass, null);
	}

	/***************************************
	 * Sets the default locale to be used if the client locale cannot be
	 * determined.
	 *
	 * @param rLocale The new default locale
	 */
	public static void setDefaultLocale(Locale rLocale)
	{
		rDefaultLocale = rLocale;
	}

	/***************************************
	 * Creates one or more process descriptions for a certain process
	 * definition. If the second argument is not NULL and the process definition
	 * relation {@link ProcessRelationTypes#OPTIONAL_PROCESS_INPUT_PARAMS}
	 * contains {@link EntityRelationTypes#ENTITY_ID} two descriptions will be
	 * added to the list, one for the creation of a new entity and one for the
	 * editing of an existing entity.
	 *
	 * @param  rDefClass        rDefinition The process definition
	 * @param  rDescriptionList A list to add the description(s) to or NULL to
	 *                          only return the description
	 *
	 * @return A new process description of the process definition
	 */
	protected static ProcessDescription createProcessDescriptions(
		Class<? extends ProcessDefinition> rDefClass,
		List<ProcessDescription>		   rDescriptionList)
	{
		ProcessDefinition rDefinition =
			ProcessManager.getProcessDefinition(rDefClass);

		int nIndex = aProcessDefinitions.indexOf(rDefinition);

		if (nIndex < 0)
		{
			nIndex = aProcessDefinitions.size();
			aProcessDefinitions.add(rDefinition);
		}

		boolean bInputRequired =
			rDefinition.hasRelation(REQUIRED_PROCESS_INPUT_PARAMS);

		if (!bInputRequired &&
			rDefinition.get(OPTIONAL_PROCESS_INPUT_PARAMS).size() > 0 &&
			rDescriptionList != null)
		{
			// create a special edit process description with the same ID (!)
			// that will be invoked if a selection is available
			ProcessDescription aDescription =
				new ProcessDescription(rDefinition.get(NAME) + "Edit",
									   rDefinition.get(DESCRIPTION),
									   nIndex,
									   true);

			rDescriptionList.add(aDescription);
		}

		ProcessDescription aDescription =
			new ProcessDescription(rDefinition.get(NAME),
								   rDefinition.get(DESCRIPTION),
								   nIndex,
								   bInputRequired);

		if (rDescriptionList != null)
		{
			rDescriptionList.add(aDescription);
		}

		return aDescription;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public ProcessState executeProcess(
		ProcessDescription rDescription,
		Relatable		   rInitParams) throws AuthenticationException,
											   ServiceException
	{
		boolean bCheckAuthentication =
			!hasProcessAuthentication() ||
			rDescription.hasFlag(PROCESS_AUTHENTICATED);

		SessionData rSessionData = getSessionData(bCheckAuthentication);

		ProcessExecutionMode  eExecutionMode = ProcessExecutionMode.EXECUTE;
		Process				  rProcess		 = null;
		Integer				  rId			 = null;
		ProcessState		  rProcessState  = null;
		Map<Integer, Process> rProcessMap    = null;

		List<Process> rProcessList = getSessionContext().get(PROCESS_LIST);

		try
		{
			// this can only happen in the case of process-based authentication
			if (rSessionData == null)
			{
				rSessionData =
					createProcessAuthenticationSession(rDescription,
													   rInitParams);
			}

			rProcessMap = rSessionData.get(USER_PROCESS_MAP);
			rProcess    = getProcess(rDescription, rSessionData, rInitParams);

			rId = rProcess.getParameter(PROCESS_ID);

			if (rDescription instanceof ProcessState)
			{
				rProcessState = (ProcessState) rDescription;

				eExecutionMode = rProcessState.getExecutionMode();
				checkOpenUiInspector(rProcessState, rProcess);
			}

			String sClientInfo   = rDescription.getClientInfo();
			String sClientLocale = rDescription.getClientLocale();

			if (sClientInfo != null)
			{
				rProcess.set(CLIENT_INFO, sClientInfo);
			}

			if (sClientLocale != null)
			{
				try
				{
					rProcess.set(CLIENT_LOCALE,
								 Locale.forLanguageTag(sClientLocale));
				}
				catch (Exception e)
				{
					Log.warn("Unknown client locale: " + sClientLocale, e);
					rProcess.set(CLIENT_LOCALE, rDefaultLocale);
				}
			}

			rProcess.set(CLIENT_WIDTH, rDescription.getClientWidth());
			rProcess.set(CLIENT_HEIGHT, rDescription.getClientHeight());

			executeProcess(rProcess, eExecutionMode);

			rProcessState =
				createProcessState(rDescription,
								   rProcess,
								   eExecutionMode ==
								   ProcessExecutionMode.RELOAD);

			if (rProcess.isFinished())
			{
				rProcessList.remove(rProcess);
				rProcessMap.remove(rId);
			}
		}
		catch (Throwable e)
		{
			if (rProcess != null)
			{
				try
				{
					rProcessState =
						createProcessState(rDescription, rProcess, false);
				}
				catch (Exception eSecondary)
				{
					// should normally not occur. If it does then log and fall
					// through to standard exception handling
					Log.error("Could not create exception process state",
							  eSecondary);
				}
			}

			ServiceException eService = wrapException(e, rProcessState);

			// keep the process on recoverable error for re-execution when the
			// client has tried to resolve the error condition
			if (!eService.isRecoverable() && rProcess != null)
			{
				rProcessList.remove(rProcess);
				rProcessMap.remove(rId);
			}

			throw eService;
		}

		return rProcessState;
	}

	/***************************************
	 * Handles the {@link ProcessService#EXECUTE_PROCESS} command.
	 *
	 * @param  rDescription The description of the process to execute
	 *
	 * @return A process state object if the process stopped for an interaction
	 *         or NULL if the process has already terminated
	 *
	 * @throws ServiceException If the process execution fails
	 */
	public ProcessState handleExecuteProcess(ProcessDescription rDescription)
		throws ServiceException
	{
		return executeProcess(rDescription, null);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ServletException
	{
		super.init();

		// property name not known to GWT serialization if not accessed once
		SHOW_UI_INSPECTOR.getName();
	}

	/***************************************
	 * Cancels all processes that are active in the given session.
	 *
	 * @param rSessionData The session data
	 */
	protected void cancelActiveProcesses(SessionData rSessionData)
	{
		List<Process> rProcessList = getSessionContext().get(PROCESS_LIST);

		Map<Integer, Process> rProcessMap = rSessionData.get(USER_PROCESS_MAP);

		for (Process rProcess : rProcessMap.values())
		{
			rProcess.cancel();
			rProcessList.remove(rProcess);
		}

		rProcessMap.clear();
	}

	/***************************************
	 * Overridden to allow the execution of a self-authenticating application
	 * process.
	 *
	 * @see ProcessServiceImpl#checkCommandExecution(Command, DataElement)
	 */
	@Override
	protected <T extends DataElement<?>> void checkCommandExecution(
		Command<T, ?> rCommand,
		T			  rData) throws ServiceException
	{
		if (!hasProcessAuthentication() ||
			!rCommand.equals(EXECUTE_PROCESS) ||
			!rData.getName().startsWith(APPLICATION_PROCESS))
		{
			super.checkCommandExecution(rCommand, rData);
		}
	}

	/***************************************
	 * Checks whether the given process description is for an application
	 * process and a corresponding process already exists in the given process
	 * collection.
	 *
	 * @param  rDescription The application process description
	 * @param  rProcesses   The processes to search
	 *
	 * @return The application process to re-use or NULL for none
	 */
	protected Process checkReuseExistingAppProcess(
		ProcessDescription  rDescription,
		Collection<Process> rProcesses)
	{
		String  sProcessName = rDescription.getName();
		Process rProcess     = null;

		if (sProcessName.startsWith(APPLICATION_PROCESS))
		{
			for (Process rExistingProcess : rProcesses)
			{
				if (sProcessName.endsWith(rExistingProcess.getName()))
				{
					rProcess = rExistingProcess;

					break;
				}
			}
		}

		return rProcess;
	}

	/***************************************
	 * Creates a dummy session in the case of process-based authentication that
	 * is used until the user is authenticated by the process. If a session
	 * timeout occurred during the execution of the authentication process step
	 * the application process will be restarted so that it is available in the
	 * session again.
	 *
	 * @param  rDescription The process description of the application process
	 * @param  rInitParams  Optional process initialization parameters or NULL
	 *                      for none
	 *
	 * @return A new unauthenticated session data object
	 *
	 * @throws ServiceException
	 * @throws ProcessException
	 * @throws StorageException
	 */
	@SuppressWarnings("boxing")
	protected SessionData createProcessAuthenticationSession(
		ProcessDescription rDescription,
		Relatable		   rInitParams) throws StorageException,
											   ProcessException,
											   ServiceException
	{
		SessionData rSessionData = createSessionData();

		if (rDescription instanceof ProcessState)
		{
			// if a session timeout occurs before authentication (i.e.
			// in the login step of the application process) re-start
			// the app process with a new process description
			ProcessDescription aRestartDescription =
				new ProcessDescription(rDescription);

			Process aRestartProcess =
				getProcess(aRestartDescription, rSessionData, rInitParams);

			// execute once for initialization, then continue with the original
			// process state (with updated process ID) to perform the last
			// interaction of the login step which had failed because of the
			// session timeout
			aRestartProcess.execute();
			((ProcessState) rDescription).setProcessId(aRestartProcess
													   .getParameter(PROCESS_ID));
		}

		return rSessionData;
	}

	/***************************************
	 * Overridden to cancel any running processes of the current user.
	 *
	 * @see AuthenticatedServiceImpl#endSession(SessionData)
	 */
	@Override
	protected void endSession(SessionData rSessionData)
	{
		cancelActiveProcesses(rSessionData);

		super.endSession(rSessionData);
	}

	/***************************************
	 * Performs the actual invocation of a process execution method.
	 *
	 * @param  rProcess The process to execute
	 * @param  eMode    The execution mode
	 *
	 * @throws ProcessException If the process execution fails
	 */
	protected void executeProcess(Process rProcess, ProcessExecutionMode eMode)
		throws ProcessException
	{
		rProcess.execute(eMode);
	}

	/***************************************
	 * Can be overridden by subclasses to indicate that their application
	 * process handles the authentication instead of the application framework.
	 * The default implementation always return FALSE.
	 *
	 * @return TRUE for process based authentication
	 */
	protected boolean hasProcessAuthentication()
	{
		return false;
	}

	/***************************************
	 * Initializes a new process and associates it with a reference entity if it
	 * is not NULL. Subclasses that override this method should typically invoke
	 * the superclass method first. The initialization parameters will be copied
	 * to the process, overriding any existing parameters.
	 *
	 * @param  rProcess    The process to initialize
	 * @param  rInitParams Optional process initialization parameters or NULL
	 *                     for none
	 *
	 * @throws ProcessException If the process initialization fails
	 * @throws ServiceException If the user is not authenticated or the
	 *                          preparing the process context fails
	 */
	protected void initProcess(Process rProcess, Relatable rInitParams)
		throws ProcessException, ServiceException
	{
		if (rInitParams != null)
		{
			ObjectRelations.copyRelations(rInitParams, rProcess, true);
		}
	}

	/***************************************
	 * Overridden to cancel any processes that remained active in the given
	 * session when the user closed the browser window.
	 *
	 * @see AuthenticatedServiceImpl#resetSessionData(SessionData)
	 */
	@Override
	protected void resetSessionData(SessionData rSessionData)
	{
		cancelActiveProcesses(rSessionData);
	}

	/***************************************
	 * Can be invoked by subclasses to set the main application process. This
	 * method should be invoked before any other process definitions are created
	 * through {@link #createProcessDescriptions(Class, List)}.
	 *
	 * @param  rAppProcess The class of the main application process
	 *
	 * @return The description of the application process
	 */
	protected ProcessDescription setApplicationProcess(
		Class<? extends ProcessDefinition> rAppProcess)
	{
		return createProcessDescriptions(rAppProcess, null);
	}

	/***************************************
	 * Wraps exceptions that may occur during the execution of one of the
	 * service methods into a {@link ServiceException} if necessary. In case of
	 * an {@link InvalidParametersException} the service exception will contain
	 * information about the invalid parameters.
	 *
	 * @param  e             The exception to handle
	 * @param  rProcessState The current process state
	 *
	 * @return The resulting {@link ServiceException}
	 */
	protected ServiceException wrapException(
		Throwable    e,
		ProcessState rProcessState)
	{
		ServiceException eResult;

		if (e instanceof ServiceException)
		{
			eResult = (ServiceException) e;
		}
		else
		{
			String sMessage = e.getMessage();

			if (e instanceof InvalidParametersException)
			{
				Map<RelationType<?>, String> rParamMessageMap =
					((InvalidParametersException) e).getInvalidParams();

				Map<String, String> aInvalidParams =
					new HashMap<String, String>(rParamMessageMap.size());

				for (Entry<RelationType<?>, String> rEntry :
					 rParamMessageMap.entrySet())
				{
					aInvalidParams.put(rEntry.getKey().getName(),
									   rEntry.getValue());
				}

				eResult =
					new ServiceException(sMessage,
										 aInvalidParams,
										 rProcessState);
			}
			else if (e.getCause() instanceof
					 ConcurrentEntityModificationException)
			{
				String sEntityId =
					((ConcurrentEntityModificationException) e.getCause())
					.getEntityId();

				Map<String, String> aLockedEntity =
					new HashMap<String, String>(1);

				aLockedEntity.put(ERROR_LOCKED_ENTITY_ID, sEntityId);

				eResult =
					new ServiceException(ERROR_ENTITY_LOCKED,
										 aLockedEntity,
										 rProcessState);
			}
			else
			{
				eResult = new ServiceException(sMessage, e);
			}
		}

		return eResult;
	}

	/***************************************
	 * Checks whether the client has requested to open the UI inspector and
	 * opens the corresponding view if necessary.
	 *
	 * @param  rProcessState The current process state
	 * @param  rProcess      The
	 *
	 * @throws Exception
	 */
	private void checkOpenUiInspector(
		ProcessState rProcessState,
		Process		 rProcess) throws Exception
	{
		if (rProcessState.hasFlag(SHOW_UI_INSPECTOR))
		{
			ProcessStep rInteractionStep = rProcess.getInteractionStep();

			if (rInteractionStep instanceof FragmentInteraction)
			{
				List<RelationType<?>> rParams =
					rInteractionStep.get(INTERACTION_PARAMS);

				ViewFragment aViewFragment =
					new ViewFragment("UI_INSPECTOR",
									 new EditInteractionParameters(rParams),
									 ViewDisplayType.VIEW);

				aViewFragment.show(((FragmentInteraction) rInteractionStep)
								   .getRootFragment());
			}
		}
	}

	/***************************************
	 * Creates the data elements for certain relations of a relatable object.
	 * For each relation a single data element will be created by invoking the
	 * method {@link #createDataElement(Relatable, RelationType, Set)}. If that
	 * method returns NULL no data element will be added to the result for the
	 * respective relation.
	 *
	 * @param  rInteractionStep The process fragment to query the relations from
	 * @param  bMarkAsChanged   Whether all data elements should be marked as
	 *                          modified
	 *
	 * @return A new list containing the resulting data elements
	 *
	 * @throws StorageException If the initialization of a storage-based data
	 *                          element fails
	 */
	private List<DataElement<?>> createInteractionDataElements(
		ProcessFragment rInteractionStep,
		boolean			bMarkAsChanged) throws StorageException
	{
		DataElementFactory rFactory = getDataElementFactory();

		List<RelationType<?>> rInteractionParams =
			rInteractionStep.get(INTERACTION_PARAMS);

		List<DataElement<?>> aDataElements =
			new ArrayList<DataElement<?>>(rInteractionParams.size());

		for (RelationType<?> rParam : rInteractionParams)
		{
			DataElement<?> aElement =
				rFactory.getDataElement(rInteractionStep, rParam);

			if (aElement != null)
			{
				aDataElements.add(aElement);

				if (bMarkAsChanged)
				{
					aElement.markAsChanged();
				}
			}
		}

		return aDataElements;
	}

	/***************************************
	 * Creates a new process instance and sets the standard parameters on it.
	 *
	 * @param  rDefinition  The process definition
	 * @param  rSessionData The data of the current session
	 *
	 * @return The new process instance
	 *
	 * @throws ProcessException If creating the instance fails
	 */
	private Process createProcess(
		ProcessDefinition rDefinition,
		SessionData		  rSessionData) throws ProcessException
	{
		Process rProcess = ProcessManager.getProcess(rDefinition);
		Entity  rUser    = rSessionData.get(SessionData.SESSION_USER);

		rProcess.setParameter(SESSION_MANAGER, this);
		rProcess.setParameter(EXTERNAL_SERVICE_ACCESS, this);
		rProcess.setParameter(STORAGE_ADAPTER_REGISTRY, this);
		rProcess.setParameter(PROCESS_USER, rUser);
		rProcess.setParameter(PROCESS_LOCALE,
							  getThreadLocalRequest().getLocale());

		return rProcess;
	}

	/***************************************
	 * Creates a new {@link ProcessState} instance from a certain process.
	 * Invoked by {@link #executeProcess(int, ProcessDescription)}.
	 *
	 * @param  rDescription The process definition
	 * @param  rProcess     The process
	 * @param  bReload      TRUE if all data elements should be forced to reload
	 *
	 * @return A new process state object
	 *
	 * @throws StorageException If the creation of a storage-based interaction
	 *                          data element fails
	 */
	@SuppressWarnings("boxing")
	private ProcessState createProcessState(ProcessDescription rDescription,
											Process			   rProcess,
											boolean			   bReload)
		throws StorageException
	{
		Integer		 sProcessId    = rProcess.getParameter(PROCESS_ID);
		String		 sProcessInfo  = rProcess.getParameter(PROCESS_INFO);
		ProcessState aProcessState;

		if (rProcess.isFinished())
		{
			aProcessState =
				new ProcessState(rDescription, sProcessId, sProcessInfo);
		}
		else
		{
			ProcessStep rInteractionStep = rProcess.getInteractionStep();

			List<DataElement<?>> aInteractionElements =
				createInteractionDataElements(rInteractionStep, bReload);

			aProcessState =
				new ProcessState(rDescription,
								 sProcessId,
								 sProcessInfo,
								 rInteractionStep.getName(),
								 aInteractionElements,
								 createViewDataElements(rInteractionStep),
								 getSpawnProcesses(rProcess),
								 getProcessStateFlags(rProcess,
													  rInteractionStep));

			if (rProcess.hasFlagParameter(MetaTypes.AUTHENTICATED))
			{
				aProcessState.setFlag(PROCESS_AUTHENTICATED);
			}

			String sStyle = rInteractionStep.getParameter(PROCESS_STEP_STYLE);

			if (sStyle != null)
			{
				aProcessState.setProperty(UserInterfaceProperties.STYLE,
										  sStyle);
			}

			Set<String> rModifiedEntities =
				EntityManager.getModifiedEntities().keySet();

			if (rModifiedEntities.size() > 0)
			{
				String sLocks = CollectionUtil.toString(rModifiedEntities, ",");

				aProcessState.setProperty(PROCESS_ENTITY_LOCKS, sLocks);
			}

			// reset modifications here to allow parameter relation listeners
			// to update parameters when the DataElements are applied after the
			// client returns from the interaction
			rInteractionStep.resetParameterModifications();
		}

		return aProcessState;
	}

	/***************************************
	 * Creates the data elements for certain relations of a relatable object.
	 * For each relation a single data element will be created by invoking the
	 * method {@link #createDataElement(Relatable, RelationType, Set)}. If that
	 * method returns NULL no data element will be added to the result for the
	 * respective relation.
	 *
	 * @param  rInteractionStep rObject The related object to query the
	 *                          relations from
	 *
	 * @return A new list containing the resulting data elements
	 *
	 * @throws StorageException If the initialization of a storage-based data
	 *                          element fails
	 */
	private List<DataElementList> createViewDataElements(
		ProcessFragment rInteractionStep) throws StorageException
	{
		DataElementFactory rFactory = getDataElementFactory();

		Set<RelationType<List<RelationType<?>>>> rViewParams =
			rInteractionStep.get(VIEW_PARAMS);

		List<DataElementList> aViewElements = null;

		if (rViewParams.size() > 0)
		{
			aViewElements = new ArrayList<>(rViewParams.size());

			for (RelationType<List<RelationType<?>>> rViewParam : rViewParams)
			{
				aViewElements.add((DataElementList) rFactory.getDataElement(rInteractionStep,
																			rViewParam));
			}
		}

		return aViewElements;
	}

	/***************************************
	 * Returns the process that is associated with a certain process description
	 * or process state and after preparing it for execution.
	 *
	 * @param  rDescription The process description or process state, depending
	 *                      on the current execution state of the process
	 * @param  rSessionData The data of the current session
	 * @param  rInitParams  Optional process initialization parameters or NULL
	 *                      for none
	 *
	 * @return The prepared process
	 *
	 * @throws ProcessException         If accessing the process fails
	 * @throws ServiceException         If the client is not authenticated or
	 *                                  preparing the process context fails
	 * @throws StorageException         If accessing a storage fails
	 * @throws IllegalArgumentException If the given process description is
	 *                                  invalid
	 */
	@SuppressWarnings("boxing")
	private Process getProcess(ProcessDescription rDescription,
							   SessionData		  rSessionData,
							   Relatable		  rInitParams)
		throws ProcessException, ServiceException, StorageException
	{
		Process rProcess = null;

		Map<Integer, Process> rUserProcessMap =
			rSessionData.get(USER_PROCESS_MAP);

		if (rDescription.getClass() == ProcessDescription.class)
		{
			// if the user reloads the browser windows the existing process can
			// be re-used instead of creating a new one
			rProcess =
				checkReuseExistingAppProcess(rDescription,
											 rUserProcessMap.values());

			if (rProcess != null)
			{
				// reload the current action if a process is re-used
				rProcess.set(INTERACTIVE_INPUT_PARAM, RELOAD_CURRENT_STEP);
			}
			else
			{
				ProcessDefinition rDefinition =
					aProcessDefinitions.get(rDescription.getDescriptionId());

				rProcess = createProcess(rDefinition, rSessionData);

				getSessionContext().get(PROCESS_LIST).add(rProcess);
				rUserProcessMap.put(rProcess.getParameter(PROCESS_ID),
									rProcess);
				initProcess(rProcess, rInitParams);
				setProcessInput(rProcess, rDescription.getProcessInput());
			}
		}
		else if (rDescription instanceof ProcessState)
		{
			ProcessState rProcessState = (ProcessState) rDescription;

			rProcess = rUserProcessMap.get(rProcessState.getProcessId());

			if (rProcess == null)
			{
				throw new IllegalStateException("NoProcessFound");
			}

			updateProcess(rProcess, rProcessState);

			if (hasProcessAuthentication())
			{
				initProcess(rProcess, rInitParams);
			}
		}
		else
		{
			throw new IllegalArgumentException("Unknown process reference: " +
											   rDescription);
		}

		return rProcess;
	}

	/***************************************
	 * Returns a set of flags for the current state of a process.
	 *
	 * @param  rProcess         The process
	 * @param  rInteractionStep The current interaction step
	 *
	 * @return
	 */
	private Set<ProcessStateFlag> getProcessStateFlags(
		Process		rProcess,
		ProcessStep rInteractionStep)
	{
		Set<ProcessStateFlag> aStepFlags = new HashSet<ProcessStateFlag>();

		if (rProcess.canRollbackToPreviousInteraction())
		{
			aStepFlags.add(ProcessStateFlag.ROLLBACK);
		}

		if (rInteractionStep.hasFlag(AUTO_CONTINUE) ||
			rInteractionStep.hasFlag(AUTO_UPDATE))
		{
			aStepFlags.add(ProcessStateFlag.AUTO_CONTINUE);
		}

		if (rInteractionStep.hasFlag(FINAL_STEP))
		{
			aStepFlags.add(ProcessStateFlag.FINAL_STEP);
		}

		if (rInteractionStep.hasFlag(DETAIL_STEP))
		{
			aStepFlags.add(ProcessStateFlag.DETAIL_STEP);
		}

		if (rInteractionStep.hasFlag(IMMEDIATE_INTERACTION))
		{
			aStepFlags.add(ProcessStateFlag.HAS_IMMEDIATE_INTERACTION);
		}

		return aStepFlags;
	}

	/***************************************
	 * Returns a list of process states for new processes that are to be spawned
	 * separate from the context of the current process. The process states must
	 * be stored in the list of the parameter {@link
	 * de.esoco.process.ProcessRelationTypes#NEW_PROCESSES}. This list will be
	 * cleared after it has been queried.
	 *
	 * @param  rProcess The process to query the new processes from
	 *
	 * @return A list of process states or NULL for none
	 */
	private List<ProcessState> getSpawnProcesses(Process rProcess)
	{
		List<ProcessState> rSpawnProcesses =
			rProcess.getParameter(SPAWN_PROCESSES);

		if (rSpawnProcesses.size() > 0)
		{
			rSpawnProcesses = new ArrayList<>(rSpawnProcesses);
		}
		else
		{
			rSpawnProcesses = null;
		}

		rProcess.getParameter(SPAWN_PROCESSES).clear();

		return rSpawnProcesses;
	}

	/***************************************
	 * Sets the process input parameters from a data element or a list of data
	 * elements.
	 *
	 * @param  rProcess      The process
	 * @param  rProcessInput The process input data element(s)
	 *
	 * @throws AuthenticationException If the current user is not authenticated
	 * @throws StorageException        If accessing storage data fails
	 */
	private void setProcessInput(Process		rProcess,
								 DataElement<?> rProcessInput)
		throws StorageException, AuthenticationException
	{
		if (rProcessInput != null)
		{
			List<DataElement<?>> rInputValues = new ArrayList<DataElement<?>>();

			if (rProcessInput instanceof DataElementList)
			{
				for (DataElement<?> rInputValue :
					 (DataElementList) rProcessInput)
				{
					rInputValues.add(rInputValue);
					rInputValue.setModified(true);
				}
			}
			else
			{
				rInputValues.add(rProcessInput);
				rProcessInput.setModified(true);
			}

			getDataElementFactory().applyDataElements(rInputValues, rProcess);
		}
	}

	/***************************************
	 * Updates a process from a certain process state that has been received
	 * from the client.
	 *
	 * @param  rProcess      The process to update
	 * @param  rProcessState The process state to update the process from
	 *
	 * @throws AuthenticationException If the client is not authenticated
	 * @throws StorageException        If a storage access fails
	 * @throws IllegalStateException   If the process is NULL
	 */
	@SuppressWarnings("boxing")
	private void updateProcess(Process rProcess, ProcessState rProcessState)
		throws AuthenticationException, StorageException
	{
		ProcessExecutionMode eMode = rProcessState.getExecutionMode();

		RelationType<?> rInteractionParam = null;

		if (eMode == ProcessExecutionMode.RELOAD)
		{
			rInteractionParam = RELOAD_CURRENT_STEP;
		}
		else
		{
			if (eMode == ProcessExecutionMode.EXECUTE)
			{
				List<DataElement<?>> rInteractionParams =
					rProcessState.getInteractionParams();

				List<DataElementList> rViewParams =
					rProcessState.getViewParams();

				getDataElementFactory().applyDataElements(rInteractionParams,
														  rProcess);
				getDataElementFactory().applyDataElements(rViewParams,
														  rProcess);
			}

			DataElement<?> rInteractionElement =
				rProcessState.getInteractionElement();

			if (rInteractionElement != null)
			{
				rInteractionParam =
					RelationType.valueOf(rInteractionElement.getName());

				if (rInteractionParam == null)
				{
					throw new IllegalStateException("Unknown interaction parameter: " +
													rInteractionParam);
				}

				InteractionEventType eEventType =
					rProcessState.getInteractionEventType();

				rProcess.setParameter(INTERACTIVE_INPUT_EVENT_TYPE, eEventType);

				// for legacy code
				rProcess.setParameter(INTERACTIVE_INPUT_ACTION_EVENT,
									  eEventType ==
									  InteractionEventType.ACTION);
			}
			else
			{
				rProcess.deleteRelation(INTERACTIVE_INPUT_EVENT_TYPE);
				rProcess.deleteRelation(INTERACTIVE_INPUT_ACTION_EVENT);
			}
		}

		rProcess.setParameter(INTERACTIVE_INPUT_PARAM, rInteractionParam);
	}
}
