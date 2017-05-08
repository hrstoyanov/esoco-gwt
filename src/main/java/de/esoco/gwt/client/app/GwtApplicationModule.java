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

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.app.EWTModule;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.MainView;
import de.esoco.ewt.component.View;
import de.esoco.ewt.style.ViewStyle;


/********************************************************************
 * Base class for application modules.
 *
 * @author eso
 */
public abstract class GwtApplicationModule implements EWTModule
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see EWTModule#createModuleView(UserInterfaceContext)
	 */
	@Override
	public View createModuleView(UserInterfaceContext rContext)
	{
		MainView aMainView = rContext.createMainView(getMainViewStyle());

		ContainerBuilder<?> aMainViewBuilder =
			new ContainerBuilder<View>(aMainView);

		createApplicationPanel(aMainViewBuilder);

		return aMainView;
	}

	/***************************************
	 * @see EWTModule#showModuleView(UserInterfaceContext, View)
	 */
	@Override
	public void showModuleView(UserInterfaceContext rContext, View rView)
	{
		rView.pack();
		rContext.displayViewCentered(rView);
	}

	/***************************************
	 * Must be implemented by subclasses to create the application's main panel
	 * with the given builder.
	 *
	 * @param rBuilder The view builder
	 */
	protected abstract void createApplicationPanel(
		ContainerBuilder<?> rBuilder);

	/***************************************
	 * Returns the view style for the main view of this module. May be
	 * overridden to modify the default style {@link ViewStyle#FULL_SIZE}.
	 *
	 * @return The view style
	 */
	protected ViewStyle getMainViewStyle()
	{
		return ViewStyle.FULL_SIZE;
	}
}
