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
package de.esoco.gwt.client.app;

import de.esoco.ewt.app.EWTEntryPoint;

import de.esoco.gwt.client.ServiceRegistry;
import de.esoco.gwt.client.res.GwtApplicationResources;
import de.esoco.gwt.client.ui.AuthenticationPanelManager;
import de.esoco.gwt.shared.GwtApplicationServiceAsync;

import com.google.gwt.resources.client.CssResource;


/********************************************************************
 * A base class for GWT applications.
 *
 * @author eso
 */
public abstract class GwtApplication extends EWTEntryPoint
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Overridden as final to invoke the {@link #init()} and {@link #start()}
	 * methods before and after loading the EWT module.
	 *
	 * @see EWTEntryPoint#onModuleLoad()
	 */
	@Override
	public final void onModuleLoad()
	{
		AuthenticationPanelManager.setAuthenticationCookiePrefix(getCookiePrefix());
		ServiceRegistry.init(createApplicationService());
		injectApplicationCss();

		init();
		super.onModuleLoad();
		start();
	}

	/***************************************
	 * Must be implemented by subclasses to create the application
	 *
	 * @return
	 */
	protected abstract GwtApplicationServiceAsync createApplicationService();

	/***************************************
	 * Must be implemented by subclasses to provide the prefix string for
	 * application cookies that are set by the framework. It is recommended to
	 * use an uppercase string without punctuation characters.
	 *
	 * @return The cookie prefix
	 */
	protected abstract String getCookiePrefix();

	/***************************************
	 * Must be implemented by subclasses to start the application after it has
	 * been initialized.
	 */
	protected abstract void start();

	/***************************************
	 * Can be implemented by subclasses to perform additional initializations
	 * before application is started. The most important ones will automatically
	 * be performed by invoking the methods {@link #createApplicationService()},
	 * {@link #getCookiePrefix()}, and {@link #injectApplicationCss()}.
	 */
	protected void init()
	{
	}

	/***************************************
	 * Must be overridden to inject the CSS of the current application by
	 * invoking {@link CssResource#ensureInjected()} on it. The implementation
	 * must also invoke the superclass implementation to ensure that the full
	 * CSS chain is initialized
	 */
	protected void injectApplicationCss()
	{
		GwtApplicationResources.INSTANCE.css().ensureInjected();
	}
}
