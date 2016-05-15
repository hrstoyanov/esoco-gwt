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
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties.LabelStyle;
import de.esoco.lib.property.UserInterfaceProperties.Layout;
import de.esoco.lib.property.UserInterfaceProperties.RelativeSize;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMN_SPAN;
import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.LAYOUT;
import static de.esoco.lib.property.UserInterfaceProperties.RELATIVE_WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.SAME_ROW;


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

	private static GridFormatterFactory rGridFormatterFactory =
		new DefaultGridFormatterFactory();

	//~ Instance fields --------------------------------------------------------

	private Map<DataElementUI<?>, StyleData> aCurrentRow =
		new LinkedHashMap<>();

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
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		super.addComponents();

		// build last row
		buildCurrentRow();
	}

	/***************************************
	 * Sets the style of a completed row of data elements.
	 */
	protected void buildCurrentRow()
	{
		GridFormatter aGridFormatter =
			rGridFormatterFactory.createGridFormatter(getDataElementList());

		ContainerBuilder<?> aRowBuilder   = this;
		boolean			    bFirstElement = true;

		for (Entry<DataElementUI<?>, StyleData> rUiAndStyle :
			 aCurrentRow.entrySet())
		{
			DataElementUI<?> rUI    = rUiAndStyle.getKey();
			StyleData		 rStyle = rUiAndStyle.getValue();

			Layout eElementLayout =
				rUI.getDataElement().getProperty(LAYOUT, null);

			if (eElementLayout == Layout.GRID_ROW && aCurrentRow.size() == 1)
			{
				rStyle =
					aGridFormatter.applyRowStyle(aCurrentRow.keySet(), rStyle);
			}
			else if (bFirstElement)
			{
				StyleData rRowStyle =
					aGridFormatter.applyRowStyle(aCurrentRow.keySet(),
												 StyleData.DEFAULT);

				aRowBuilder   = addPanel(rRowStyle, Layout.GRID_ROW);
				bFirstElement = false;
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
					rUI.createElementLabel(rUiBuilder, FORM_LABEL_STYLE);
				}
			}
			else if (eElementLayout != Layout.GRID_ROW)
			{
				rStyle = aGridFormatter.applyColumnStyle(rUI, rStyle);
			}

			rUI.buildUserInterface(rUiBuilder, rStyle);
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

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface for factories that create instances of {@link
	 * GridFormatter}.
	 *
	 * @author eso
	 */
	public static interface GridFormatterFactory
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Creates a new grid formatter instance.
		 *
		 * @param  rGridElement The data element to create the formatter for
		 *
		 * @return The new grid formatter
		 */
		public GridFormatter createGridFormatter(DataElementList rGridElement);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A {@link GridFormatter} implementation that uses a fixed column count for
	 * the grid size calculation.
	 *
	 * @author eso
	 */
	public static class ColumnCountGridFormatter extends GridFormatter
	{
		//~ Instance fields ----------------------------------------------------

		private int    nGridColumns;
		private String sPrefix;

		private int nRowElementCount;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nGridColumns The number of grid columns
		 * @param sPrefix      The prefix for column styles
		 */
		public ColumnCountGridFormatter(int nGridColumns, String sPrefix)
		{
			this.nGridColumns = nGridColumns;
			this.sPrefix	  = sPrefix;
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public StyleData applyColumnStyle(
			DataElementUI<?> rColumUI,
			StyleData		 rColumnStyle)
		{
			DataElement<?> rDataElement = rColumUI.getDataElement();

			@SuppressWarnings("boxing")
			int nElementColumns = rDataElement.getProperty(COLUMN_SPAN, -1);

			if (nElementColumns == -1)
			{
				RelativeSize eRelativeWidth =
					rDataElement.getProperty(RELATIVE_WIDTH, null);

				nElementColumns =
					nGridColumns /
					(eRelativeWidth != null ? eRelativeWidth.ordinal() + 1
											: nRowElementCount);
			}

			rColumnStyle = addStyles(rColumnStyle, sPrefix + nElementColumns);

			return rColumnStyle;
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public StyleData applyRowStyle(
			Collection<DataElementUI<?>> rRowUIs,
			StyleData					 rRowStyle)
		{
			nRowElementCount = rRowUIs.size();

			return super.applyRowStyle(rRowUIs, rRowStyle);
		}
	}

	/********************************************************************
	 * A default grid formatter factory that returns instances of {@link
	 * GridFormatter} which returns the input styles unchanged.
	 *
	 * @author eso
	 */
	public static class DefaultGridFormatterFactory
		implements GridFormatterFactory
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public GridFormatter createGridFormatter(DataElementList rGridElement)
		{
			return new GridFormatter();
		}
	}

	/********************************************************************
	 * An interface that defines the formatting of grid data elements. Can be
	 * subclassed by an UI extension to provide the formatting for it's grid
	 * layout mechanism. The row style method will always be invoked first and
	 * then the column style method for each column UI. Therefore
	 * implementations may cache aggregated data from the row style invocation
	 * to refer to it during the column formatting.
	 *
	 * @author eso
	 */
	public static class GridFormatter
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * This method will be invoked to apply the style of a column in the
		 * grid layout to the given style data. The default implementation
		 * simply returns the original style object.
		 *
		 * @param  rColumUI     The data element UI of the column
		 * @param  rColumnStyle The original column style to apply the layout
		 *                      styles to
		 *
		 * @return The row style data (a new instance if modified as {@link
		 *         StyleData} is immutable)
		 */
		public StyleData applyColumnStyle(
			DataElementUI<?> rColumUI,
			StyleData		 rColumnStyle)
		{
			return rColumnStyle;
		}

		/***************************************
		 * This method will be invoked to apply the style of a row in the grid
		 * layout to the given style data. The default implementation simply
		 * returns the original style object.
		 *
		 * @param  rRowUIs   The data element UIs for the data elements in the
		 *                   row
		 * @param  rRowStyle The original row style to apply the layout styles
		 *                   to
		 *
		 * @return The row style data (a new instance if modified as {@link
		 *         StyleData} is immutable)
		 */
		public StyleData applyRowStyle(
			Collection<DataElementUI<?>> rRowUIs,
			StyleData					 rRowStyle)
		{
			return rRowStyle;
		}
	}
}
