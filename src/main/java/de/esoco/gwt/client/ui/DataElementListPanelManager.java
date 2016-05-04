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
import de.esoco.data.element.DataElementList.Layout;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.esoco.data.element.DataElementList.LAYOUT;


/********************************************************************
 * A panel manager subclass that displays the children of data element lists as
 * defined by the property {@link DataElementList#LAYOUT}. The
 * default mode is {@link Layout#TABS TABS}. The groups will be in the
 * same order as the data elements in the {@link DataElementList} received by
 * the constructor. If the list contains a single data element no groups will be
 * created, only a single data element panel manager.
 *
 * @author eso
 */
public abstract class DataElementListPanelManager
	extends DataElementPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Set<Layout> ORDERED_DISPLAY_MODES =
		EnumSet.of(Layout.DOCK, Layout.SPLIT);

	private static final Set<Layout> GROUP_DISPLAY_MODES =
		EnumSet.of(Layout.TABS,
				   Layout.STACK,
				   Layout.DECK);

	//~ Instance fields --------------------------------------------------------

	private DataElementList rDataElementList;
	private Layout eDisplayMode;

	private Map<String, DataElementUI<?>> aDataElementUIs;

	private DataElementInteractionHandler<DataElementList> aInteractionHandler;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent          The parent panel manager
	 * @param rDataElementList The data elements to display grouped
	 */
	protected DataElementListPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, createPanelStyle(rDataElementList));

		this.rDataElementList = rDataElementList;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Factory method that creates a new subclass instance based on the {@link
	 * Layout} of the data element list argument.
	 *
	 * @param  rParent          The parent panel manager
	 * @param  rDataElementList The list of data elements to be handled by the
	 *                          new panel manager
	 *
	 * @return A new data element panel manager instance
	 */
	public static DataElementListPanelManager newInstance(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		DataElementListPanelManager aPanelManager = null;
		Layout			    eDisplayMode  =
			rDataElementList.getProperty(LAYOUT,
										 Layout.TABLE);

		if (GROUP_DISPLAY_MODES.contains(eDisplayMode))
		{
			aPanelManager =
				new DataElementGroupPanelManager(rParent, rDataElementList);
		}
		else if (ORDERED_DISPLAY_MODES.contains(eDisplayMode))
		{
			aPanelManager =
				new DataElementOrderedPanelManager(rParent, rDataElementList);
		}
		else if (eDisplayMode == Layout.TABLE)
		{
			aPanelManager =
				new DataElementGridPanelManager(rParent, rDataElementList);
		}
		else
		{
			aPanelManager =
				new DataElementLayoutPanelManager(rParent, rDataElementList);
		}

		return aPanelManager;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void addElementEventListener(
		EventType		rEventType,
		EWTEventHandler rListener)
	{
		for (DataElementUI<?> rDataElementUI : aDataElementUIs.values())
		{
			rDataElementUI.addEventListener(rEventType, rListener);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void addElementEventListener(DataElement<?>  rDataElement,
										EventType		rEventType,
										EWTEventHandler rListener)
	{
		DataElementUI<?> rDataElementUI =
			aDataElementUIs.get(rDataElement.getName());

		if (rDataElementUI != null)
		{
			rDataElementUI.addEventListener(rEventType, rListener);
		}
		else
		{
			throw new IllegalArgumentException("Unknown data element: " +
											   rDataElement);
		}
	}

	/***************************************
	 * @see DataElementPanelManager#collectInput()
	 */
	@Override
	public void collectInput()
	{
		for (DataElementUI<?> rUI : aDataElementUIs.values())
		{
			if (rUI != null)
			{
				rUI.collectInput();
			}
		}
	}

	/***************************************
	 * Overridden to dispose the existing data element UIs.
	 *
	 * @see DataElementPanelManager#dispose()
	 */
	@Override
	public void dispose()
	{
		for (DataElementUI<?> rUI : aDataElementUIs.values())
		{
			rUI.dispose();
		}
	}

	/***************************************
	 * @see DataElementPanelManager#enableInteraction(boolean)
	 */
	@Override
	public void enableInteraction(boolean bEnable)
	{
		for (DataElementUI<?> rUI : aDataElementUIs.values())
		{
			rUI.enableInteraction(bEnable);
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
	 * Returns the dataElementList value.
	 *
	 * @return The dataElementList value
	 */
	public final DataElementList getDataElementList()
	{
		return rDataElementList;
	}

	/***************************************
	 * @see DataElementPanelManager#getDataElements()
	 */
	@Override
	public Collection<DataElement<?>> getDataElements()
	{
		return Arrays.<DataElement<?>>asList(rDataElementList);
	}

	/***************************************
	 * @see DataElementPanelManager#getDataElementUI(DataElement)
	 */
	@Override
	public final DataElementUI<?> getDataElementUI(DataElement<?> rDataElement)
	{
		DataElementUI<?> rDataElementUI =
			aDataElementUIs.get(rDataElement.getName());

		if (rDataElementUI == null)
		{
			for (DataElementUI<?> rUI : aDataElementUIs.values())
			{
				if (rUI instanceof DataElementListUI)
				{
					rDataElementUI =
						((DataElementListUI) rUI).getPanelManager()
												 .getDataElementUI(rDataElement);
				}

				if (rDataElementUI != null)
				{
					break;
				}
			}
		}

		return rDataElementUI;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updateDataElements(List<DataElement<?>> rNewDataElements,
								   Map<String, String>  rErrorMessages,
								   boolean				bUpdateUI)
	{
		if (rNewDataElements.size() != 1 ||
			!(rNewDataElements.get(0) instanceof DataElementList))
		{
			throw new IllegalArgumentException("DataElementList expected, not " +
											   rNewDataElements);
		}

		getContainer().applyStyle(DataElementUI.applyElementStyle(rDataElementList,
																  getBaseStyle()));

		DataElementList rNewDataElementList =
			(DataElementList) rNewDataElements.get(0);

		if (rNewDataElementList.getName().equals(rDataElementList.getName()) &&
			containsSameElements(rNewDataElementList.getElements(),
								 rDataElementList.getElements()))
		{
			// must be assigned before updating panel manager for correct lookup
			// of data element dependencies
			rDataElementList = rNewDataElementList;

			List<DataElement<?>> rOrderedElements =
				new ArrayList<>(prepareChildDataElements(rDataElementList)
								.keySet());

			Iterator<DataElementUI<?>> rUIs =
				aDataElementUIs.values().iterator();

			for (DataElement<?> rNewElement : rOrderedElements)
			{
				rUIs.next()
					.updateDataElement(rNewElement, rErrorMessages, bUpdateUI);
			}
		}
		else
		{
			rDataElementList = rNewDataElementList;
			aDataElementUIs.clear();
			rebuild();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updatePanel()
	{
		for (DataElementUI<?> rElementUI : aDataElementUIs.values())
		{
			rElementUI.update();
		}
	}

	/***************************************
	 * Must be implemented by subclasses to create the panel in which the data
	 * element user interfaces are placed.
	 *
	 * @param  rBuilder     The builder to create the panel with
	 * @param  rStyleData   The style to create the panel with
	 * @param  eDisplayMode The display mode of the data element list
	 *
	 * @return A container builder instance for the new panel
	 */
	protected abstract ContainerBuilder<? extends Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		Layout		eDisplayMode);

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		Map<DataElement<?>, StyleData> rDataElementStyles =
			prepareChildDataElements(rDataElementList);

		for (Entry<DataElement<?>, StyleData> rElementStyle :
			 rDataElementStyles.entrySet())
		{
			DataElement<?> rDataElement = rElementStyle.getKey();
			StyleData	   rStyle	    = rElementStyle.getValue();

			DataElementUI<?> aDataElementUI =
				DataElementUIFactory.create(this, rDataElement);

			if (!(rDataElement instanceof DataElementList))
			{
				String sElementStyle = aDataElementUI.getElementStyleName();

				if (rDataElement.isImmutable())
				{
					sElementStyle = CSS.readonly() + " " + sElementStyle;
				}

				rStyle = addStyles(rStyle, CSS.gfDataElement(), sElementStyle);
			}

			buildDataElementUI(aDataElementUI, rStyle);
			aDataElementUIs.put(rDataElement.getName(), aDataElementUI);
		}

		setupEventHandling();
	}

	/***************************************
	 * Builds the user interface for a data element in this container.
	 *
	 * @param aDataElementUI The element UI to build
	 * @param rStyle         The style for the data element UI
	 */
	protected void buildDataElementUI(
		DataElementUI<?> aDataElementUI,
		StyleData		 rStyle)
	{
		aDataElementUI.buildUserInterface(this, rStyle);
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

		aDataElementUIs =
			new LinkedHashMap<>(rDataElementList.getElementCount());

		rStyleData =
			DataElementUI.applyElementStyle(rDataElementList, rStyleData);

		eDisplayMode =
			rDataElementList.getProperty(LAYOUT,
										 Layout.TABS);

		aPanelBuilder = createPanel(rBuilder, rStyleData, eDisplayMode);

		return (ContainerBuilder<Panel>) aPanelBuilder;
	}

	/***************************************
	 * Returns the {@link DataElementUI} instances of this instance. The order
	 * in the returned map corresponds to the order in which the UIs are
	 * displayed.
	 *
	 * @return A ordered mapping from data element names to data element UIs
	 */
	protected final Map<String, DataElementUI<?>> getDataElementUIs()
	{
		return aDataElementUIs;
	}

	/***************************************
	 * Returns the display mode of this panel.
	 *
	 * @return The display mode
	 */
	protected final Layout getDisplayMode()
	{
		return eDisplayMode;
	}

	/***************************************
	 * Prepares the child data elements that need to be displayed in this
	 * instance.
	 *
	 * @param  rDataElementList The list of child data elements
	 *
	 * @return A mapping from child data elements to the corresponding styles
	 */
	protected Map<DataElement<?>, StyleData> prepareChildDataElements(
		DataElementList rDataElementList)
	{
		Map<DataElement<?>, StyleData> rDataElementStyles =
			new LinkedHashMap<>();

		for (DataElement<?> rDataElement : rDataElementList)
		{
			rDataElementStyles.put(rDataElement, StyleData.DEFAULT);
		}

		return rDataElementStyles;
	}

	/***************************************
	 * Initializes the event handling for this instance.
	 */
	protected void setupEventHandling()
	{
		aInteractionHandler =
			new DataElementInteractionHandler<>(this, rDataElementList);

		aInteractionHandler.setupEventHandling(getContainer(), false);
	}

	/***************************************
	 * Updates a child panel manager with a new data element.
	 *
	 * @param rPanelManager   The panel manager to update
	 * @param rNewDataElement The new data element
	 * @param rErrorMessages  The optional error messages to be applied
	 * @param nPanelIndex     The index of the current panel
	 * @param bUpdateUI       TRUE if the UI needs to be updated
	 */
	protected void updateChildPanelManager(
		DataElementPanelManager rPanelManager,
		DataElement<?>			rNewDataElement,
		Map<String, String>		rErrorMessages,
		int						nPanelIndex,
		boolean					bUpdateUI)
	{
		rPanelManager.updateFromDataElement(rNewDataElement,
											rErrorMessages,
											bUpdateUI);
	}
}
