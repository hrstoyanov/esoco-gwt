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

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.StringDataElement;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Button;
import de.esoco.ewt.component.Label;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.component.TextField;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.layout.GridLayout;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.StyleFlag;

import de.esoco.gwt.client.ServiceRegistry;
import de.esoco.gwt.client.res.GwtFrameworkResource;
import de.esoco.gwt.shared.AuthenticatedService;

import java.util.Date;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;


/********************************************************************
 * A panel manager implementation that displays and handles a login panel.
 *
 * @author t.kuechenthal
 */
public class LoginPanelManager extends PanelManager<Panel, PanelManager<?, ?>>
	implements EWTEventHandler
{
	//~ Static fields/initializers ---------------------------------------------

	private static final String USER_NAME_COOKIE  = "_USER";
	private static final String SESSION_ID_COOKIE = "_SID";

	//~ Instance fields --------------------------------------------------------

	private final LoginHandler rLoginHandler;
	private final boolean	   bReauthenticate;

	private final String sUserCookie;
	private final String sSessionCookie;

	private TextField aUserField;
	private TextField aPasswordField;
	private Button    aLoginButton;
	private Label     aFailureMessage;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see PanelManager#PanelManager(PanelManager, String)
	 */
	public LoginPanelManager(PanelManager<?, ?> rParent,
							 LoginHandler		rLoginHandler,
							 String				sCookiePrefix,
							 boolean			bReauthenticate)
	{
		super(rParent, GwtFrameworkResource.INSTANCE.css().gfLoginPanel());

		this.rLoginHandler   = rLoginHandler;
		this.bReauthenticate = bReauthenticate;

		sUserCookie    = sCookiePrefix + USER_NAME_COOKIE;
		sSessionCookie = sCookiePrefix + SESSION_ID_COOKIE;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Handles the action event of the login button.
	 *
	 * @param rEvent The event
	 */
	@Override
	public void handleEvent(EWTEvent rEvent)
	{
		if (rEvent.getSource() == aUserField)
		{
			aPasswordField.requestFocus();
		}
		else
		{
			login();
		}
	}

	/***************************************
	 * Sets the focus to the first input field.
	 */
	public void requestFocus()
	{
		if (aUserField != null && aUserField.getText().length() == 0)
		{
			aUserField.requestFocus();
		}
		else
		{
			aPasswordField.requestFocus();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		String sUserName = Cookies.getCookie(sUserCookie);

		ContainerBuilder<Panel> aBuilder =
			addPanel(AlignedPosition.CENTER, new GridLayout(2, true, 3));

		aBuilder.addLabel(AlignedPosition.TOP,
						  null,
						  GwtFrameworkResource.INSTANCE.imLogin());
		aBuilder.addLabel(AlignedPosition.TOP, "$lblLogin", null);

		aBuilder.addLabel(StyleData.DEFAULT.setFlags(StyleFlag.HORIZONTAL_ALIGN_RIGHT),
						  "$lblLoginName",
						  null);

		if (bReauthenticate)
		{
			aBuilder.addLabel(StyleData.DEFAULT, sUserName, null);
		}
		else
		{
			aUserField = aBuilder.addTextField(StyleData.DEFAULT, "");
			aUserField.setText(sUserName);
			aUserField.addEventListener(EventType.ACTION, this);
		}

		aBuilder.addLabel(StyleData.DEFAULT.setFlags(StyleFlag.HORIZONTAL_ALIGN_RIGHT),
						  "$lblPassword",
						  null);
		aPasswordField =
			aBuilder.addTextField(StyleData.DEFAULT.setFlags(StyleFlag.PASSWORD),
								  "");

		aBuilder.addLabel(StyleData.DEFAULT, "", null);

		String sError = GwtFrameworkResource.INSTANCE.css().error();

		StyleData rErrorStyle =
			StyleData.DEFAULT.set(StyleData.WEB_ADDITIONAL_STYLES, sError);

		StyleData rButtonStyle =
			StyleData.DEFAULT.setFlags(StyleFlag.HORIZONTAL_ALIGN_CENTER);

		aFailureMessage =
			aBuilder.addLabel(rErrorStyle, "$lblLoginFailed", null);

		aBuilder.addLabel(StyleData.DEFAULT, "", null);

		aLoginButton = aBuilder.addButton(rButtonStyle, "$btnLogin", null);
		aPasswordField.addEventListener(EventType.ACTION, this);
		aLoginButton.addEventListener(EventType.ACTION, this);
		aFailureMessage.setVisible(false);
	}

	/***************************************
	 * {@inheritDoc}
	 */

	@Override
	protected ContainerBuilder<Panel> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		return rBuilder.addPanel(rStyleData);
	}

	/***************************************
	 * Handles login failures. There are two ways this method can be invoked.
	 * This class first tries to connect to the server with user name and
	 * password set to NULL to check for an existing authentication. If that
	 * call fails the internal container builder reference will not be NULL and
	 * will be used to create the login components to query for the user login.
	 * The builder reference will then be set to NULL.
	 *
	 * <p>If the builder reference is NULL an invalid user name and/or password
	 * has been used to connect and only an error message will be displayed (by
	 * invoking {@link WorkflowPanelManager#handleError(Throwable)}).</p>
	 *
	 * @param rCaught The exception that occurred
	 */
	private void handleLoginFailure(Throwable rCaught)
	{
		aLoginButton.setEnabled(true);
		aFailureMessage.setVisible(true);
		rLoginHandler.loginFailed((Exception) rCaught);
	}

	/***************************************
	 * Handles a successful authentication by invoking the login method {@link
	 * LoginHandler#loginSuccesful()}.
	 *
	 * @param rUserData The user data instance returned by the call to the
	 *                  service method {@link WorkflowService#connect(String,
	 *                  String)}
	 */
	private void handleLoginSuccess(DataElementList rUserData)
	{
		String sSessionID =
			rUserData.getProperty(AuthenticatedService.SESSION_ID, null);

		if (sSessionID == null)
		{
			throw new IllegalArgumentException("No Session ID in user data");
		}

		if (aPasswordField != null)
		{
			aPasswordField.setText("");
			aFailureMessage.setVisible(false);

			aUserField     = null;
			aPasswordField = null;
		}

		Cookies.setCookie(sSessionCookie, sSessionID);
		rLoginHandler.loginSuccessful(rUserData);
	}

	/***************************************
	 * Performs the login with the data from the input fields.
	 */
	private void login()
	{
		aLoginButton.setEnabled(false);

		String sUserName =
			bReauthenticate ? Cookies.getCookie(sUserCookie)
							: aUserField.getText();

		String sPassword   = aPasswordField.getText();
		String sSessionId  = Cookies.getCookie(sSessionCookie);
		Date   aExpiryDate = new Date();

		StringDataElement aLoginData =
			new StringDataElement(sUserName, sPassword);

		StringBuilder aLoginUserInfo = new StringBuilder();

		aLoginUserInfo.append("UserAgent: ");
		aLoginUserInfo.append(Window.Navigator.getUserAgent());
		aLoginUserInfo.append("\nApp: ");
		aLoginUserInfo.append(Window.Navigator.getAppName());
		aLoginUserInfo.append(" (");
		aLoginUserInfo.append(Window.Navigator.getAppCodeName());
		aLoginUserInfo.append(")\nVersion: ");
		aLoginUserInfo.append(Window.Navigator.getAppVersion());
		aLoginUserInfo.append("\nPlatform: ");
		aLoginUserInfo.append(Window.Navigator.getPlatform());

		aLoginData.setProperty(AuthenticatedService.LOGIN_USER_INFO,
							   aLoginUserInfo.toString());

		CalendarUtil.addMonthsToDate(aExpiryDate, 3);
		Cookies.setCookie(sUserCookie, sUserName, aExpiryDate);

		if (sSessionId != null)
		{
			aLoginData.setProperty(AuthenticatedService.SESSION_ID, sSessionId);
		}

		ServiceRegistry.getCommandService()
					   .executeCommand(AuthenticatedService.LOGIN,
									   aLoginData,
			new AsyncCallback<DataElementList>()
			{
				@Override
				public void onFailure(Throwable rCaught)
				{
					handleLoginFailure(rCaught);
				}

				@Override
				public void onSuccess(DataElementList rResult)
				{
					handleLoginSuccess(rResult);
				}
			});
	}
}
