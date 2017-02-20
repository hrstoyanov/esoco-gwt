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
package de.esoco.gwt.client.app;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Button;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.Label;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.dialog.MessageBox;
import de.esoco.ewt.dialog.MessageBox.ResultHandler;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.graphics.Image;
import de.esoco.ewt.layout.DockLayout;
import de.esoco.ewt.layout.FillLayout;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.StyleFlag;

import de.esoco.gwt.client.res.EsocoGwtCss;
import de.esoco.gwt.client.res.EsocoGwtResources;
import de.esoco.gwt.client.ui.CommandResultHandler;
import de.esoco.gwt.client.ui.DataElementListView;
import de.esoco.gwt.client.ui.DataElementPanelManager;
import de.esoco.gwt.client.ui.DataElementPanelManager.InteractiveInputHandler;
import de.esoco.gwt.client.ui.DataElementTablePanelManager;
import de.esoco.gwt.client.ui.PanelManager;
import de.esoco.gwt.shared.GwtApplicationService;
import de.esoco.gwt.shared.ProcessDescription;
import de.esoco.gwt.shared.ProcessService;
import de.esoco.gwt.shared.ProcessState;
import de.esoco.gwt.shared.ProcessState.ProcessExecutionMode;
import de.esoco.gwt.shared.ServiceException;

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.Layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;

import static de.esoco.ewt.style.StyleData.WEB_ADDITIONAL_STYLES;

import static de.esoco.gwt.shared.StorageService.ERROR_ENTITY_LOCKED;

import static de.esoco.lib.property.ContentProperties.RESOURCE_ID;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.StyleProperties.STYLE;


/********************************************************************
 * A panel manager subclass that handles the interaction with a process that
 * runs in the service.
 *
 * @author eso
 */
