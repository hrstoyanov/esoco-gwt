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
package de.esoco.gwt.server;

import de.esoco.data.SessionManager;
import de.esoco.data.element.DataElement;

import de.esoco.gwt.shared.Command;
import de.esoco.gwt.shared.CommandService;
import de.esoco.gwt.shared.ServiceException;

import de.esoco.lib.logging.Log;
import de.esoco.lib.reflect.ReflectUtil;
import de.esoco.lib.text.TextConvert;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/********************************************************************
 * The base implementation of the {@link CommandService} interface. The
 * interface method {@link #executeCommand(Command, DataElement)} has a default
 * implementation that dispatches command executions to subclass methods named
 * {@code handle <CommandName>} by means of reflection. Therefore a subclass
 * only needs to implement a corresponding handler method for each command it
 * defines in it's public service interface.
 *
 * @author eso
 */
public abstract class CommandServiceImpl extends RemoteServiceServlet
	implements CommandService
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private ResourceBundle aResource;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see CommandService#executeCommand(Command, DataElement)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DataElement<?>, R extends DataElement<?>> R executeCommand(
		Command<T, R> rCommand,
		T			  rData) throws ServiceException
	{
		checkCommandExecution(rCommand, rData);

		String sMethod =
			"handle" + TextConvert.capitalizedIdentifier(rCommand.getName());

		try
		{
			Method rHandler =
				ReflectUtil.findAnyPublicMethod(getClass(), sMethod);

			if (rHandler == null)
			{
				throw new ServiceException("Missing command handling method " +
										   sMethod);
			}

			return (R) rHandler.invoke(this, rData);
		}
		catch (Exception e)
		{
			throw handleException(e);
		}
	}

	/***************************************
	 * @see SessionManager#getAbsoluteFileName(String)
	 */
	public String getAbsoluteFileName(String sFileName)
	{
		return getServletContext().getRealPath(sFileName);
	}

	/***************************************
	 * @see RemoteServiceServlet#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("%s[%s]",
							 getClass().getSimpleName(),
							 getServletContext().getServerInfo());
	}

	/***************************************
	 * Must be implemented to return the name of the properties file that
	 * contains the application resources. If no resource file is needed NULL
	 * may be returned.
	 *
	 * @return The string properties file name or NULL for none
	 */
	protected abstract String getApplicationStringPropertiesFile();

	/***************************************
	 * Performs a checks whether the execution of a command is possible. This
	 * method can be overridden by subclasses to implement authentication or
	 * command (parameter) validations. To deny the command execution the
	 * implementation must throw an exception.
	 *
	 * @param  rCommand The command that is about to be executed
	 * @param  rData    The command argument
	 *
	 * @throws ServiceException If the command is not allowed to be executed
	 */
	protected <T extends DataElement<?>> void checkCommandExecution(
		Command<T, ?> rCommand,
		T			  rData) throws ServiceException
	{
	}

	/***************************************
	 * Returns a string from the application resources if such exist. Subclasses
	 * must implement the method {@link #getApplicationStringPropertiesFile()}
	 * to define the resource file to be loaded.
	 *
	 * @param  sKey The key identifying the resource
	 *
	 * @return The resource string or NULL if not found
	 */
	protected String getResourceString(String sKey)
	{
		if (aResource == null)
		{
			String sFile = getApplicationStringPropertiesFile();

			if (sFile != null)
			{
				aResource = readResourceFile(sFile);
			}
		}

		String sResourceString = null;

		if (aResource != null)
		{
			try
			{
				sResourceString = aResource.getString(sKey);
			}
			catch (MissingResourceException e)
			{
				// just return NULL
			}
		}

		return sResourceString;
	}

	/***************************************
	 * Logs, processes, and if necessary converts an exception. If the argument
	 * exception is a service exception it will be returned directly. All other
	 * exceptions will be converted into a service exception.
	 *
	 * @param  e The exception to handle
	 *
	 * @return Always returns a service exception
	 */
	protected ServiceException handleException(Exception e)
	{
		if (e instanceof InvocationTargetException)
		{
			Throwable t = e.getCause();

			if (t instanceof Exception)
			{
				e = (Exception) t;
			}
		}

		if (!(e instanceof ServiceException &&
			  ((ServiceException) e).isRecoverable()))
		{
			Log.error(e.getMessage(), e);
		}

		if (e instanceof ServiceException)
		{
			return (ServiceException) e;
		}
		else
		{
			return new ServiceException(e);
		}
	}

	/***************************************
	 * Reads a resource file with a certain name.
	 *
	 * @param  sFileName The resource file name
	 *
	 * @return A new resource bundle instance from the given file
	 *
	 * @throws IllegalArgumentException If the resource file could not be found
	 */
	protected ResourceBundle readResourceFile(String sFileName)
	{
		try
		{
			sFileName = getAbsoluteFileName(sFileName);

			InputStreamReader rReader =
				new InputStreamReader(new FileInputStream(sFileName), "UTF-8");

			return new PropertyResourceBundle(rReader);
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
