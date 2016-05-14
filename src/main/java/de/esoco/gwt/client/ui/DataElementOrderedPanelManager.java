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
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.layout.DockLayout;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties.Layout;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.esoco.lib.property.UserInterfaceProperties.HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.VERTICAL;
import static de.esoco.lib.property.UserInterfaceProperties.WIDTH;


/********************************************************************
 * A panel manager for {@link DataElementList} instances that places the child
 * data elements of the data element list in a layout that is defined by the
 * {@link UserInterfaceProperties.Layout} of the data element list.
 *
 * @author eso
 */
public class DataElementOrderedPanelManager extends DataElementPanelManager
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DataElementOrderedPanelManager(
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
	protected ContainerBuilder<? extends Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		Layout				eLayout)
	{
		ContainerBuilder<? extends Panel> aPanelBuilder;

		switch (eLayout)
		{
			case DOCK:
				assert getDataElementList().getElementCount() <= 3 : "Element count for DOCK layout mode must be <= 3";
				aPanelBuilder =
					rBuilder.addPanel(rStyleData, new DockLayout(true, false));
				break;

			case SPLIT:
				aPanelBuilder = rBuilder.addSplitPanel(rStyleData);
				break;

			default:
				throw new IllegalStateException("Unsupported layout " +
												eLayout);
		}

		return aPanelBuilder;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected Map<DataElement<?>, StyleData> prepareChildDataElements(
		DataElementList rDataElementList)
	{
		Map<DataElement<?>, StyleData> rElementStyles = new LinkedHashMap<>();

		boolean bVertical     = rDataElementList.hasFlag(VERTICAL);
		int     nElementCount = rDataElementList.getElementCount();

		// reorder elements because the center element must be added last
		AlignedPosition rCenter = AlignedPosition.CENTER;
		AlignedPosition rFirst  =
			bVertical ? AlignedPosition.TOP : AlignedPosition.LEFT;
		AlignedPosition rLast   =
			bVertical ? AlignedPosition.BOTTOM : AlignedPosition.RIGHT;

		if (nElementCount == 3)
		{
			rElementStyles.put(rDataElementList.getElement(0), rFirst);
			rElementStyles.put(rDataElementList.getElement(2), rLast);
			rElementStyles.put(rDataElementList.getElement(1), rCenter);
		}
		else if (nElementCount == 2)
		{
			if (rDataElementList.getElement(1)
				.hasProperty(bVertical ? HEIGHT : WIDTH))
			{
				rElementStyles.put(rDataElementList.getElement(1), rLast);
				rElementStyles.put(rDataElementList.getElement(0), rCenter);
			}
			else
			{
				rElementStyles.put(rDataElementList.getElement(0), rFirst);
				rElementStyles.put(rDataElementList.getElement(1), rCenter);
			}
		}
		else
		{
			rElementStyles.put(rDataElementList.getElement(0), rCenter);
		}

		return rElementStyles;
	}
}