public class ProcessPanelManager
	extends GwtApplicationPanelManager<Container,
									   GwtApplicationPanelManager<?, ?>>
	implements InteractiveInputHandler, CommandResultHandler<ProcessState>,
			   EWTEventHandler
{
	//~ Static fields/initializers ---------------------------------------------

	/** A prefix for the generation of labels from process names. */
	public static final String PROCESS_LABEL_PREFIX = "$lblPrc";

	private static final EsocoGwtCss CSS = EsocoGwtResources.INSTANCE.css();

	private static final StyleData TOP_PANEL_STYLE     =
		AlignedPosition.TOP.h(6)
						   .set(WEB_ADDITIONAL_STYLES, CSS.gaProcessTopPanel());
	private static final StyleData TOOLBAR_STYLE	   =
		StyleData.DEFAULT.set(WEB_ADDITIONAL_STYLES,
							  CSS.gaProcessButtonPanel());
	private static final StyleData PARAM_PANEL_STYLE   =
		AlignedPosition.CENTER.set(WEB_ADDITIONAL_STYLES,
								   CSS.gaProcessParamPanel());
	private static final StyleData SUMMARY_PANEL_STYLE =
		AlignedPosition.CENTER.set(WEB_ADDITIONAL_STYLES,
								   CSS.gaProcessSummaryPanel());

	private static final StyleData TITLE_LABEL_STYLE   =
		StyleData.DEFAULT.set(WEB_ADDITIONAL_STYLES, CSS.gaProcessTitle());
	private static final StyleData MESSAGE_LABEL_STYLE =
		StyleData.DEFAULT.set(WEB_ADDITIONAL_STYLES, CSS.gaErrorMessage());
	private static final StyleData SUMMARY_LABEL_STYLE = AlignedPosition.CENTER;

	//~ Instance fields --------------------------------------------------------

	private String  sProcessName;
	private boolean bShowNavigationBar;
	private boolean bRenderInline;

	private DataElementPanelManager aParamPanelManager;

	private Container rParamPanel;
	private Button    aPrevButton;
	private Button    aNextButton;
	private Button    aCancelButton;
	private Button    aReloadButton;
	private Label     aTitleLabel;
	private Label     aMessageLabel;
	private Image     rBusyImage = null;

	private ProcessState rProcessState  = null;
	private String		 sPreviousStep  = null;
	private String		 sPreviousStyle = "";

	private boolean bAutoContinue	   = false;
	private boolean bPauseAutoContinue = false;
	private boolean bCancelProcess     = false;
	private boolean bCancelled		   = false;

	private DataElement<?>		 rDeferredInteractionElement   = null;
	private InteractionEventType eDeferredInteractionEventType;

	private Map<String, DataElementListView> aProcessViews =
		Collections.emptyMap();

	private HandlerRegistration rUiInspectorEventHandler = null;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain process.
	 *
	 * @param rParent      The parent panel manager
	 * @param sProcessName The name of the process
	 */
	public ProcessPanelManager(
		GwtApplicationPanelManager<?, ?> rParent,
		String							 sProcessName)
	{
		this(rParent, sProcessName, true, false);
	}

	/***************************************
	 * Creates a new instance for a certain process.
	 *
	 * @param rParent            The parent panel manager
	 * @param sProcessName       The name of the process
	 * @param bShowNavigationBar TRUE to show the process navigation bar at the
	 *                           top, FALSE to show only the process parameters
	 * @param bRenderInline      TRUE to render the process UI in the parent
	 *                           container, FALSE to create a separate panel
	 *                           (may not be compatible with a navigation bar)
	 */
	public ProcessPanelManager(
		GwtApplicationPanelManager<?, ?> rParent,
		String							 sProcessName,
		boolean							 bShowNavigationBar,
		boolean							 bRenderInline)
	{
		super(rParent, CSS.gaProcessPanel());

		this.sProcessName	    = sProcessName;
		this.bShowNavigationBar = bShowNavigationBar;
		this.bRenderInline	    = bRenderInline;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a label for the current state of a process. If the process state
	 * is NULL or the process is finished an empty string will be returned.
	 *
	 * @param  rProcessState The process sate to create the label for
	 *
	 * @return The resulting label string
	 */
	public static String createProcessLabel(ProcessState rProcessState)
	{
		return rProcessState != null && !rProcessState.isFinished()
			   ? PROCESS_LABEL_PREFIX + rProcessState.getCurrentStep() : "";
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the message label visible and displays a message in the panels
	 * message area.
	 *
	 * @see GwtApplicationPanelManager#displayMessage(String, int)
	 */
	@Override
	public void displayMessage(String sMessage, int nDisplayTime)
	{
		if (aMessageLabel != null)
		{
			aMessageLabel.setVisible(true);
			aMessageLabel.setText(sMessage);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void dispose()
	{
		for (DataElementListView rView : aProcessViews.values())
		{
			rView.hide();
		}

		super.dispose();
	}

	/***************************************
	 * @see CommandResultHandler#handleCommandResult(DataElement)
	 */
	@Override
	public void handleCommandResult(ProcessState rNewState)
	{
		boolean bFinishProcess =
			rProcessState != null && rProcessState.isFinalStep();

		rProcessState = rNewState;
		bAutoContinue = rProcessState.isAutoContinue();

		if (bCancelProcess)
		{
			// cancel process if user confirmed the cancellation during an
			// automatically continued process step
			bCancelProcess = false;
			cancelProcess();
		}
		else if (!bCancelled)
		{
			update(bFinishProcess);
		}
	}

	/***************************************
	 * Handles the failure of an asynchronous call. The handling differs if the
	 * exception is recoverable or not. If not, a message will be displayed to
	 * the user. If the exception is a recoverable exception a rollback to the
	 * previous interaction will be done. The user can then input the data
	 * again.
	 *
	 * @param rCaught The exception that is caught
	 */
	@Override
	public void handleError(Throwable rCaught)
	{
		bAutoContinue = false;

		if (rCaught instanceof ServiceException &&
			((ServiceException) rCaught).isRecoverable())
		{
			ServiceException eService = (ServiceException) rCaught;

			String			    sMessage     = eService.getMessage();
			Map<String, String> rErrorParams = eService.getErrorParameters();
			ProcessState	    rNewState    = eService.getProcessState();

			if (sMessage.equals(ERROR_ENTITY_LOCKED))
			{
				Object[] rMessageArgs = rErrorParams.values().toArray();

				sMessage =
					getContext().getResourceString("msg" +
												   sMessage,
												   rMessageArgs);
			}
			else
			{
				if (rNewState != null)
				{
					rProcessState = rNewState;
				}

				if (!sMessage.startsWith("$"))
				{
					sMessage = "$msg" + sMessage;
				}

				buildParameterPanel(rErrorParams);
			}

			displayMessage(sMessage, 0);
		}
		else
		{
			bCancelled = true;
			buildSummaryPanel(rCaught);
		}

		setUserInterfaceState();
	}

	/***************************************
	 * Handles the events of user interface elements in this panel.
	 *
	 * @param rEvent The event
	 */
	@Override
	public void handleEvent(EWTEvent rEvent)
	{
		if (rEvent.getSource() instanceof Button)
		{
			Button rSource = (Button) rEvent.getSource();

			if (rSource.isEnabled())
			{
				lockUserInterface();

				if (rSource == aNextButton)
				{
					handleNextProcessStepEvent();
				}
				else if (rSource == aPrevButton)
				{
					handlePreviousProcessStepEvent();
				}
				else if (rSource == aCancelButton)
				{
					handleCancelProcessEvent();
				}
				else if (rSource == aReloadButton)
				{
					handleReload();
				}
			}
		}
	}

	/***************************************
	 * Executes the current process step to send the new value when an
	 * interactive input event occurs.
	 *
	 * @see InteractiveInputHandler#handleInteractiveInput(DataElement, InteractionEventType)
	 */
	@Override
	public void handleInteractiveInput(
		final DataElement<?> rDataElement,
		InteractionEventType eEventType)
	{
		if (isCommandExecuting())
		{
			// save the last interaction UI to prevent loss of input by
			// executing the process after the current handling ends
			rDeferredInteractionElement   = rDataElement;
			eDeferredInteractionEventType = eEventType;
		}
		else
		{
			rDeferredInteractionElement = null;

			lockUserInterface();

			for (DataElementListView rView : aProcessViews.values())
			{
				rView.collectInput();
			}

			rProcessState.setInteractionElement(rDataElement, eEventType);
			executeProcess(ProcessExecutionMode.EXECUTE, rProcessState, true);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		if (rBusyImage == null)
		{
			rBusyImage = getContext().createImage("$imBusy");
		}

		if (bShowNavigationBar)
		{
			buildTopPanel();
		}

		buildParameterPanel(null);
		setUserInterfaceState();

		addUiInspectorEventHandler();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ContainerBuilder<Container> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		ContainerBuilder<? extends Container> rPanelBuilder;

		if (bRenderInline)
		{
			rPanelBuilder = rBuilder;
		}
		else
		{
			rPanelBuilder =
				rBuilder.addPanel(rStyleData,
								  bShowNavigationBar
								  ? new DockLayout(false, false)
								  : new FillLayout());
		}

		return (ContainerBuilder<Container>) rPanelBuilder;
	}

	/***************************************
	 * Overridden to remove the UI inspector event handler registration.
	 *
	 * @see GwtApplicationPanelManager#processFinished(PanelManager, ProcessState)
	 */
	@Override
	protected void processFinished(
		PanelManager<?, ?> rProcessPanelManager,
		ProcessState	   rProcessState)
	{
		if (rUiInspectorEventHandler != null)
		{
			rUiInspectorEventHandler.removeHandler();
		}

		super.processFinished(rProcessPanelManager, rProcessState);
	}

	/***************************************
	 * Updates the UI from the process state after an interaction.
	 *
	 * @param bFinishProcess TRUE if the process needs to be finished
	 */
	protected void update(boolean bFinishProcess)
	{
		if (rProcessState.isFinished())
		{
			if (bFinishProcess)
			{
				processFinished(this, rProcessState);
			}
			else
			{
				setTitle(null);
				buildSummaryPanel(null);
			}
		}
		else
		{
			processUpdated(this, rProcessState);
			setTitle(rProcessState.getName());

			// update after other UI updates (e.g. animations) have finished
			Scheduler.get()
					 .scheduleDeferred(new ScheduledCommand()
				{
					@Override
					public void execute()
					{
						buildParameterPanel(null);

						if (rDeferredInteractionElement != null)
						{
							handleDeferredInteraction();
						}
						else if (bAutoContinue && !bPauseAutoContinue)
						{
							executeProcess(ProcessExecutionMode.EXECUTE,
										   rProcessState,
										   true);
						}
					}
				});
		}

		setUserInterfaceState();
	}

	/***************************************
	 * Lock the user interface against input events.
	 */
	void lockUserInterface()
	{
		if (bShowNavigationBar)
		{
			aNextButton.setImage(rBusyImage);
			aMessageLabel.setVisible(false);

			aPrevButton.setEnabled(false);
			aNextButton.setEnabled(false);
			aCancelButton.setEnabled(false);
			aReloadButton.setEnabled(false);
		}

		if (aParamPanelManager != null)
		{
			aParamPanelManager.enableInteraction(false);
		}

		for (DataElementListView rView : aProcessViews.values())
		{
			rView.enableInteraction(false);
		}
	}

	/***************************************
	 * Opens the UI inspector panel.
	 */
	void toggleUiInspector()
	{
		@SuppressWarnings("boxing")
		boolean bShowUiInspector =
			rProcessState.getProperty(ProcessService.SHOW_UI_INSPECTOR, false);

		rProcessState.setProperty(ProcessService.SHOW_UI_INSPECTOR,
								  !bShowUiInspector);

		executeProcess(ProcessExecutionMode.RELOAD, rProcessState, false);
	}

	/***************************************
	 * Adds a data element panel for a list of process parameter data elements.
	 *
	 * @param  rBuilder     The builder to add the panel with
	 * @param  rParams      The list of process parameter data elements
	 * @param  sCurrentStep The name of the current step
	 * @param  sStyle       An optional step style name (empty string for NONE)
	 *
	 * @return The panel manager for the new panel
	 */
	private DataElementPanelManager addParamDataElementPanel(
		ContainerBuilder<?>  rBuilder,
		List<DataElement<?>> rParams,
		String				 sCurrentStep,
		String				 sStyle)
	{
		DataElementPanelManager aPanelManager = null;

		if (rParams.size() == 0)
		{
			throw new IllegalArgumentException("No DataElements in " +
											   sProcessName + "." +
											   sCurrentStep);
		}

		DataElement<?> rFirstElement = rParams.get(0);

		if (rParams.size() == 1 &&
			rFirstElement instanceof DataElementList &&
			rFirstElement.getProperty(LAYOUT, Layout.TABLE) != Layout.TABLE)
		{
			aPanelManager =
				DataElementPanelManager.newInstance(this,
													(DataElementList)
													rFirstElement);
		}
		else
		{
			String sName = sProcessName + " " + sCurrentStep;

			if (sStyle != null && sStyle.length() > 0)
			{
				sName += " " + sStyle;
			}

			DataElementList rParamDataElements =
				new DataElementList(sName, rParams);

			// also set ResID
			rParamDataElements.setProperty(RESOURCE_ID, sName);

			aPanelManager =
				new DataElementTablePanelManager(this, rParamDataElements);
		}

		StyleData aPanelStyle =
			StyleData.DEFAULT.setFlags(StyleFlag.VERTICAL_ALIGN_TOP);

		aPanelManager.buildIn(rBuilder, aPanelStyle);
		aPanelManager.setInteractiveInputHandler(this);

		return aPanelManager;
	}

	/***************************************
	 * Adds a global event handler that listens for the invocation of the UI
	 * inspector.
	 */
	private void addUiInspectorEventHandler()
	{
//		rUiInspectorEventHandler =
//			Event.addNativePreviewHandler(new NativePreviewHandler()
//				{
//					@Override
//					public void onPreviewNativeEvent(NativePreviewEvent rEvent)
//					{
//						NativeEvent rNativeEvent = rEvent.getNativeEvent();
//
//						if ((rEvent.getTypeInt() & Event.ONKEYDOWN) != 0 &&
//							rNativeEvent.getKeyCode() == 73 &&
//							rNativeEvent.getAltKey() &&
//							rNativeEvent.getCtrlKey())
//						{
//							toggleUiInspector();
//						}
//					}
//				});
	}

	/***************************************
	 * Builds the panel to display the process parameters of the current
	 * interactive step.
	 *
	 * @param rErrorParams A list containing the names of erroneous parameters
	 *                     or NULL for none
	 */
	private void buildParameterPanel(Map<String, String> rErrorParams)
	{
		if (rProcessState != null && !rProcessState.isFinished())
		{
			String sCurrentStep = rProcessState.getCurrentStep();
			String sStepStyle   = rProcessState.getProperty(STYLE, "");

			List<DataElement<?>> rInteractionParams =
				rProcessState.getInteractionParams();

			if (aParamPanelManager != null &&
				sCurrentStep.equals(sPreviousStep) &&
				sStepStyle.equals(sPreviousStyle) &&
				rInteractionParams.size() ==
				aParamPanelManager.getDataElements().size())
			{
				aParamPanelManager.updateDataElements(rInteractionParams,
													  rErrorParams,
													  true);
			}
			else
			{
				List<DataElement<?>> rParams = rInteractionParams;

				if (aParamPanelManager != null)
				{
					aParamPanelManager.dispose();
				}

				removeParameterPanel();

				ContainerBuilder<? extends Container> aBuilder =
					bRenderInline
					? this : addPanel(PARAM_PANEL_STYLE, new FillLayout(true));

				rParamPanel    = aBuilder.getContainer();
				sPreviousStyle = sStepStyle;

				aParamPanelManager =
					addParamDataElementPanel(aBuilder,
											 rParams,
											 sCurrentStep,
											 sStepStyle);
			}

			manageProcessViews(rErrorParams);
		}
	}

	/***************************************
	 * Builds the summary panel.
	 *
	 * @param eException An error exception that occurred or NULL for success
	 */

	private void buildSummaryPanel(Throwable eException)
	{
		String sMessage =
			(eException != null ? "$msgProcessError" : "$msgProcessSuccess");

		removeParameterPanel();

		if (bShowNavigationBar)
		{
			aTitleLabel.setVisible(false);
		}

		if (bRenderInline)
		{
			addLabel(StyleData.DEFAULT, sMessage, null);
		}
		else
		{
			ContainerBuilder<Panel> aBuilder =
				addPanel(SUMMARY_PANEL_STYLE, new FillLayout(true));

			aBuilder.addLabel(SUMMARY_LABEL_STYLE, sMessage, null);
		}
	}

	/***************************************
	 * Builds the panel with the process control buttons.
	 */
	private void buildTopPanel()
	{
		ContainerBuilder<Panel> aToolbar =
			addToolbar(this, TOP_PANEL_STYLE, TOOLBAR_STYLE, 0);

		aPrevButton =
			addToolbarButton(aToolbar, "#imNavPrev", "$ttProcessPrevious");
		aNextButton =
			addToolbarButton(aToolbar, "#imNavNext", "$ttProcessNext");

		addToolbarSeparator(aToolbar);

		aCancelButton =
			addToolbarButton(aToolbar, "#imCancel", "$ttProcessCancel");

		String sTitle = createProcessLabel(rProcessState);

		addToolbarSeparator(aToolbar);
		aTitleLabel = aToolbar.addLabel(TITLE_LABEL_STYLE, sTitle, null);

		addToolbarSeparator(aToolbar);
		aReloadButton = addToolbarButton(aToolbar, "#imReload", "$ttReload");
		addToolbarSeparator(aToolbar);
		aMessageLabel =
			aToolbar.addLabel(MESSAGE_LABEL_STYLE, "", "#imWarning");

		aMessageLabel.setVisible(false);

		aNextButton.addEventListener(EventType.ACTION, this);
		aPrevButton.addEventListener(EventType.ACTION, this);
		aCancelButton.addEventListener(EventType.ACTION, this);
		aReloadButton.addEventListener(EventType.ACTION, this);
	}

	/***************************************
	 * Cancels the currently running process and notifies the parent panel
	 * manager.
	 */
	private void cancelProcess()
	{
		if (isCommandExecuting())
		{
			// if a command is currently executed delay the actual canceling
			// until handleCommandResult() is invoked
			bCancelProcess = true;
		}
		else
		{
			bCancelled = true;
			executeProcess(ProcessExecutionMode.CANCEL, rProcessState, false);
			processFinished(this, rProcessState);
		}
	}

	/***************************************
	 * Executes the process to receive the next process state.
	 *
	 * @param eMode         The execution mode
	 * @param rDescription  The process description or state
	 * @param bUpdateParams TRUE if the currently displayed parameters will only
	 *                      be updated by the execution
	 */
	private void executeProcess(final ProcessExecutionMode eMode,
								final ProcessDescription   rDescription,
								boolean					   bUpdateParams)
	{
		sPreviousStep = rProcessState.getCurrentStep();

		if (rDescription instanceof ProcessState)
		{
			((ProcessState) rDescription).setExecutionMode(eMode);
		}

		setClientSize(rDescription);
		executeCommand(GwtApplicationService.EXECUTE_PROCESS,
					   rDescription,
					   this);
	}

	/***************************************
	 * Handles the button selection from the confirmation message box displayed
	 * by {@link #handleCancelProcessEvent()}.
	 *
	 * @param nButton The selected button
	 */
	private void handleCancelConfirmation(int nButton)
	{
		bPauseAutoContinue = false;

		if (nButton == 1)
		{
			cancelProcess();
		}
		else if (bAutoContinue && !isCommandExecuting())
		{
			// restart an automatically continuing process if it had been
			// stopped in the meantime with bPauseAutoContinue
			executeProcess(ProcessExecutionMode.EXECUTE, rProcessState, true);
		}

		setUserInterfaceState();
	}

	/***************************************
	 * Handles the event of canceling the currently running process.
	 */
	private void handleCancelProcessEvent()
	{
		if (bCancelled)
		{
			processFinished(this, rProcessState);
		}
		else
		{
			if (rProcessState.hasImmedidateInteraction())
			{
				cancelProcess();
			}
			else
			{
				if (isCommandExecuting())
				{
					// pause a running process while cancel dialog is displayed
					bPauseAutoContinue = true;
				}

				MessageBox.showQuestion(getPanel().getView(),
										"$tiCancelProcess",
										"$msgCancelProcess",
										MessageBox.ICON_QUESTION,
					new ResultHandler()
					{
						@Override
						public void handleResult(int nButton)
						{
							handleCancelConfirmation(nButton);
						}
					});
			}
		}
	}

	/***************************************
	 * Process a deferred interaction to prevent loss of input.
	 */
	private void handleDeferredInteraction()
	{
		List<DataElement<?>> rInteractionParams =
			rProcessState.getInteractionParams();

		aParamPanelManager.updateDataElements(rInteractionParams, null, false);
		aParamPanelManager.collectInput();

		handleInteractiveInput(rDeferredInteractionElement,
							   eDeferredInteractionEventType);
	}

	/***************************************
	 * Handles the event of executing the next process step after an
	 * interaction.
	 */
	private void handleNextProcessStepEvent()
	{
		if (rProcessState.isFinished())
		{
			processFinished(this, rProcessState);
		}
		else
		{
			if (aParamPanelManager != null)
			{
				aParamPanelManager.collectInput();
			}

			executeProcess(ProcessExecutionMode.EXECUTE, rProcessState, false);
		}
	}

	/***************************************
	 * Handles a rollback to the previous step event.
	 */
	private void handlePreviousProcessStepEvent()
	{
		executeProcess(ProcessExecutionMode.ROLLBACK, rProcessState, false);
	}

	/***************************************
	 * Handles a reload event by updating the user interface.
	 */
	private void handleReload()
	{
		executeProcess(ProcessExecutionMode.RELOAD, rProcessState, true);
	}

	/***************************************
	 * Manages the views that are defined in the process state.
	 *
	 * @param rErrorParams The current error parameters
	 */
	private void manageProcessViews(Map<String, String> rErrorParams)
	{
		List<DataElementList> rViewParams = rProcessState.getViewParams();

		Map<String, DataElementListView> aNewViews =
			new HashMap<>(rViewParams.size());

		for (DataElementList rViewParam : rViewParams)
		{
			String			    sViewName = rViewParam.getName();
			DataElementListView aView     = aProcessViews.remove(sViewName);

			if (aView != null && aView.isVisible())
			{
				aView.updateDataElement(rViewParam, rErrorParams, true);
			}
			else
			{
				aView = new DataElementListView(aParamPanelManager, rViewParam);
				aView.show();
			}

			aNewViews.put(sViewName, aView);
		}

		// close views that are no longer listed
		for (DataElementListView rOldView : aProcessViews.values())
		{
			rOldView.hide();
		}

		aProcessViews = aNewViews;
	}

	/***************************************
	 * Removes the current center panel.
	 */
	private void removeParameterPanel()
	{
		if (bRenderInline)
		{
			getContainer().clear();
			aParamPanelManager = null;
		}
		else if (rParamPanel != null)
		{
			removeComponent(rParamPanel);
			rParamPanel = null;
		}
	}

	/***************************************
	 * Changes the title in the navigation bar if it is visible.
	 *
	 * @param sProcessStepName The name of the current process step or NULL to
	 *                         clear the title
	 */
	private void setTitle(String sProcessStepName)
	{
		if (bShowNavigationBar)
		{
			String sText  = "";
			Image  rImage = null;

			if (sProcessStepName != null)
			{
				String sImage = "$im" + sProcessStepName;

				rImage = getContext().createImage(sImage);
				sText  = createProcessLabel(rProcessState);
			}

			aTitleLabel.setText(sText);
			aTitleLabel.setImage(rImage);
		}
	}

	/***************************************
	 * Sets the state of the user interface elements from the current process
	 * state.
	 */
	private void setUserInterfaceState()
	{
		boolean bHasState =
			rProcessState != null && !rProcessState.isFinished();

		if (aParamPanelManager != null)
		{
			aParamPanelManager.enableInteraction(bHasState);
		}

		for (DataElementListView rView : aProcessViews.values())
		{
			rView.enableInteraction(bHasState);
		}

		if (bShowNavigationBar)
		{
			UserInterfaceContext rContext = getContext();

			String sNextImage;
			String sNextToolTip;

			if (rProcessState != null &&
				(rProcessState.isFinished() || rProcessState.isFinalStep()))
			{
				sNextImage   = "$imFinish";
				sNextToolTip = "$ttProcessFinish";
			}
			else
			{
				sNextImage   = "$imNavNext";
				sNextToolTip = "$ttProcessNext";
			}

			aNextButton.setImage(rContext.createImage(sNextImage));
			aNextButton.setToolTip(sNextToolTip);

			// if auto-continue is active only enable cancel to allow the
			// interruption of the process
			aCancelButton.setEnabled(bHasState);
			aReloadButton.setEnabled(!bAutoContinue && bHasState);
			aPrevButton.setEnabled(!bAutoContinue && !bCancelled && bHasState &&
								   rProcessState.canRollback());
			aNextButton.setEnabled(!bAutoContinue && !bCancelled &&
								   !(bHasState &&
									 rProcessState.hasImmedidateInteraction()));
		}
	}
}
