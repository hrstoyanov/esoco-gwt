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

import java.util.Map;


/********************************************************************
 * The base class for all exceptions that can be thrown by services.
 *
 * @author eso
 */
public class ServiceException extends Exception
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private Map<String, String> rErrorParameters;
	private String			    sCauseMessage;
	private boolean			    bRecoverable;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see Exception#Exception()
	 */
	public ServiceException()
	{
	}

	/***************************************
	 * @see Exception#Exception(String)
	 */
	public ServiceException(String sMessage)
	{
		super(sMessage);
	}

	/***************************************
	 * @see Exception#Exception(Throwable)
	 */
	public ServiceException(Throwable eCause)
	{
		this(null, eCause);
	}

	/***************************************
	 * Creates a new instance of a recoverable service exception. A recoverable
	 * exception will return TRUE from the {@link #isRecoverable()} method. It
	 * provides additional information about the causing problem by returning a
	 * map containing the causing parameters and a description of the error from
	 * the method {@link #getErrorParameters()}.
	 *
	 * @param sMessage         The error message
	 * @param rErrorParameters The list of error
	 */
	public ServiceException(
		String				sMessage,
		Map<String, String> rErrorParameters)
	{
		super(sMessage);

		this.rErrorParameters = rErrorParameters;
		this.bRecoverable     = true;
	}

	/***************************************
	 * @see Exception#Exception(String, Throwable)
	 */
	public ServiceException(String sMessage, Throwable eCause)
	{
		super(sMessage, eCause);

		do
		{
			sCauseMessage = eCause.getMessage();
			eCause		  = eCause.getCause();
		}
		while (eCause != null);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the message of the root cause exception of this instance. This is
	 * needed for clients because the chain of cause messages is lost on GWT
	 * serialization.
	 *
	 * @return The causing exception's message or NULL for none
	 */
	public final String getCauseMessage()
	{
		return sCauseMessage;
	}

	/***************************************
	 * Returns the parameters of a recoverable exception. The result is a map of
	 * strings that contains the erroneous parameters as the keys and the
	 * associated error message as the values.
	 *
	 * @return A map from erroneous parameters to error messages (NULL for none)
	 */
	public Map<String, String> getErrorParameters()
	{
		return rErrorParameters;
	}

	/***************************************
	 * Indicates whether this exception is recoverable.
	 *
	 * @return TRUE if this
	 */
	public boolean isRecoverable()
	{
		return bRecoverable;
	}
}
