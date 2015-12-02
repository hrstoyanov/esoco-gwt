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

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;

import de.esoco.gwt.client.res.GwtFrameworkResource;
import de.esoco.gwt.shared.StorageService;

import java.util.List;


/********************************************************************
 * A panel manager that handles a panel that can be used to display mail
 * messages that are retrieved from a {@link StorageService}.
 *
 * @author eso
 */
public class MailMessagePanelManager
	extends PanelManager<Panel, PanelManager<?, ?>>
{
	//~ Instance fields --------------------------------------------------------

	private DataElementList rMessage;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent  The parent panel
	 * @param rMessage The mail message to display
	 */
	public MailMessagePanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rMessage)
	{
		super(rParent,
			  GwtFrameworkResource.INSTANCE.css().gfMailMessagePanel());

		this.rMessage = rMessage;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public ContainerBuilder<Panel> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		return rBuilder.addPanel(rStyleData);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		List<DataElement<?>> rMessageFields = rMessage.getElements();

		PanelManager<Panel, PanelManager<?, ?>> aMessagePanelManager =
			new DataElementGridPanelManager(this,
											"MailMessage",
											rMessageFields);

		build(aMessagePanelManager, AlignedPosition.CENTER);
	}
}
