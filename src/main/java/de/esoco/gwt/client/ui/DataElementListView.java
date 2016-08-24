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
package de.esoco.gwt.client.ui;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.View;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.ViewStyle;

import de.esoco.gwt.client.res.EsocoGwtResources;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.StandardProperties;
import de.esoco.lib.property.ViewDisplayType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;
import static de.esoco.lib.property.LayoutProperties.VIEW_DISPLAY_TYPE;


/********************************************************************
 * A class that handles the display of a data element list in a separate EWT
 * view.
 *
 * @author eso
 */
public class DataElementListView
{
	//~ Static fields/initializers ---------------------------------------------

	private static Set<ViewStyle.Flag> aDefaultViewFlags =
		EnumSet.noneOf(ViewStyle.Flag.class);

	//~ Instance fields --------------------------------------------------------

	private DataElementListUI aViewUI;
	private View			  aView;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent      The parent data element panel manager
	 * @param rViewElement The data element list to be displayed in a view
	 */
	public DataElementListView(
		DataElementPanelManager rParent,
		DataElementList			rViewElement)
	{
		aViewUI =
			(DataElementListUI) DataElementUIFactory.create(rParent,
															rViewElement);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Sets the default view style flags for new views.
	 *
	 * @param rDefaultViewFlags The default view style flags
	 */
	public static final void setDefaultViewFlags(
		Set<ViewStyle.Flag> rDefaultViewFlags)
	{
		aDefaultViewFlags = EnumSet.copyOf(rDefaultViewFlags);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Collects the current input values into the corresponding data elements.
	 */
	public void collectInput()
	{
		aViewUI.collectInput();
	}

	/***************************************
	 * Hides this view.
	 */
	public void hide()
	{
		aView.setVisible(false);
	}

	/***************************************
	 * Returns the visibility of this view.
	 *
	 * @return TRUE if the view is visible, FALSE if it hidden
	 */
	public boolean isVisible()
	{
		return aView.isVisible();
	}

	/***************************************
	 * Shows this view.
	 */
	public void show()
	{
		ViewDisplayType eViewType =
			aViewUI.getDataElement()
				   .getProperty(VIEW_DISPLAY_TYPE,
								ViewDisplayType.MODAL_DIALOG);

		ContainerBuilder<View> rBuilder =
			createView(aViewUI.getParent().getContainer().getView(), eViewType);

		StyleData rStyle =
			PanelManager.addStyles(StyleData.DEFAULT,
								   aViewUI.getElementStyleName());

		aViewUI.buildUserInterface(rBuilder, rStyle);

		DataElementPanelManager    rViewManager = aViewUI.getPanelManager();
		Collection<DataElement<?>> rElements    =
			rViewManager.getDataElements();

		rViewManager.checkSelectionDependencies(rViewManager, rElements);

		aView = rBuilder.getContainer();

		aView.pack();
		aView.getContext().displayViewCentered(aView);
	}

	/***************************************
	 * Updates the data element of this view.
	 *
	 * @see DataElementUI#updateDataElement(DataElement, Map, boolean)
	 */
	public void updateDataElement(DataElementList	  rNewElement,
								  Map<String, String> rElementErrors,
								  boolean			  bUpdateUI)
	{
		aViewUI.updateDataElement(rNewElement, rElementErrors, bUpdateUI);
	}

	/***************************************
	 * Creates a view of a certain type to display the list element UIs.
	 *
	 * @param  rParentView The parent view
	 * @param  eViewType   The view type
	 *
	 * @return
	 */
	private ContainerBuilder<View> createView(
		View			rParentView,
		ViewDisplayType eViewType)
	{
		DataElementList		   rDataElementList = aViewUI.getDataElement();
		UserInterfaceContext   rContext		    = rParentView.getContext();
		ViewStyle			   rViewStyle	    = ViewStyle.DEFAULT;
		ContainerBuilder<View> aViewBuilder     = null;
		View				   aPanelView	    = null;

		Set<ViewStyle.Flag> aViewFlags = EnumSet.copyOf(aDefaultViewFlags);

		String sDialogStyle =
			EsocoGwtResources.INSTANCE.css().gfDataElementListDialog();

		if (eViewType == ViewDisplayType.MODAL_VIEW ||
			eViewType == ViewDisplayType.MODAL_DIALOG)
		{
			rViewStyle = ViewStyle.MODAL;
		}

		if (rDataElementList.hasProperty(VERTICAL_ALIGN))
		{
			if (rDataElementList.getProperty(VERTICAL_ALIGN, null) ==
				Alignment.END)
			{
				aViewFlags.add(ViewStyle.Flag.BOTTOM);
			}
			else
			{
				aViewFlags.remove(ViewStyle.Flag.BOTTOM);
			}
		}

		if (!aViewFlags.isEmpty())
		{
			rViewStyle = rViewStyle.withFlags(aViewFlags);
		}

		switch (eViewType)
		{
			case DIALOG:
			case MODAL_DIALOG:
				aPanelView = rContext.createDialog(rParentView, rViewStyle);

				break;

			case VIEW:
			case MODAL_VIEW:
				aPanelView = rContext.createChildView(rParentView, rViewStyle);
				break;
		}

		String sViewTitle = "$ti" +
							rDataElementList.getResourceId();

		sViewTitle =
			rDataElementList.getProperty(StandardProperties.TITLE, sViewTitle);

		aViewBuilder = new ContainerBuilder<View>(aPanelView);

		aPanelView.setTitle(sViewTitle);
		aPanelView.applyStyle(StyleData.DEFAULT.set(StyleData.WEB_ADDITIONAL_STYLES,
													sDialogStyle));

		return aViewBuilder;
	}
}
