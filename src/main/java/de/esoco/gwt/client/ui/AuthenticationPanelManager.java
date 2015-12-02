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
package de.esoco.gwt.client.ui;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.DialogView;
import de.esoco.ewt.component.View;
import de.esoco.ewt.geometry.Rectangle;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.ViewStyle;

import de.esoco.gwt.shared.AuthenticatedService;
import de.esoco.gwt.shared.AuthenticationException;
import de.esoco.gwt.shared.Command;

import com.google.gwt.dom.client.Style.Position;


/********************************************************************
 * A panel manager subclass that performs the authentication of users. It also
 * implements the automatic re-execution of commands that have failed because a
 * missing or expired authentication.
 *
 * @author eso
 */
public abstract class AuthenticationPanelManager<C extends Container,
												 P extends AuthenticationPanelManager<?,
																					  ?>>
	extends PanelManager<C, P> implements LoginHandler
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * Enumeration of the possible login modes.
	 */
	public enum LoginMode { DIALOG, PAGE }

	//~ Static fields/initializers ---------------------------------------------

	private static String sAuthenticationCookiePrefix = "";

	//~ Instance fields --------------------------------------------------------

	private Command<?, ?>		    rLastCommand;
	private DataElement<?>		    rLastCommandData;
	private CommandResultHandler<?> rLastCommandHandler;

	private DialogView		  aLoginDialog;
	private LoginPanelManager aLoginPanel;
	private LoginMode		  eLoginMode = LoginMode.DIALOG;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public AuthenticationPanelManager(P rParent, String sPanelStyle)
	{
		super(rParent, sPanelStyle);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Returns the sCookiePrefix value.
	 *
	 * @return The sCookiePrefix value
	 */
	public static String getAuthenticationCookiePrefix()
	{
		return sAuthenticationCookiePrefix;
	}

	/***************************************
	 * Sets the authentication cookie prefix for the current application.
	 *
	 * @param sPrefix The cookie prefix
	 */
	public static void setAuthenticationCookiePrefix(String sPrefix)
	{
		sAuthenticationCookiePrefix = sPrefix;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see LoginHandler#loginFailed(Exception)
	 */
	@Override
	public void loginFailed(Exception rError)
	{
		handleError(rError);
	}

	/***************************************
	 * @see LoginHandler#loginSuccessful(DataElementList)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void loginSuccessful(DataElementList rUserData)
	{
		hideLoginPanel();

		if (rLastCommand == AuthenticatedService.GET_USER_DATA)
		{
			((CommandResultHandler<DataElementList>) rLastCommandHandler)
			.handleCommandResult(rUserData);
		}
		else
		{
			executeCommand((Command<DataElement<?>, DataElement<?>>)
						   rLastCommand,
						   rLastCommandData,
						   (CommandResultHandler<DataElement<?>>)
						   rLastCommandHandler);
		}
	}

	/***************************************
	 * Creates the login panel in the given container builder.
	 *
	 * @param  aDialogBuilder  The builder to create the login panel with
	 * @param  bReauthenticate TRUE if the invocation is only for a
	 *                         re-authentication of the current user
	 *
	 * @return The {@link LoginPanelManager} instance
	 */
	protected LoginPanelManager buildLoginPanel(
		ContainerBuilder<?> aDialogBuilder,
		boolean				bReauthenticate)
	{
		final LoginPanelManager aLoginPanelManager =
			new LoginPanelManager(this,
								  this,
								  getAuthenticationCookiePrefix(),
								  bReauthenticate);

		aLoginPanelManager.buildIn(aDialogBuilder, AlignedPosition.CENTER);

		return aLoginPanelManager;
	}

	/***************************************
	 * Checks whether the current user is authenticated. If not the login
	 * procedure will be initiated causing the login dialog to be displayed. On
	 * successful authentication the {@link #userAuthenticated(DataElementList)}
	 * method will be invoked. Subclasses should invoked this method before
	 * performing an action that requires an authenticated user.
	 */
	protected void checkAuthentication()
	{
		executeCommand(AuthenticatedService.GET_USER_DATA,
					   null,
			new CommandResultHandler<DataElementList>()
			{
				@Override
				public void handleCommandResult(DataElementList rUserData)
				{
					userAuthenticated(rUserData);
				}
			});
	}

	/***************************************
	 * Executes a certain command on the server.
	 *
	 * @param rCommand       The command to execute
	 * @param rData          The data to be processed by the command
	 * @param rResultHandler The result handler to process the command result in
	 *                       case of a successful command execution
	 *
	 * @see   PanelManager#executeCommand(Command, DataElement,
	 *        CommandResultHandler)
	 */
	@Override
	protected <T extends DataElement<?>, R extends DataElement<?>> void executeCommand(
		Command<T, R>			rCommand,
		T						rData,
		CommandResultHandler<R> rResultHandler)
	{
		this.rLastCommand		 = rCommand;
		this.rLastCommandData    = rData;
		this.rLastCommandHandler = rResultHandler;

		super.executeCommand(rCommand, rData, rResultHandler);
	}

	/***************************************
	 * @see PanelManager#handleCommandFailure(Command, Throwable)
	 */
	@Override
	protected void handleCommandFailure(
		Command<?, ?> rCommand,
		Throwable	  rCaught)
	{
		if (rCaught instanceof AuthenticationException)
		{
			login(rCommand != AuthenticatedService.GET_USER_DATA);
		}
		else
		{
			super.handleCommandFailure(rCommand, rCaught);
		}
	}

	/***************************************
	 * Hides the login panel. The default implementation hides the login dialog.
	 * Subclasses can override this panel to modify the login panel handling.
	 */
	protected void hideLoginPanel()
	{
		if (aLoginPanel != null)
		{
			if (eLoginMode == LoginMode.DIALOG)
			{
				aLoginDialog.setVisible(false);
				aLoginDialog = null;
			}
			else
			{
				removeComponent(aLoginPanel.getContainer());
				aLoginPanel = null;
			}
		}
	}

	/***************************************
	 * Will be invoked to perform a login if no user is authenticated currently.
	 *
	 * @param bReauthenticate TRUE if this is a re-authentication because of an
	 *                        expired session
	 */
	protected void login(boolean bReauthenticate)
	{
		if (eLoginMode == LoginMode.DIALOG)
		{
			UserInterfaceContext rContext = getContext();

			aLoginDialog = rContext.createDialog(null, ViewStyle.MODAL);

			ContainerBuilder<View> aDialogBuilder =
				new ContainerBuilder<View>(aLoginDialog);

			aLoginPanel = buildLoginPanel(aDialogBuilder, bReauthenticate);

			aLoginDialog.setTitle("$tiLogin");
			aLoginDialog.pack();

			Rectangle rScreen = rContext.getDefaultScreen().getClientArea();

			rContext.displayView(aLoginDialog,
								 rScreen.getX() + rScreen.getWidth() / 2,
								 rScreen.getY() + rScreen.getHeight() / 3,
								 AlignedPosition.CENTER,
								 true);

			rContext.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						aLoginPanel.requestFocus();
					}
				});
		}
		else
		{
			removeApplicationPanel();

			aLoginPanel = buildLoginPanel(this, bReauthenticate);
			aLoginPanel.getContainer().getElement().getStyle()
					   .setPosition(Position.RELATIVE);
			aLoginPanel.requestFocus();
		}
	}

	/***************************************
	 * Sets the login mode of this panel. If set to dialog the login panel will
	 * appear in a dialog view above the (initial) application view. Otherwise
	 * the login panel will replace the application view and the subclass must
	 * implement the method {@link #removeApplicationPanel()}.
	 *
	 * <p>To change the login mode (the default is dialog mode) this method must
	 * be invoked before the login takes place. To change the login mode while
	 * the application runs it must be done while no login is in progress, e.g.
	 * after invocation of {@link #hideLoginPanel()}.</p>
	 *
	 * @param eMode bUseLoginDialog TRUE for a login dialog, FALSE for a panel
	 */
	protected void setLoginMode(LoginMode eMode)
	{
		eLoginMode = eMode;
	}

	/***************************************
	 * Will be invoked to initialize this instance from the user data obtained
	 * through {@link #checkAuthentication()}. Subclasses can override this
	 * method to perform user-specific initializations. The default
	 * implementation does nothing.
	 *
	 * @param rUserData The user data received from the server
	 */
	protected void userAuthenticated(DataElementList rUserData)
	{
	}
}
