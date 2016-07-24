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

import de.esoco.lib.property.Alignment;
import de.esoco.lib.property.LabelStyle;
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.RelativeSize;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.LayoutProperties.RELATIVE_WIDTH;
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
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		aGridFormatter =
			rGridFormatterFactory.createGridFormatter(getDataElementList());

		super.addComponents();

		// build last row
		buildCurrentRow();

		aGridFormatter = null;
	}

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
		private String sSmallPrefix;
		private String sMediumPrefix;
		private String sLargePrefix;

		private int   nCurrentColumn;
		private int[] aColumnWidths;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param nGridColumns  The number of grid columns
		 * @param sSmallPrefix  The prefix for small display column styles
		 * @param sMediumPrefix The prefix for medium display column styles
		 * @param sLargePrefix  The prefix for large display column styles
		 */
		public ColumnCountGridFormatter(int    nGridColumns,
										String sSmallPrefix,
										String sMediumPrefix,
										String sLargePrefix)
		{
			this.nGridColumns  = nGridColumns;
			this.sSmallPrefix  = sSmallPrefix;
			this.sMediumPrefix = sMediumPrefix;
			this.sLargePrefix  = sLargePrefix;
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
			StringBuilder aColumnStyle = new StringBuilder();
			int			  nColumnWidth = aColumnWidths[nCurrentColumn++];

			aColumnStyle.append(sSmallPrefix)
						.append(Math.min(nColumnWidth * 4, nGridColumns))
						.append(' ');
			aColumnStyle.append(sMediumPrefix)
						.append(Math.min(nColumnWidth * 2, nGridColumns))
						.append(' ');
			aColumnStyle.append(sLargePrefix).append(nColumnWidth);

			return addStyles(rColumnStyle, aColumnStyle.toString());
		}

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		public StyleData applyRowStyle(
			Collection<DataElementUI<?>> rRowUIs,
			StyleData					 rRowStyle)
		{
			nCurrentColumn = 0;
			aColumnWidths  = new int[rRowUIs.size()];

			int nRemainingWidth = nGridColumns;
			int nUnsetColumns   = 0;
			int nColumn		    = 0;

			for (DataElementUI<?> rColumnUI : rRowUIs)
			{
				DataElement<?> rDataElement = rColumnUI.getDataElement();

				@SuppressWarnings("boxing")
				int nElementWidth = rDataElement.getProperty(COLUMN_SPAN, -1);

				if (nElementWidth == -1)
				{
					RelativeSize eRelativeWidth =
						rDataElement.getProperty(RELATIVE_WIDTH, null);

					if (eRelativeWidth != null)
					{
						nElementWidth = eRelativeWidth.calcSize(nGridColumns);
					}
					else
					{
						nUnsetColumns++;
					}
				}

				aColumnWidths[nColumn++] = nElementWidth;

				if (nElementWidth > 0)
				{
					nRemainingWidth -= nElementWidth;
				}
			}

			int nUnsetWidth =
				nRemainingWidth / (nUnsetColumns > 0 ? nUnsetColumns : 1);

			for (nColumn = 0; nColumn < aColumnWidths.length; nColumn++)
			{
				int nWidth = aColumnWidths[nColumn];

				if (nWidth < 0)
				{
					if (--nUnsetColumns == 0)
					{
						nWidth = nRemainingWidth;
					}
					else
					{
						nWidth = nUnsetWidth;
					}

					nRemainingWidth -= nUnsetWidth;
				}

				aColumnWidths[nColumn] = nWidth;
			}

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
	 * An class that defines the formatting of grid data elements. Can be
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
