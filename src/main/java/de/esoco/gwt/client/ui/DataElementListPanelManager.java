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

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.GroupPanel;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.layout.DockLayout;
import de.esoco.ewt.layout.FillLayout;
import de.esoco.ewt.layout.FlowLayout;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.data.element.DataElementList.LIST_DISPLAY_MODE;

import static de.esoco.lib.property.UserInterfaceProperties.CURRENT_SELECTION;
import static de.esoco.lib.property.UserInterfaceProperties.HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.VERTICAL;
import static de.esoco.lib.property.UserInterfaceProperties.WIDTH;


/********************************************************************
 * A panel manager subclass that displays the children of data element lists as
 * defined by the property {@link DataElementList#LIST_DISPLAY_MODE}. The
 * default mode is {@link ListDisplayMode#TABS TABS}. The groups will be in the
 * same order as the data elements in the {@link DataElementList} received by
 * the constructor. If the list contains a single data element no groups will be
 * created, only a single data element panel manager.
 *
 * @author eso
 */
public class DataElementListPanelManager extends DataElementPanelManager
	implements EWTEventHandler
{
	//~ Instance fields --------------------------------------------------------

	private DataElementList rDataElementList;
	private ListDisplayMode eDisplayMode;

	private GroupPanel aGroupPanel;
	private String     sLabelPrefix;

	private DataElementInteractionHandler<DataElementList> aInteractionHandler;

	private List<DataElementPanelManager> aPanelManagers =
		new ArrayList<DataElementPanelManager>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent          The parent panel manager
	 * @param rDataElementList The data elements to display grouped
	 */
	public DataElementListPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, createPanelStyle(rDataElementList));

		this.rDataElementList = rDataElementList;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Static helper method to create the panel manager's style name.
	 *
	 * @param  rDataElementList The data element list to create the style name
	 *                          for
	 *
	 * @return
	 */
	private static String createPanelStyle(DataElementList rDataElementList)
	{
		String sStyle = CSS.gfDataElementPanel();

		if (rDataElementList.getElementCount() > 1)
		{
			ListDisplayMode eDisplayMode =
				rDataElementList.getProperty(LIST_DISPLAY_MODE,
											 ListDisplayMode.TABS);

			switch (eDisplayMode)
			{
				case GRID:
					sStyle = CSS.gfDataElementPanel();
					break;

				case FLOW:
					sStyle = CSS.gfDataElementFlowPanel();
					break;

				case FILL:
					sStyle = CSS.gfDataElementFillPanel();
					break;

				case DOCK:
					sStyle = CSS.gfDataElementListPanel();
					break;

				case SPLIT:
					sStyle = CSS.gfDataElementSplitPanel();
					break;

				case STACK:
					sStyle = CSS.gfDataElementStackPanel();
					break;

				case TABS:
					sStyle = CSS.gfDataElementTabPanel();
					break;
			}
		}

		return sStyle + " " + rDataElementList.getResourceId();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see DataElementPanelManager#addElementEventListener(EventType, EWTEventHandler)
	 */
	@Override
	public void addElementEventListener(
		EventType		eEventType,
		EWTEventHandler rListener)
	{
		for (DataElement<?> rDataElement : rDataElementList)
		{
			int nIndex = rDataElementList.getElementIndex(rDataElement);

			aPanelManagers.get(nIndex)
						  .addElementEventListener(eEventType, rListener);
		}
	}

	/***************************************
	 * @see DataElementPanelManager#addElementEventListener(DataElement,
	 *      EventType, EWTEventHandler)
	 */
	@Override
	public void addElementEventListener(DataElement<?>  rDataElement,
										EventType		eEventType,
										EWTEventHandler rListener)
	{
		int nIndex = rDataElementList.getElementIndex(rDataElement);

		if (nIndex >= 0)
		{
			DataElementPanelManager rPanelManager = aPanelManagers.get(nIndex);

			if (rPanelManager instanceof DataElementListPanelManager)
			{
				rPanelManager.addElementEventListener(eEventType, rListener);
			}
		}
	}

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
		if (aGroupPanel == null)
		{
			for (DataElementPanelManager rPanelManager : aPanelManagers)
			{
				rPanelManager.collectInput();
			}
		}
		else
		{
			int nSelectedElement = getSelectedElement();

			rDataElementList.setProperty(CURRENT_SELECTION, nSelectedElement);

			if (nSelectedElement >= 0)
			{
				aPanelManagers.get(nSelectedElement).collectInput();
			}
		}
	}

	/***************************************
	 * Overridden to dispose the child panel managers.
	 *
	 * @see de.esoco.gwt.client.ui.PanelManager#dispose()
	 */
	@Override
	public void dispose()
	{
		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			rPanelManager.dispose();
		}
	}

	/***************************************
	 * Sets the enabled.
	 *
	 * @param bEnable The new enabled
	 */
	@Override
	public void enableInteraction(boolean bEnable)
	{
		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			rPanelManager.enableInteraction(bEnable);
		}
	}

	/***************************************
	 * @see DataElementPanelManager#findDataElement(String)
	 */
	@Override
	public DataElement<?> findDataElement(String sName)
	{
		return rDataElementList.findChild(sName);
	}

	/***************************************
	 * Returns a certain element in this manager's list of data elements.
	 *
	 * @param  nIndex The index of the element to return
	 *
	 * @return The data element
	 */
	public final DataElement<?> getDataElement(int nIndex)
	{
		return rDataElementList.getElement(nIndex);
	}

	/***************************************
	 * @see DataElementPanelManager#getDataElements()
	 */
	@Override
	public final Collection<DataElement<?>> getDataElements()
	{
		return Arrays.<DataElement<?>>asList(rDataElementList);
	}

	/***************************************
	 * @see DataElementPanelManager#getDataElementUI(DataElement)
	 */
	@Override
	public final DataElementUI<?> getDataElementUI(DataElement<?> rDataElement)
	{
		DataElementUI<?> rUI = null;

		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			rUI = rPanelManager.getDataElementUI(rDataElement);

			if (rUI != null)
			{
				break;
			}
		}

		return rUI;
	}

	/***************************************
	 * Returns the index of the currently selected group element.
	 *
	 * @return The selection index
	 */
	public int getSelectedElement()
	{
		int nSelection = 0;

		if (aGroupPanel != null)
		{
			nSelection = aGroupPanel.getSelectionIndex();
		}

		return nSelection;
	}

	/***************************************
	 * Returns the subordinate data element panel manager for a certain data
	 * element of this instance.
	 *
	 * @param  rElement The element to return the panel manager for
	 *
	 * @return The matching panel manager or NULL if not found
	 */
	public DataElementPanelManager getSubPanelManager(DataElement<?> rElement)
	{
		int nIndex = rDataElementList.getElementIndex(rElement);

		DataElementPanelManager rSubManager = null;

		if (nIndex >= 0)
		{
			rSubManager = aPanelManagers.get(nIndex);
		}

		return rSubManager;
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
		if (aGroupPanel != null)
		{
			aGroupPanel.setSelection(nElement);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updateDataElements(List<DataElement<?>> rNewDataElements,
								   Map<String, String>  rErrorMessages,
								   boolean				bUpdateUI)
	{
		int nIndex = 0;

		if (rNewDataElements.size() != 1 ||
			!(rNewDataElements.get(0) instanceof DataElementList))
		{
			throw new IllegalArgumentException("DataElementList expected, not " +
											   rNewDataElements);
		}

		getContainer().applyStyle(DataElementUI.applyElementStyle(rDataElementList,
																  getStyle()));

		DataElementList rNewDataElementList =
			(DataElementList) rNewDataElements.get(0);

		if (rNewDataElementList.getElementCount() !=
			rDataElementList.getElementCount())
		{
			throw new IllegalArgumentException("Update has different element count");
		}

		// must be assigned before updating panel manager for correct lookup
		// of data element dependencies
		rDataElementList = rNewDataElementList;

		List<DataElement<?>> rOrderedElements =
			new ArrayList<>(checkReorderElements(rDataElementList).keySet());

		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			DataElement<?> rDataElement = rOrderedElements.get(nIndex);

			rPanelManager.updateFromDataElement(rDataElement,
												rErrorMessages,
												bUpdateUI);

			if (bUpdateUI && aGroupPanel != null)
			{
				String sLabel =
					DataElementUI.getLabelText(getContext(),
											   rDataElement,
											   sLabelPrefix);

				aGroupPanel.setGroupTitle(nIndex, sLabel);
			}

			nIndex++;
		}

		updateSelection();
	}

	/***************************************
	 * @see PanelManager#updatePanel()
	 */
	@Override
	public void updatePanel()
	{
		if (aGroupPanel != null)
		{
			int nSelectedElement = getSelectedElement();

			if (nSelectedElement >= 0)
			{
				DataElementPanelManager rPanelManager =
					aPanelManagers.get(nSelectedElement);

				rPanelManager.updatePanel();
			}
		}
		else
		{
			for (DataElementPanelManager rPanelManager : aPanelManagers)
			{
				rPanelManager.updatePanel();
			}
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		UserInterfaceContext rContext    = getContext();
		int					 nPanelIndex = 0;

		Map<DataElement<?>, StyleData> rDataElementStyles =
			checkReorderElements(rDataElementList);

		for (DataElement<?> rDataElement : rDataElementStyles.keySet())
		{
			DataElementPanelManager aPanelManager =
				createDataElementPanel(rDataElement);

			aPanelManagers.add(aPanelManager);
		}

		// all panels need to be defined before building to allow correct
		// resolving of dependencies between data element UIs
		for (Entry<DataElement<?>, StyleData> rElementStyle :
			 rDataElementStyles.entrySet())
		{
			DataElement<?> rDataElement = rElementStyle.getKey();
			StyleData	   rPanelStyle  = rElementStyle.getValue();

			rPanelStyle =
				DataElementUI.applyElementStyle(rDataElement, rPanelStyle);

			DataElementPanelManager rPanelManager =
				aPanelManagers.get(nPanelIndex++);

			build(rPanelManager, rPanelStyle);

			if (aGroupPanel != null)
			{
				Component rElementComponent =
					rPanelManager.getContentComponent();

				String sLabel =
					DataElementUI.getLabelText(rContext,
											   rDataElement,
											   sLabelPrefix);

				aGroupPanel.addGroup(rElementComponent, sLabel, false);
			}
		}

		setupEventHandling();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ContainerBuilder<Panel> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		ContainerBuilder<? extends Panel> aPanelBuilder = null;

		rStyleData =
			DataElementUI.applyElementStyle(rDataElementList, rStyleData);

		aPanelManagers.clear();

		if (rDataElementList.getElementCount() > 1)
		{
			eDisplayMode =
				rDataElementList.getProperty(LIST_DISPLAY_MODE,
											 ListDisplayMode.TABS);

			assert !((eDisplayMode == ListDisplayMode.DOCK ||
					  eDisplayMode == ListDisplayMode.SPLIT) &&
					 rDataElementList.getElementCount() > 3) : "Element count for PLAIN or SPLIT mode must be <= 3";

			switch (eDisplayMode)
			{
				case FLOW:
					aPanelBuilder =
						rBuilder.addPanel(rStyleData, new FlowLayout());
					break;

				case FILL:
					aPanelBuilder =
						rBuilder.addPanel(rStyleData, new FillLayout());
					break;

				case DOCK:
					aPanelBuilder =
						rBuilder.addPanel(rStyleData,
										  new DockLayout(true, false));
					break;

				case SPLIT:
					aPanelBuilder = rBuilder.addSplitPanel(rStyleData);
					break;

				case STACK:
					sLabelPrefix  = "$grp";
					aPanelBuilder = rBuilder.addStackPanel(rStyleData);
					break;

				case TABS:
					sLabelPrefix  = "$tab";
					aPanelBuilder = rBuilder.addTabPanel(rStyleData);
					break;

				default:
					throw new IllegalStateException("Unsupported DataElementList mode " +
													eDisplayMode);
			}

			Panel rPanel = aPanelBuilder.getContainer();

			if (rPanel instanceof GroupPanel)
			{
				aGroupPanel = (GroupPanel) rPanel;
			}
		}
		else
		{
			aPanelBuilder = rBuilder.addPanel(rStyleData, new FillLayout());
		}

		return (ContainerBuilder<Panel>) aPanelBuilder;
	}

	/***************************************
	 * Initializes the event handling for this instance.
	 */
	protected void setupEventHandling()
	{
		if (aGroupPanel != null)
		{
			@SuppressWarnings("boxing")
			int nSelectedElement =
				rDataElementList.getProperty(CURRENT_SELECTION, 0);

			setSelectedElement(nSelectedElement);
			aGroupPanel.addEventListener(EventType.SELECTION, this);
		}

		aInteractionHandler =
			new DataElementInteractionHandler<>(this, rDataElementList);

		aInteractionHandler.setupEventHandling(getContainer(), false);
	}

	/***************************************
	 * Checks whether the order of a list of data elements needs to be changed
	 * to comply with layout constraints. Also maps the corresponding layout
	 * styles if necessary.
	 *
	 * @param  rDataElementList The list of data elements
	 *
	 * @return An mapping from the data elements (reordered if necessary) to the
	 *         associated layout style data
	 */
	private Map<DataElement<?>, StyleData> checkReorderElements(
		DataElementList rDataElementList)
	{
		boolean bVertical     = rDataElementList.hasFlag(VERTICAL);
		int     nElementCount = rDataElementList.getElementCount();

		Map<DataElement<?>, StyleData> rDataElements =
			new LinkedHashMap<>(nElementCount);

		if (eDisplayMode == ListDisplayMode.DOCK ||
			eDisplayMode == ListDisplayMode.SPLIT)
		{
			// reorder elements because the center element must be added last
			AlignedPosition rCenter = AlignedPosition.CENTER;
			AlignedPosition rFirst  =
				bVertical ? AlignedPosition.TOP : AlignedPosition.LEFT;
			AlignedPosition rLast   =
				bVertical ? AlignedPosition.BOTTOM : AlignedPosition.RIGHT;

			if (nElementCount == 3)
			{
				rDataElements.put(rDataElementList.getElement(0), rFirst);
				rDataElements.put(rDataElementList.getElement(2), rLast);
				rDataElements.put(rDataElementList.getElement(1), rCenter);
			}
			else if (nElementCount == 2)
			{
				if (rDataElementList.getElement(1)
					.hasProperty(bVertical ? HEIGHT : WIDTH))
				{
					rDataElements.put(rDataElementList.getElement(1), rLast);
					rDataElements.put(rDataElementList.getElement(0), rCenter);
				}
				else
				{
					rDataElements.put(rDataElementList.getElement(0), rFirst);
					rDataElements.put(rDataElementList.getElement(1), rCenter);
				}
			}
			else
			{
				rDataElements.put(rDataElementList.getElement(0), rCenter);
			}
		}
		else
		{
			for (DataElement<?> rDataElement : rDataElementList)
			{
				rDataElements.put(rDataElement, StyleData.DEFAULT);
			}
		}

		return rDataElements;
	}

	/***************************************
	 * Creates a new panel for a certain data element and returns the associated
	 * panel manager.
	 *
	 * @param  rDataElement The selection data element for the mail to display
	 *
	 * @return
	 */
	private DataElementPanelManager createDataElementPanel(
		DataElement<?> rDataElement)
	{
		DataElementPanelManager aPanelManager = null;
		List<DataElement<?>>    aElements     = null;

		String sName = rDataElement.getResourceId();

		if (rDataElement instanceof DataElementList)
		{
			DataElementList rElementList = (DataElementList) rDataElement;

			ListDisplayMode eListElementDisplayMode =
				rDataElement.getProperty(LIST_DISPLAY_MODE,
										 ListDisplayMode.GRID);

			if (eListElementDisplayMode == ListDisplayMode.GRID)
			{
				aPanelManager =
					new DataElementGridPanelManager(this, sName, rElementList);
			}
			else
			{
				aPanelManager =
					new DataElementListPanelManager(this, rElementList);
			}
		}
		else if (eDisplayMode != null)
		{
			aPanelManager =
				new SingleDataElementPanelManager(this, rDataElement);
		}
		else
		{
			aElements     = Arrays.<DataElement<?>>asList(rDataElement);
			aPanelManager =
				new DataElementGridPanelManager(this, sName, aElements);
		}

		return aPanelManager;
	}

	/***************************************
	 * Updates the current selection from the data element state.
	 */
	@SuppressWarnings("boxing")
	private void updateSelection()
	{
		int nCurrentSelection = getSelectedElement();

		Integer nNewSelection =
			rDataElementList.getProperty(CURRENT_SELECTION, nCurrentSelection);

		if (nCurrentSelection != nNewSelection)
		{
			setSelectedElement(nNewSelection);
		}
	}
}
