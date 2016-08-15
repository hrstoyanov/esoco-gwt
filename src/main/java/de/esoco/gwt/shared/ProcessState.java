//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.gwt.shared;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;

import de.esoco.lib.property.InteractionEventType;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/********************************************************************
 * A process description subclass that provides additional information about the
 * current state of a process during it's execution. It also overrides the
 * methods {@link DataElement#equals(Object)} and {@link DataElement#hashCode()}
 * to only check an internal, unique and immutable ID that is assigned during
 * instance creation. This allows to use process state objects as map keys.
 *
 * @author eso
 */
public class ProcessState extends ProcessDescription
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * An enumeration of the process execution modes.
	 */
	public static enum ProcessExecutionMode
	{
		EXECUTE, RELOAD, ROLLBACK, CANCEL
	}

	/********************************************************************
	 * An enumeration of flags that describe the current state of the process
	 * and the interactive step at which the process paused. The possible values
	 * are:
	 *
	 * <ul>
	 *   <li>{@link #ROLLBACK}: The process can perform a rollback to the
	 *     previous interactive step.</li>
	 *   <li>{@link #AUTO_CONTINUE}: The step only displays data and expects
	 *     automatic re-execution of the process.</li>
	 *   <li>{@link #FINAL_STEP}: The step is the last interaction and the
	 *     process will be finished after the next re-execution.</li>
	 *   <li>{@link #DETAIL_STEP}: The step performs detail handling in an
	 *     enclosing context which may be visualized by a client.</li>
	 *   <li>{@link #HAS_IMMEDIATE_INTERACTION}: The step contains parameters
	 *     that will cause an immediate re-execution of the process. This can be
	 *     used as a hint to the user interface that no separate execution
	 *     control needs to be displayed.</li>
	 * </ul>
	 */
	public static enum ProcessStateFlag
	{
		ROLLBACK, AUTO_CONTINUE, FINAL_STEP, DETAIL_STEP,
		HAS_IMMEDIATE_INTERACTION
	}

	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the data element list that contains the interaction parameter
	 * data elements.
	 */
	public static final String INTERACTION_PARAMS_LIST = "InteractionParams";

	/**
	 * The name of the data element list that contains the data elements for
	 * additional views.
	 */
	public static final String VIEW_PARAMS_LIST = "InteractionParams";

	//~ Instance fields --------------------------------------------------------

	private int    nProcessId;
	private String sCurrentStep;
	private String sProcessInfo;

	private ProcessExecutionMode  rExecutionMode;
	private List<DataElement<?>>  rInteractionParams;
	private List<DataElementList> rViewParams;
	private DataElement<?>		  rInteractionElement;
	private InteractionEventType  eInteractionEventType;

	private Set<ProcessStateFlag> rCurrentStepFlags =
		Collections.<ProcessStateFlag>emptySet();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a finished process without any further
	 * interaction.
	 *
	 * @see #ProcessState(ProcessDescription, int, String, String, List, List,
	 *      Set)
	 */
	public ProcessState(ProcessDescription rDescription,
						int				   nProcessId,
						String			   sProcessInfo)
	{
		this(rDescription,
			 nProcessId,
			 sProcessInfo,
			 null,
			 null,
			 null,
			 Collections.<ProcessStateFlag>emptySet());
	}

	/***************************************
	 * Creates a new instance by copying the data from another process
	 * description (or state) and specific process state attributes.
	 *
	 * @param rDescription       The process description or state
	 * @param nProcessId         The ID of the described process
	 * @param sProcessInfo       An information string describing the process
	 * @param sCurrentStep       The name of the currently executed process step
	 * @param rInteractionParams The interaction parameter data elements
	 * @param rViewParams        The view parameter data elements
	 * @param rCurrentStepFlags  The flags for the current step
	 */
	public ProcessState(ProcessDescription    rDescription,
						int					  nProcessId,
						String				  sProcessInfo,
						String				  sCurrentStep,
						List<DataElement<?>>  rInteractionParams,
						List<DataElementList> rViewParams,
						Set<ProcessStateFlag> rCurrentStepFlags)
	{
		super(rDescription);

		this.nProcessId		    = nProcessId;
		this.sCurrentStep	    = sCurrentStep;
		this.sProcessInfo	    = sProcessInfo;
		this.rInteractionParams = rInteractionParams;
		this.rViewParams	    = rViewParams;
		this.rCurrentStepFlags  = rCurrentStepFlags;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	ProcessState()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the rollback state of the current process.
	 *
	 * @return TRUE if the current process can be rolled back to a previous
	 *         interaction
	 */
	public final boolean canRollback()
	{
		return rCurrentStepFlags.contains(ProcessStateFlag.ROLLBACK);
	}

	/***************************************
	 * Returns the name of the currently executed process step.
	 *
	 * @return The currently executed process step
	 */
	public final String getCurrentStep()
	{
		return sCurrentStep;
	}

	/***************************************
	 * Returns the process execution mode.
	 *
	 * @return The process execution mode
	 */
	public final ProcessState.ProcessExecutionMode getExecutionMode()
	{
		return rExecutionMode;
	}

	/***************************************
	 * Returns the data element that caused the re-execution of the process
	 * after during an interaction because of an interactive input event. In all
	 * other cases NULL will be returned.
	 *
	 * @return The data element that caused an interactive input event or NULL
	 *         for a non-interactive re-execution of the process
	 */
	public final DataElement<?> getInteractionElement()
	{
		return rInteractionElement;
	}

	/***************************************
	 * Returns TRUE if an interaction event has been caused by an action event
	 * or FALSE for a continuous selection event.
	 *
	 * @return TRUE for an action event, FALSE for a continuous selection event
	 */
	public final InteractionEventType getInteractionEventType()
	{
		return eInteractionEventType;
	}

	/***************************************
	 * Returns the data elements that represent the interaction parameters of
	 * the current process step.
	 *
	 * @return A list containing the interaction parameter data elements
	 */
	public List<DataElement<?>> getInteractionParams()
	{
		return rInteractionParams != null
			   ? rInteractionParams : Collections.<DataElement<?>>emptyList();
	}

	/***************************************
	 * Returns the ID of the process.
	 *
	 * @return The process ID
	 */
	public final int getProcessId()
	{
		return nProcessId;
	}

	/***************************************
	 * Returns an information string for the process or it's current state.
	 *
	 * @return The process information
	 */
	public final String getProcessInfo()
	{
		return sProcessInfo;
	}

	/***************************************
	 * Returns the data elements that represent the additional views in an
	 * interactive process step.
	 *
	 * @return A list containing the view parameter data elements
	 */
	public List<DataElementList> getViewParams()
	{
		return rViewParams != null ? rViewParams
								   : Collections.<DataElementList>emptyList();
	}

	/***************************************
	 * Returns TRUE if the current step contains interaction parameters that
	 * will cause an immediate re-execution of the process and therefore doesn't
	 * need a special interaction control ("next button") to proceed.
	 *
	 * @return TRUE if immediate execution parameters are present
	 */
	public final boolean hasImmedidateInteraction()
	{
		return rCurrentStepFlags.contains(ProcessStateFlag.HAS_IMMEDIATE_INTERACTION);
	}

	/***************************************
	 * Returns TRUE if the process execution should be continued automatically
	 * after an interaction.
	 *
	 * @return The auto continue flag
	 */
	public final boolean isAutoContinue()
	{
		return rCurrentStepFlags.contains(ProcessStateFlag.AUTO_CONTINUE);
	}

	/***************************************
	 * Returns TRUE if the current step performs some kind of detail handling in
	 * an enclosing context.
	 *
	 * @return TRUE if the current step performs detail handling
	 */
	public final boolean isDetailStep()
	{
		return rCurrentStepFlags.contains(ProcessStateFlag.DETAIL_STEP);
	}

	/***************************************
	 * Returns TRUE if the current step is the final interactive step in the
	 * process.
	 *
	 * @return TRUE for the final interactive step
	 */
	public final boolean isFinalStep()
	{
		return rCurrentStepFlags.contains(ProcessStateFlag.FINAL_STEP);
	}

	/***************************************
	 * Checks whether the process has finished execution. This will be the case
	 * if the name of the current step as returned by {@link #getCurrentStep()}
	 * is NULL.
	 *
	 * @return TRUE if the process is finished
	 */
	public boolean isFinished()
	{
		return sCurrentStep == null;
	}

	/***************************************
	 * Sets the execution mode. This method is intended to be used by clients to
	 * send the process execution mode to the server.
	 *
	 * @param rExecutionMode The new execution mode
	 */
	public final void setExecutionMode(ProcessExecutionMode rExecutionMode)
	{
		this.rExecutionMode = rExecutionMode;
	}

	/***************************************
	 * Will be set by the client side if an interactive input event occurred
	 * during an interaction. The parameter is the data element for which the
	 * event occurred.
	 *
	 * @param rElement   The data element that caused the interactive input
	 *                   event or NULL to reset
	 * @param eEventType bActionEvent The interaction event type
	 */
	public final void setInteractionElement(
		DataElement<?>		 rElement,
		InteractionEventType eEventType)
	{
		rInteractionElement   = rElement;
		eInteractionEventType = eEventType;
	}

	/***************************************
	 * Updates the ID of the process.
	 *
	 * @param nProcessId The new process id
	 */
	public final void setProcessId(int nProcessId)
	{
		this.nProcessId = nProcessId;
	}
}
