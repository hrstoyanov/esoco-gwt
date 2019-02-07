//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2019 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.logging.Log;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;


/********************************************************************
 * A server-side WebSocket to send client notifications over.
 *
 * @author eso
 */
public class ClientNotificationWebSocket extends Endpoint
{
	//~ Static fields/initializers ---------------------------------------------

	private static CommandServiceImpl rService;

	//~ Instance fields --------------------------------------------------------

	private Session rSession;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public ClientNotificationWebSocket()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the service this socket is associated with.
	 *
	 * @return The app service
	 */
	static CommandServiceImpl getService()
	{
		return rService;
	}

	/***************************************
	 * Sets the service this socket is associated with.
	 *
	 * @param rService The service
	 */
	static void setService(CommandServiceImpl rService)
	{
		ClientNotificationWebSocket.rService = rService;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Invoked when the socket is closed.
	 *
	 * @param rSession The closed session
	 * @param rReason  The close reason
	 */
	@Override
	public void onClose(Session rSession, CloseReason rReason)
	{
		Log.infof("WebSocket %s closed\n", rSession.getId());
	}

	/***************************************
	 * Invoked on socket errors.
	 *
	 * @param rSession The errored session
	 * @param eError   The error that occurred
	 */
	@Override
	public void onError(Session rSession, Throwable eError)
	{
		Log.error("WebSocket error", eError);
	}

	/***************************************
	 * Receives client messages.
	 *
	 * @param sMessage The message
	 */
	public void onMessage(String sMessage)
	{
		System.out.printf("Message: %s\n", sMessage);
	}

	/***************************************
	 * Invoked when the socket connection has been established.
	 *
	 * @param rSession The client session
	 * @param rConfig  The endpoint configuration
	 */
	@Override
	public void onOpen(Session rSession, EndpointConfig rConfig)
	{
		this.rSession = rSession;
		Log.infof("WebSocket %s opened\n", rSession.getId());

		rSession.addMessageHandler(
			new MessageHandler.Whole<String>()
			{
				@Override
				public void onMessage(String sMessage)
				{
					ClientNotificationWebSocket.this.onMessage(sMessage);
				}
			});
	}
}
