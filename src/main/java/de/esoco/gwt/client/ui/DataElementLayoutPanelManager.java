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

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.layout.FlowLayout;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.LabelStyle;
import de.esoco.lib.property.UserInterfaceProperties.Layout;
import de.esoco.lib.text.TextConvert;

import java.util.EnumSet;
import java.util.Set;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;


/********************************************************************
 * A panel manager for {@link DataElementList} instances that places the child
 * data elements of the data element list in a layout that is defined by the
 * {@link UserInterfaceProperties.Layout} of the data element list.
 *
 * @author eso
 */
public class DataElementLayoutPanelManager extends DataElementListPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Set<UserInterfaceProperties.Layout> GRID_LAYOUTS =
		EnumSet.of(UserInterfaceProperties.Layout.GRID,
				   UserInterfaceProperties.Layout.FORM,
				   UserInterfaceProperties.Layout.GROUP);

	private static final StyleData LAYOUT_ROW_STYLE =
		addStyles(StyleData.DEFAULT, "gfLayoutRow");

	private static final StyleData DATA_ELEMENT_WRAPPER_STYLE =
		addStyles(StyleData.DEFAULT, "gfDataElementWrapper");

	//~ Instance fields --------------------------------------------------------

	private ContainerBuilder<? extends Container> aRowBuilder;

	private int     nRowElements;
	private boolean bBuildGrid;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DataElementLayoutPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, rDataElementList);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		aRowBuilder  = this;
		nRowElements = 0;
		bBuildGrid   = GRID_LAYOUTS.contains(getLayout());

		super.addComponents();

		// set style of the last or only row
		setRowStyle();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void buildDataElementUI(
		DataElementUI<?> aDataElementUI,
		StyleData		 rStyle)
	{
		DataElement<?> rDataElement = aDataElementUI.getDataElement();

		ContainerBuilder<? extends Container> rUIBuilder = aRowBuilder;

		boolean bNewRow   = !rDataElement.hasFlag(SAME_ROW);
		boolean bAddLabel = !rDataElement.hasFlag(HIDE_LABEL);

		if (bBuildGrid)
		{
			if (bNewRow)
			{
				setRowStyle();

				aRowBuilder  = addPanel(LAYOUT_ROW_STYLE, Layout.GRID_ROW);
				rUIBuilder   = aRowBuilder;
				nRowElements = 0;
			}

			if (bAddLabel)
			{
				rUIBuilder =
					aRowBuilder.addPanel(DATA_ELEMENT_WRAPPER_STYLE,
										 new FlowLayout());

				aDataElementUI.createElementLabel(rUIBuilder,
												  ELEMENT_LABEL_STYLE.set(UserInterfaceProperties.LABEL_STYLE,
																		  LabelStyle.FORM));
			}
		}

		aDataElementUI.buildUserInterface(rUIBuilder, rStyle);
		nRowElements++;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected ContainerBuilder<Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		Layout				eLayout)
	{
		return rBuilder.addPanel(rStyleData, eLayout);
	}

	/***************************************
	 * Sets the style of a completed row of data elements.
	 */
	protected void setRowStyle()
	{
		if (nRowElements > 0 && bBuildGrid)
		{
			aRowBuilder.getContainer().getWidget()
					   .addStyleName("flex " +
									 TextConvert.numberString(nRowElements));
		}
	}
}
