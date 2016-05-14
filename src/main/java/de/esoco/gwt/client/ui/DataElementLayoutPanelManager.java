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

import de.esoco.data.element.DataElementList;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties.Layout;


/********************************************************************
 * A panel manager for {@link DataElementList} instances that places the child
 * data elements of the data element list in a layout that is defined by the
 * {@link UserInterfaceProperties.Layout} of the data element list.
 *
 * @author eso
 */
public class DataElementLayoutPanelManager extends DataElementPanelManager
{
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
	protected ContainerBuilder<?> createPanel(ContainerBuilder<?> rBuilder,
											  StyleData			  rStyleData,
											  Layout			  eLayout)
	{
		return rBuilder.addPanel(rStyleData, eLayout);
	}
}
