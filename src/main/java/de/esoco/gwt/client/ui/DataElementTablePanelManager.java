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
import de.esoco.data.element.ListDataElement;

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.Label;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.layout.GridLayout;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.Layout;
import de.esoco.lib.property.UserInterfaceProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static de.esoco.lib.property.LayoutProperties.COLUMN_SPAN;
import static de.esoco.lib.property.LayoutProperties.HTML_HEIGHT;
import static de.esoco.lib.property.LayoutProperties.HTML_WIDTH;
import static de.esoco.lib.property.LayoutProperties.ROW_SPAN;
import static de.esoco.lib.property.LayoutProperties.SAME_ROW;
import static de.esoco.lib.property.StateProperties.INITIAL_FOCUS;
import static de.esoco.lib.property.StyleProperties.HEADER_LABEL;
import static de.esoco.lib.property.StyleProperties.HIDE_LABEL;
import static de.esoco.lib.property.StyleProperties.STYLE;


/********************************************************************
 * A panel manager that organizes data elements in a table layout.
 *
 * @author eso
 */
public class DataElementTablePanelManager extends DataElementPanelManager
{
	//~ Instance fields --------------------------------------------------------

	private List<DataElement<?>> aDataElements;

	private boolean bHasOptions;
	private boolean bHasLabels;
	private int     nElementColumns;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance from the elements in a {@link DataElementList}.
	 *
	 * @param rParent          The parent panel manager
	 * @param rDataElementList sName A name for this instance that will be set
	 *                         as an additional GWT style name
	 */
	public DataElementTablePanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, rDataElementList);

		initDataElements(rDataElementList.getDataElements());
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see DataElementPanelManager#getDataElements()
	 */
	@Override
	public final Collection<DataElement<?>> getDataElements()
	{
		return aDataElements;
	}

	/***************************************
	 * @see DataElementPanelManager#setElementVisibility(DataElementUI, boolean)
	 */
	@Override
	public void setElementVisibility(
		DataElementUI<?> rElementUI,
		boolean			 bVisible)
	{
		getDataElementsLayout().changeCellStyle(getContainer(),
												rElementUI
												.getElementComponent(),
												CSS.gfEmptyCell(),
												!bVisible);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updateDataElements(List<DataElement<?>> rNewDataElements,
								   Map<String, String>  rElementErrors,
								   boolean				bUpdateUI)
	{
		boolean bUpdate = containsSameElements(aDataElements, rNewDataElements);

		if (bUpdate)
		{
			aDataElements = new ArrayList<DataElement<?>>(rNewDataElements);

			for (DataElement<?> rElement : aDataElements)
			{
				DataElementUI<?> rElementUI =
					getDataElementUIs().get(rElement.getName());

				rElementUI.updateDataElement(rElement,
											 rElementErrors,
											 bUpdateUI);
			}
		}
		else
		{
			dispose();
			initDataElements(rNewDataElements);
			getDataElementsLayout().setGridCount(calcLayoutColumns(aDataElements));
			rebuild();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void updateFromDataElement(DataElement<?>	  rNewDataElement,
									  Map<String, String> rErrorMessages,
									  boolean			  bUpdateUI)
	{
		if (rNewDataElement instanceof DataElementList)
		{
			String sElementStyle = rNewDataElement.getProperty(STYLE, null);
			String sStyleName    = getStyleName();

			if (sElementStyle != null && sStyleName.indexOf(sElementStyle) < 0)
			{
				sStyleName = sStyleName + " " + sElementStyle;
			}

			rNewDataElement.setProperty(STYLE, sStyleName);

			StyleData rNewStyle =
				DataElementUI.applyElementStyle(rNewDataElement,
												getBaseStyle());

			getContainer().applyStyle(rNewStyle);

			updateDataElements(((ListDataElement<DataElement<?>>)
								rNewDataElement).getElements(),
							   rErrorMessages,
							   bUpdateUI);
		}
		else
		{
			super.updateFromDataElement(rNewDataElement,
										rErrorMessages,
										bUpdateUI);
		}
	}

	/***************************************
	 * Adds the user interfaces for the data elements in this panel.
	 */
	@Override
	protected void addComponents()
	{
		UserInterfaceContext rContext = getContext();
		List<Label>			 aHeaders = null;

		int     nColumn		  = nElementColumns;
		int     nHeaderColumn = 0;
		int     nElementCount = aDataElements.size();
		boolean bFocusSet     = false;

		for (int nElement = 0; nElement < nElementCount; nElement++)
		{
			DataElement<?> rDataElement = aDataElements.get(nElement);

			String sWidth  = rDataElement.getProperty(HTML_WIDTH, null);
			String sHeight = rDataElement.getProperty(HTML_HEIGHT, null);

			boolean bNewRow    = !rDataElement.hasFlag(SAME_ROW);
			boolean bImmutable = rDataElement.isImmutable();
			boolean bHideLabel =
				rDataElement.hasFlag(HIDE_LABEL) ||
				rDataElement.hasFlag(HEADER_LABEL);

			boolean isChildViewElement =
				rDataElement.hasProperty(UserInterfaceProperties.VIEW_DISPLAY_TYPE);

			int nExtraColumns = bNewRow && bHideLabel && bHasLabels ? 1 : 0;

			DataElementUI<?> aElementUI =
				DataElementUIFactory.create(this, rDataElement);

			String sStyle = aElementUI.getElementStyleName();

			if (bNewRow && !isChildViewElement)
			{
				int nRowElement = nElement;
				int nCol	    = 0;

				aHeaders	  = null;
				nHeaderColumn = 0;

				while (nCol < nElementColumns && nRowElement < nElementCount)
				{
					DataElement<?> rElement = aDataElements.get(nRowElement++);

					if (aHeaders == null && rElement.hasFlag(HEADER_LABEL))
					{
						aHeaders = new ArrayList<>();
					}

					nCol += rElement.getIntProperty(COLUMN_SPAN, 1);
				}

				if (aHeaders != null)
				{
					addElementRow(aElementUI, "", nColumn);
					nColumn = 0;

					for (int i = nElement; i < nRowElement; i++)
					{
						DataElement<?> rElement = aDataElements.get(i);

						String sHeaderStyle = rElement.getResourceId();

						Label aHeader =
							addLabel(addStyles(HEADER_LABEL_STYLE,
											   sHeaderStyle),
									 "",
									 null);

						aHeaders.add(aHeader);
						addCellStyles(CSS.gfDataElementHeader(),
									  sHeaderStyle + "Header");
						nColumn++;
					}
				}

				addElementRow(aElementUI, sStyle + "Label", nColumn);
				nColumn = 0;
			}

			if (bImmutable)
			{
				sStyle = CSS.readonly() + " " + sStyle;
			}

			StyleData aElementStyle = addStyles(ELEMENT_STYLE, sStyle);

			// element UI must be registered before building to allow
			// cross-panel references of selection dependencies
			getDataElementUIs().put(rDataElement.getName(), aElementUI);
			aElementUI.buildUserInterface(this, aElementStyle);

			if ("100%".equals(sWidth) && "100%".equals(sHeight))
			{
				aElementUI.getElementComponent().getWidget()
						  .setSize("100%", "100%");
			}

			if (!bNewRow || bHideLabel)
			{
				if (aHeaders != null && rDataElement.hasFlag(HEADER_LABEL))
				{
					Label rHeaderLabel = aHeaders.get(nHeaderColumn);

					rHeaderLabel.setText(aElementUI.getElementLabelText(rContext));
				}
				else
				{
					aElementUI.setHiddenLabelHint(rContext);
				}
			}

			if (!isChildViewElement)
			{
				nHeaderColumn++;
				nColumn +=
					checkLayoutProperties(rDataElement,
										  sStyle,
										  sWidth,
										  sHeight,
										  nExtraColumns);
			}

			if (rDataElement.hasFlag(INITIAL_FOCUS) ||
				getParent() == null && !(bFocusSet || bImmutable))
			{
				bFocusSet = aElementUI.requestFocus();
			}
		}
	}

	/***************************************
	 * Calculates the total number of columns for a panel layout containing the
	 * given data elements.
	 *
	 * @param  rDataElements The data elements to analyze
	 *
	 * @return The new layout
	 */
	protected int calcLayoutColumns(Collection<DataElement<?>> rDataElements)
	{
		int nExtraColumns	   = 0;
		int nRowElementColumns = 0;

		nElementColumns = 0;
		bHasOptions     = false;
		bHasLabels	    = false;

		for (DataElement<?> rDataElement : rDataElements)
		{
			boolean bNewRow = !rDataElement.hasFlag(SAME_ROW);

			if (!bHasOptions && rDataElement.isOptional())
			{
				bHasOptions = true;
				nExtraColumns++;
			}

			if (!bHasLabels &&
				bNewRow &&
				!(rDataElement.hasFlag(HIDE_LABEL) ||
				  rDataElement.hasFlag(HEADER_LABEL)))
			{
				bHasLabels = true;
				nExtraColumns++;
			}

			if (bNewRow)
			{
				nElementColumns = Math.max(nElementColumns, nRowElementColumns);

				nRowElementColumns = 0;
			}

			nRowElementColumns += rDataElement.getIntProperty(COLUMN_SPAN, 1);
		}

		// repeat comparison for the last row
		nElementColumns = Math.max(nElementColumns, nRowElementColumns);

		// always return at least 1 column in the case of no data elements
		return Math.max(nElementColumns + nExtraColumns, 1);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected ContainerBuilder<? extends Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		Layout				eLayout)
	{
		ContainerBuilder<Panel> aContainerBuilder =
			rBuilder.addPanel(rStyleData,
							  new GridLayout(calcLayoutColumns(aDataElements),
											 true));

		return aContainerBuilder;
	}

	/***************************************
	 * Adds certain style names to a cell in the grid layout of this instance.
	 *
	 * @param rStyles sLabelCellStyle
	 */
	private void addCellStyles(String... rStyles)
	{
		GridLayout rLayout    = getDataElementsLayout();
		Container  rContainer = getContainer();

		for (String sStyle : rStyles)
		{
			if (sStyle.length() > 0)
			{
				rLayout.addCellStyle(rContainer, sStyle);
			}
		}
	}

	/***************************************
	 * Adds a new row of data element UIs to the layout.
	 *
	 * @param aElementUI             The first data element UI to add
	 * @param sLabelCellStyle        The style for the grid cell of the element
	 *                               label
	 * @param nPreviousRowLastColumn The last column of the previous row
	 */
	private void addElementRow(DataElementUI<?> aElementUI,
							   String			sLabelCellStyle,
							   int				nPreviousRowLastColumn)
	{
		DataElement<?> rDataElement = aElementUI.getDataElement();

		while (nPreviousRowLastColumn++ < nElementColumns)
		{
			addEmptyCell();
		}

		if (aElementUI != null &&
			!rDataElement.isImmutable() &&
			rDataElement.isOptional())
		{
			aElementUI.addOptionSelector(this);
		}
		else if (bHasOptions)
		{
			addEmptyCell();
		}

		if (aElementUI != null &&
			!(rDataElement.hasFlag(HIDE_LABEL) ||
			  rDataElement.hasFlag(HEADER_LABEL)))
		{
			StyleData aElementLabelStyle =
				addStyles(ELEMENT_LABEL_STYLE, sLabelCellStyle);

			aElementUI.createElementLabel(this, aElementLabelStyle);
			addCellStyles(CSS.gfDataElementLabel(), sLabelCellStyle);
		}
	}

	/***************************************
	 * Adds a cell with no content to a container builder with a grid layout.
	 */
	private void addEmptyCell()
	{
		addLabel(StyleData.DEFAULT, "", null);
		getDataElementsLayout().addCellStyle(getContainer(), CSS.gfEmptyCell());
	}

	/***************************************
	 * Checks if grid layout properties exist for a data element and applies
	 * them if necessary. Returns the column span so that it can be subtracted
	 * from the extra columns in the layout. Also applies a style to the current
	 * cell.
	 *
	 * @param  rDataElement  The data element
	 * @param  sStyle        The style name for the data element
	 * @param  sWidth        The HTML width or NULL for none
	 * @param  sHeight       The HTML height or NULL for none
	 * @param  nExtraColumns The count of extra columns that the element should
	 *                       span (zero for none)
	 *
	 * @return The column span
	 */
	private int checkLayoutProperties(DataElement<?> rDataElement,
									  String		 sStyle,
									  String		 sWidth,
									  String		 sHeight,
									  int			 nExtraColumns)
	{
		Container  rContainer = getContainer();
		GridLayout rLayout    = getDataElementsLayout();
		String     sAddStyle  = rDataElement.getProperty(STYLE, null);
		int		   nRowSpan   = rDataElement.getIntProperty(ROW_SPAN, 1);
		int		   nColSpan   = rDataElement.getIntProperty(COLUMN_SPAN, 1);

		if (nRowSpan > 1)
		{
			rLayout.joinRows(rContainer, nRowSpan);
		}

		if (nColSpan + nExtraColumns > 1)
		{
			rLayout.joinColumns(rContainer, nColSpan + nExtraColumns);
		}

		if (sWidth != null || sHeight != null)
		{
			rLayout.setCellSize(rContainer, sWidth, sHeight);
		}

		if (sAddStyle != null)
		{
			sStyle += " " + sAddStyle;
		}

		rLayout.addCellStyle(rContainer, sStyle.trim());

		return nColSpan;
	}

	/***************************************
	 * Returns the grid layout for the data elements in this panel.
	 *
	 * @return The data elements layout
	 */
	private GridLayout getDataElementsLayout()
	{
		return (GridLayout) getContainer().getLayout();
	}

	/***************************************
	 * Initializes the internal data structures that contain the data elements
	 * of this instance.
	 *
	 * @param  rDataElements The data elements
	 *
	 * @throws IllegalArgumentException If the list of data elements is empty
	 */
	private void initDataElements(Collection<DataElement<?>> rDataElements)
	{
		if (rDataElements == null)
		{
			throw new IllegalArgumentException(getStyleName() +
											   ": No data elements");
		}

		aDataElements = new ArrayList<DataElement<?>>(rDataElements);
	}
}
