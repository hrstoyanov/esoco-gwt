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
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.GroupPanel;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.esoco.lib.property.UserInterfaceProperties.CURRENT_SELECTION;


/********************************************************************
 * A panel manager for {@link DataElementList} instances that renders the child
 * elements of the list in visual groups (e.g. Tabs).
 *
 * @author eso
 */
public class DataElementGroupPanelManager extends DataElementListPanelManager
	implements EWTEventHandler
{
	//~ Instance fields --------------------------------------------------------

	private GroupPanel aGroupPanel;
	private String     sLabelPrefix;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DataElementGroupPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, rDataElementList);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds an event listener for group selection events. The listener will be
	 * notified of an {@link EventType#SELECTION} event if a new display group
	 * is selected. The source of the event will be the panel used by this panel
	 * manager, not the manager itself. Not all grouping panels may support
	 * listener registrations.
	 *
	 * @param rListener The listener to notify if a group is selected
	 */
	public void addGroupSelectionListener(EWTEventHandler rListener)
	{
		aGroupPanel.addEventListener(EventType.SELECTION, rListener);
	}

	/***************************************
	 * Invokes {@link DataElementPanelManager#collectInput()} on the currently
	 * selected group's panel manager.
	 */
	@Override
	public void collectInput()
	{
		int nSelectedElement = getSelectedElement();

		getDataElementList().setProperty(CURRENT_SELECTION, nSelectedElement);

		if (nSelectedElement >= 0)
		{
			DataElementUI<?> rDataElementUI =
				getDataElementUI(nSelectedElement);

			rDataElementUI.collectInput();
		}
	}

	/***************************************
	 * Returns the index of the currently selected group element.
	 *
	 * @return The selection index
	 */
	public int getSelectedElement()
	{
		return aGroupPanel.getSelectionIndex();
	}

	/***************************************
	 * Handles the selection of a group (tab or stack).
	 *
	 * @see EWTEventHandler#handleEvent(EWTEvent)
	 */
	@Override
	public void handleEvent(EWTEvent rEvent)
	{
		updatePanel();
	}

	/***************************************
	 * Sets the currently selected group.
	 *
	 * @param nElement The index of the group to select
	 */
	public void setSelectedElement(int nElement)
	{
		aGroupPanel.setSelection(nElement);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("boxing")
	public void updateDataElements(List<DataElement<?>> rNewDataElements,
								   Map<String, String>  rErrorMessages,
								   boolean				bUpdateUI)
	{
		super.updateDataElements(rNewDataElements, rErrorMessages, bUpdateUI);

		int nCurrentSelection = getSelectedElement();

		Integer nNewSelection =
			getDataElementList().getProperty(CURRENT_SELECTION,
											 nCurrentSelection);

		if (nCurrentSelection != nNewSelection)
		{
			setSelectedElement(nNewSelection);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updatePanel()
	{
		int nSelectedElement = getSelectedElement();

		if (nSelectedElement >= 0)
		{
			DataElementUI<?> rDataElementUI =
				getDataElementUI(nSelectedElement);

			rDataElementUI.update();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		super.addComponents();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void buildDataElementUI(
		DataElementUI<?> rDataElementUI,
		StyleData		 rStyle)
	{
		super.buildDataElementUI(rDataElementUI, rStyle);

		Component rElementComponent = rDataElementUI.getElementComponent();

		String sLabel =
			DataElementUI.getLabelText(getContext(),
									   rDataElementUI.getDataElement(),
									   sLabelPrefix);

		aGroupPanel.addGroup(rElementComponent, sLabel, false);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected ContainerBuilder<? extends Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		ListDisplayMode		eDisplayMode)
	{
		ContainerBuilder<? extends GroupPanel> aPanelBuilder;

		switch (eDisplayMode)
		{
			case TABS:
				sLabelPrefix  = "$tab";
				aPanelBuilder = rBuilder.addTabPanel(rStyleData);
				break;

			case STACK:
				sLabelPrefix  = "$grp";
				aPanelBuilder = rBuilder.addStackPanel(rStyleData);
				break;

			case DECK:
				sLabelPrefix  = "$dck";
				aPanelBuilder = rBuilder.addDeckPanel(rStyleData);
				break;

			default:
				throw new IllegalStateException("Unsupported DataElementList mode " +
												eDisplayMode);
		}

		aGroupPanel = aPanelBuilder.getContainer();

		return aPanelBuilder;
	}

	/***************************************
	 * Initializes the event handling for this instance.
	 */
	@Override
	protected void setupEventHandling()
	{
		super.setupEventHandling();

		@SuppressWarnings("boxing")
		int nSelectedGroup =
			getDataElementList().getProperty(CURRENT_SELECTION, 0);

		setSelectedElement(nSelectedGroup);
		aGroupPanel.addEventListener(EventType.SELECTION, this);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void updateChildPanelManager(
		DataElementPanelManager rPanelManager,
		DataElement<?>			rNewDataElement,
		Map<String, String>		rErrorMessages,
		int						nPanelIndex,
		boolean					bUpdateUI)
	{
		super.updateChildPanelManager(rPanelManager,
									  rNewDataElement,
									  rErrorMessages,
									  nPanelIndex,
									  bUpdateUI);

		if (bUpdateUI)
		{
			String sLabel =
				DataElementUI.getLabelText(getContext(),
										   rNewDataElement,
										   sLabelPrefix);

			aGroupPanel.setGroupTitle(nPanelIndex, sLabel);
		}
	}

	/***************************************
	 * Returns a value from a collection at a certain position, relative to the
	 * iteration order. The first position is zero.
	 *
	 * @param  nIndex The position index
	 *
	 * @return The corresponding value
	 *
	 * @throws IndexOutOfBoundsException If the index is invalid for the
	 *                                   collection
	 */
	private DataElementUI<?> getDataElementUI(int nIndex)
	{
		assert nIndex >= 0 && nIndex < getDataElementUIs().size();

		Iterator<DataElementUI<?>> rUIs =
			getDataElementUIs().values().iterator();

		DataElementUI<?> rResult = null;

		while (nIndex-- >= 0 && rUIs.hasNext())
		{
			rResult = rUIs.next();
		}

		return rResult;
	}
}
