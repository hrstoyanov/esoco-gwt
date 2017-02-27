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
package de.esoco.gwt.client.ui;

import de.esoco.data.element.DataElementList;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.style.StyleData;

import de.esoco.gwt.client.ui.GridFormatter.DefaultGridFormatterFactory;
import de.esoco.gwt.client.ui.GridFormatter.GridFormatterFactory;

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.LabelStyle;
import de.esoco.lib.property.Layout;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.LayoutProperties.VERTICAL_ALIGN;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.LABEL_STYLE;


/********************************************************************
 * A layout panel manager subclass that arranges the data element UIs in a CSS
 * grid by setting the corresponding styles based on the data element
 * properties.
 *
 * @author eso
 */
public class DataElementGridPanelManager extends DataElementLayoutPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	private static final StyleData FORM_LABEL_STYLE =
		ELEMENT_LABEL_STYLE.set(LABEL_STYLE, LabelStyle.FORM);

	private static final StyleData ROW_VALIGN_CENTER_STYLE =
		StyleData.DEFAULT.set(StyleData.WEB_ADDITIONAL_STYLES,
							  CSS.valignCenter());

	private static final StyleData ROW_VALIGN_BOTTOM_STYLE =
		StyleData.DEFAULT.set(StyleData.WEB_ADDITIONAL_STYLES,
							  CSS.valignBottom());

	private static GridFormatterFactory rGridFormatterFactory =
		new DefaultGridFormatterFactory();

	//~ Instance fields --------------------------------------------------------

	private GridFormatter aGridFormatter;

	private Map<DataElementUI<?>, StyleData> aCurrentRow =
		new LinkedHashMap<>();

	private StyleData aRowStyle = StyleData.DEFAULT;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DataElementGridPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, rDataElementList);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A global configuration method to set the grid formatter for all
	 * grid-based panels.
	 *
	 * @param rFactory rFormatter The global grid formatter
	 */
	public static void setGridFormatterFactory(GridFormatterFactory rFactory)
	{
		rGridFormatterFactory = rFactory;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the style of a completed row of data elements.
	 */
	protected void buildCurrentRow()
	{
		ContainerBuilder<?> aRowBuilder   = this;
		boolean			    bFirstElement = true;

		for (Entry<DataElementUI<?>, StyleData> rUiAndStyle :
			 aCurrentRow.entrySet())
		{
			DataElementUI<?> rUI		   = rUiAndStyle.getKey();
			StyleData		 rStyle		   = rUiAndStyle.getValue();
			int				 nElementCount = aCurrentRow.size();

			Layout eElementLayout =
				rUI.getDataElement().getProperty(LAYOUT, null);

			if (eElementLayout == Layout.GRID_ROW && nElementCount == 1)
			{
				rStyle =
					aGridFormatter.applyRowStyle(aCurrentRow.keySet(), rStyle);
			}
			else if (bFirstElement)
			{
				bFirstElement = false;

				StyleData rRowStyle =
					aGridFormatter.applyRowStyle(aCurrentRow.keySet(),
												 aRowStyle);

				aRowBuilder = addPanel(rRowStyle, Layout.GRID_ROW);
			}

			ContainerBuilder<?> rUiBuilder = aRowBuilder;

			boolean bAddLabel = !rUI.getDataElement().hasFlag(HIDE_LABEL);

			if (bAddLabel || eElementLayout != Layout.GRID_COLUMN)
			{
				StyleData aColumnStyle =
					aGridFormatter.applyColumnStyle(rUI, StyleData.DEFAULT);

				rUiBuilder =
					aRowBuilder.addPanel(aColumnStyle, Layout.GRID_COLUMN);

				if (bAddLabel)
				{
					String sLabel = rUI.createElementLabelString(getContext());

					if (sLabel.length() > 0)
					{
						rUI.createElementLabel(rUiBuilder,
											   FORM_LABEL_STYLE,
											   sLabel);
					}
				}
			}
			else if (eElementLayout != Layout.GRID_ROW)
			{
				// apply column count to elements with Layout GRID_COLUMN
				rStyle = aGridFormatter.applyColumnStyle(rUI, rStyle);
			}

			rUI.buildUserInterface(rUiBuilder, rStyle);
			applyElementProperties(rUI);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void buildDataElementUI(
		DataElementUI<?> aDataElementUI,
		StyleData		 rStyle)
	{
		if (!aDataElementUI.getDataElement().hasFlag(SAME_ROW))
		{
			buildCurrentRow();
			aCurrentRow.clear();
		}

		aCurrentRow.put(aDataElementUI, rStyle);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void buildElementUIs()
	{
		aGridFormatter =
			rGridFormatterFactory.createGridFormatter(getDataElementList());

		super.buildElementUIs();

		// build last row
		buildCurrentRow();

		aGridFormatter = null;
	}

	/***************************************
	 * Overridden to check the container style for vertical alignment.
	 *
	 * @see DataElementLayoutPanelManager#createPanel(ContainerBuilder,StyleData,
	 *      Layout)
	 */
	@Override
	protected ContainerBuilder<?> createPanel(ContainerBuilder<?> rBuilder,
											  StyleData			  rStyle,
											  Layout			  eLayout)
	{
		Alignment eVAlign = rStyle.getProperty(VERTICAL_ALIGN, null);

		if (eVAlign == Alignment.CENTER)
		{
			aRowStyle = ROW_VALIGN_CENTER_STYLE;
		}
		else if (eVAlign == Alignment.END)
		{
			aRowStyle = ROW_VALIGN_BOTTOM_STYLE;
		}

		return super.createPanel(rBuilder, rStyle, eLayout);
	}
}
