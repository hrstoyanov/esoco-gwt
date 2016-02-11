//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.data.element.StringDataElement;


/********************************************************************
 * A data element that describes a process for the use on the client.
 *
 * @author eso
 */
public class ProcessDescription extends StringDataElement
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	private static final String SEPARATOR_NAME = "Separator";

	//~ Instance fields --------------------------------------------------------

	private int     nId;
	private boolean bInputRequired;

	private DataElement<?> rProcessInput = null;

	private int nClientWidth;
	private int nClientHeight;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance with certain attributes.
	 *
	 * @param sName          The process name
	 * @param sDescription   The process description
	 * @param nProcessId     The internal process definition ID
	 * @param bInputRequired TRUE if the process can only be executed with an
	 *                       input value
	 */
	public ProcessDescription(String  sName,
							  String  sDescription,
							  int	  nProcessId,
							  boolean bInputRequired)
	{
		super(sName, sDescription, null, null);

		this.nId		    = nProcessId;
		this.bInputRequired = bInputRequired;
	}

	/***************************************
	 * Default constructor for serialization.
	 */
	ProcessDescription()
	{
	}

	/***************************************
	 * Copy constructor for subclasses.
	 *
	 * @param rOther The other instance to copy the state of
	 */
	ProcessDescription(ProcessDescription rOther)
	{
		this(rOther.getName(),
			 rOther.getValue(),
			 rOther.nId,
			 rOther.bInputRequired);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Creates a new instance that can be used to indicate a separator in UI
	 * listings between description.
	 *
	 * @return A new separator UI description
	 */
	public static final ProcessDescription createSeparator()
	{
		return new ProcessDescription(SEPARATOR_NAME, null, -1, false);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the height of the client area of the current user's web browser.
	 *
	 * @return The client area height
	 */
	public final int getClientHeight()
	{
		return nClientHeight;
	}

	/***************************************
	 * Returns the width of the client area of the current user's web browser.
	 *
	 * @return The client area width
	 */
	public final int getClientWidth()
	{
		return nClientWidth;
	}

	/***************************************
	 * Returns the ID of the described process. This method is only intended to
	 * be used internally by the framework.
	 *
	 * @return The ID
	 */
	public final int getDescriptionId()
	{
		return nId;
	}

	/***************************************
	 * Returns an entity ID to be used as an initialization parameter for the
	 * process execution.
	 *
	 * @return The entity ID or -1 for none
	 */
	public final DataElement<?> getProcessInput()
	{
		return rProcessInput;
	}

	/***************************************
	 * Checks whether the process execution requires an input value. If so it
	 * must be set with the method {@link #setProcessInput(DataElement)}.
	 *
	 * @return TRUE if a process input value is required
	 */
	public final boolean isInputRequired()
	{
		return bInputRequired;
	}

	/***************************************
	 * Checks whether this description is a placeholder for a separator between
	 * descriptions.
	 *
	 * @return TRUE if this instance is a separator
	 */
	public final boolean isSeparator()
	{
		return getName().equals(SEPARATOR_NAME);
	}

	/***************************************
	 * Sets the size of the client area of the current user's web browser. This
	 * will be used to transfer the available UI area and it's proportions to
	 * the server upon and during a process execution.
	 *
	 * @param nWidth  The width of the client area
	 * @param nHeight The height of the client area
	 */
	public final void setClientSize(int nWidth, int nHeight)
	{
		nClientWidth  = nWidth;
		nClientHeight = nHeight;
	}

	/***************************************
	 * Sets an entity ID that will be set as a parameter on the process when it
	 * is started. This allows to execute a process for an entity that has been
	 * selected on the client.
	 *
	 * @param rInput rProcessInput rEntityId The entityId value
	 */
	public final void setProcessInput(DataElement<?> rInput)
	{
		rProcessInput = rInput;
	}
}
