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
import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.style.StyleData;

import java.util.Map;

import static de.esoco.data.element.DataElementList.LIST_DISPLAY_MODE;


/********************************************************************
 * A data element user interface that manages a {@link DataElementList} data
 * element in a child panel manager.
 *
 * @author eso
 */
public class DataElementListUI extends DataElementUI<DataElementList>
{
	//~ Instance fields --------------------------------------------------------

	private DataElementPanelManager aListPanelManager;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DataElementListUI()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void collectInput()
	{
		aListPanelManager.collectInput();
	}

	/***************************************
	 * Delegates the update to the child panel manager.
	 *
	 * @see DataElementUI#update()
	 */
	@Override
	public void update()
	{
		String sAddStyle = aListPanelManager.getStyleName();

		StyleData rNewStyle =
			applyElementStyle(getDataElement(),
							  PanelManager.addStyles(getBaseStyle(),
													 sAddStyle));

		applyElementProperties();
		aListPanelManager.getPanel().applyStyle(rNewStyle);
		aListPanelManager.updatePanel();
	}

	/***************************************
	 * Overridden to create a child {@link DataElementGridPanelManager} for the
	 * data element list.
	 *
	 * @see DataElementUI#buildDataElementUI(ContainerBuilder, StyleData)
	 */
	@Override
	protected Component buildDataElementUI(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle)
	{
		DataElementList rDataElementList = getDataElement();
		Panel		    rListPanel		 = null;

		ListDisplayMode eDisplayMode =
			rDataElementList.getProperty(LIST_DISPLAY_MODE,
										 ListDisplayMode.GRID);

		if (eDisplayMode == ListDisplayMode.GRID)
		{
			aListPanelManager =
				new DataElementGridPanelManager(getParent(),
												getElementStyleName(),
												rDataElementList);
		}
		else
		{
			aListPanelManager =
				new DataElementListPanelManager(getParent(), rDataElementList);
		}

		aListPanelManager.buildIn(rBuilder, rStyle);
		rListPanel = aListPanelManager.getPanel();
		setupInteractiveInputHandling(rListPanel, false);

		return rListPanel;
	}

	/***************************************
	 * @see DataElementUI#enableInteraction(boolean)
	 */
	@Override
	protected void enableInteraction(boolean bEnable)
	{
		aListPanelManager.enableInteraction(bEnable);
	}

	/***************************************
	 * Implemented to close an open child view.
	 *
	 * @see DataElementUI#dispose()
	 */
	@Override
	void dispose()
	{
		aListPanelManager.dispose();

		super.dispose();
	}

	/***************************************
	 * Returns the {@link DataElementGridPanelManager} that is used for the
	 * display of the {@link DataElementList} of this instance.
	 *
	 * @return The panel manager or NULL for none
	 */
	final DataElementPanelManager getPanelManager()
	{
		return aListPanelManager;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	void updateDataElement(DataElement<?>	   rNewElement,
						   Map<String, String> rElementErrors,
						   boolean			   bUpdateUI)
	{
		super.updateDataElement(rNewElement, rElementErrors, bUpdateUI);

		if (aListPanelManager != null)
		{
			aListPanelManager.updateFromDataElement(rNewElement,
													rElementErrors,
													bUpdateUI);
		}
	}
}
