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

import de.esoco.lib.property.UserInterfaceProperties.ViewDisplayType;

import java.util.Collection;
import java.util.Map;

import static de.esoco.lib.property.UserInterfaceProperties.VIEW_DISPLAY_TYPE;


/********************************************************************
 * A class that handles the display of a data element list in a separate EWT
 * view.
 *
 * @author eso
 */
public class DataElementListView
{
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
		ContainerBuilder<View> aViewBuilder     = null;
		View				   aPanelView	    = null;

		String sDialogStyle =
			EsocoGwtResources.INSTANCE.css().gfDataElementListDialog();

		switch (eViewType)
		{
			case DIALOG:
			case MODAL_DIALOG:
				aPanelView =
					rContext.createDialog(rParentView,
										  eViewType ==
										  ViewDisplayType.MODAL_DIALOG
										  ? ViewStyle.MODAL
										  : ViewStyle.DEFAULT);
				break;

			case VIEW:
			case MODAL_VIEW:
				aPanelView =
					rContext.createChildView(rParentView,
											 eViewType ==
											 ViewDisplayType.MODAL_VIEW
											 ? ViewStyle.MODAL
											 : ViewStyle.DEFAULT);
				break;
		}

		aViewBuilder = new ContainerBuilder<View>(aPanelView);
		aPanelView.setTitle("$ti" + rDataElementList.getResourceId());
		aPanelView.applyStyle(StyleData.DEFAULT.set(StyleData.WEB_ADDITIONAL_STYLES,
													sDialogStyle));

		return aViewBuilder;
	}
}
