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
import de.esoco.data.element.DataElementList.ListDisplayMode;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.layout.FillLayout;
import de.esoco.ewt.layout.FlowLayout;
import de.esoco.ewt.layout.FormLayout;
import de.esoco.ewt.layout.GenericLayout;
import de.esoco.ewt.layout.GroupLayout;
import de.esoco.ewt.layout.MenuLayout;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.LabelStyle;
import de.esoco.lib.text.TextConvert;

import java.util.EnumSet;
import java.util.Set;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;


/********************************************************************
 * A panel manager for {@link DataElementList} instances that places the child
 * data elements of the data element list in a layout that is defined by the
 * {@link ListDisplayMode} of the data element list.
 *
 * @author eso
 */
public class DataElementLayoutPanelManager extends DataElementListPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Set<ListDisplayMode> ROW_DISPLAY_MODES =
		EnumSet.of(ListDisplayMode.GRID,
				   ListDisplayMode.FORM,
				   ListDisplayMode.GROUP);

	private static final StyleData DATA_ELEMENT_ROW_STYLE =
		addStyles(StyleData.DEFAULT, "DataElementRow");

	//~ Instance fields --------------------------------------------------------

	private ContainerBuilder<Panel> aRowBuilder  = this;
	private int					    nRowElements = 0;

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
		super.addComponents();

		// set style of last (or only) row
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
		DataElement<?>		    rDataElement = aDataElementUI.getDataElement();
		ContainerBuilder<Panel> rUIBuilder   = aRowBuilder;

		if (ROW_DISPLAY_MODES.contains(getDisplayMode()))
		{
			if (!rDataElement.hasFlag(SAME_ROW))
			{
				setRowStyle();

				aRowBuilder  =
					addPanel(DATA_ELEMENT_ROW_STYLE, new FlowLayout());
				nRowElements = 0;
			}

			rUIBuilder =
				aRowBuilder.addPanel(StyleData.DEFAULT, new FlowLayout());

			if (rDataElement.hasFlag(HIDE_LABEL))
			{
				// add an empty label to prevent layout errors
				rUIBuilder.addLabel(StyleData.DEFAULT, "&nbsp;", null);
			}
			else
			{
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
		ListDisplayMode		eDisplayMode)
	{
		GenericLayout rLayout;

		switch (eDisplayMode)
		{
			case FILL:
				rLayout = new FillLayout();
				break;

			case FLOW:
			case GRID:
				rLayout = new FlowLayout();
				break;

			case FORM:
				rLayout = new FormLayout();
				break;

			case GROUP:
				rLayout = new GroupLayout();
				break;

			case MENU:
				rLayout = new MenuLayout();
				break;

			default:
				throw new IllegalStateException("Unsupported DataElementList mode " +
												eDisplayMode);
		}

		return rBuilder.addPanel(rStyleData, rLayout);
	}

	/***************************************
	 * Sets the style of a completed row of data elements.
	 */
	protected void setRowStyle()
	{
		if (nRowElements > 0 && ROW_DISPLAY_MODES.contains(getDisplayMode()))
		{
			aRowBuilder.getContainer().getWidget()
					   .addStyleName("flex " +
									 TextConvert.numberString(nRowElements));
		}
	}
}
