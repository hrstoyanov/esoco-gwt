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
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.AlignedPosition;
import de.esoco.ewt.style.StyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.esoco.data.element.DataElementList.LIST_DISPLAY_MODE;

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
public abstract class DataElementListPanelManager
	extends DataElementPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	private static final Set<ListDisplayMode> ORDERED_DISPLAY_MODES =
		EnumSet.of(ListDisplayMode.DOCK, ListDisplayMode.SPLIT);

	private static final Set<ListDisplayMode> GROUP_DISPLAY_MODES =
		EnumSet.of(ListDisplayMode.TABS,
				   ListDisplayMode.STACK,
				   ListDisplayMode.DECK);

	//~ Instance fields --------------------------------------------------------

	private DataElementList rDataElementList;
	private ListDisplayMode eDisplayMode;

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
	 * ListDisplayMode} of the data element list argument.
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
		ListDisplayMode			    eDisplayMode  =
			rDataElementList.getProperty(LIST_DISPLAY_MODE,
										 ListDisplayMode.TABS);

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
		else
		{
			aPanelManager =
				new DataElementLayoutPanelManager(rParent, rDataElementList);
		}

		return aPanelManager;
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
		int nElementCount = rDataElementList.getElementCount();

		for (int i = 0; i < nElementCount; i++)
		{
			aPanelManagers.get(i)
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
	 * Invokes {@link DataElementPanelManager#collectInput()} on the currently
	 * selected group's panel manager.
	 */
	@Override
	public void collectInput()
	{
		for (DataElementPanelManager rPanelManager : getPanelManagers())
		{
			rPanelManager.collectInput();
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
	 * Returns the subordinate data element panel manager for a certain data
	 * element of this instance.
	 *
	 * @param  rElement The element to return the panel manager for
	 *
	 * @return The matching panel manager or NULL if not found
	 */
	public DataElementPanelManager getChildPanelManager(DataElement<?> rElement)
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
																  getBaseStyle()));

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
			new ArrayList<>(prepareChildDataElements().keySet());

		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			DataElement<?> rDataElement = rOrderedElements.get(nIndex);

			updateChildPanelManager(rPanelManager,
									rDataElement,
									rErrorMessages,
									nIndex,
									bUpdateUI);

			nIndex++;
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updatePanel()
	{
		for (DataElementPanelManager rPanelManager : aPanelManagers)
		{
			rPanelManager.updatePanel();
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		int nPanelIndex = 0;

		Map<DataElement<?>, StyleData> rDataElementStyles =
			prepareChildDataElements();

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

			DataElementPanelManager rPanelManager =
				aPanelManagers.get(nPanelIndex++);

			buildPanel(rPanelManager, rDataElement, rPanelStyle);
		}

		setupEventHandling();
	}

	/***************************************
	 * Builds a certain panel manager in this instance.
	 *
	 * @param rPanelManager The panel manager to build
	 * @param rDataElement  The associated data element
	 * @param rPanelStyle   The panel style
	 */
	protected void buildPanel(DataElementPanelManager rPanelManager,
							  DataElement<?>		  rDataElement,
							  StyleData				  rPanelStyle)
	{
		if (rPanelManager instanceof SingleDataElementManager)
		{
			rPanelStyle = addStyles(rPanelStyle, CSS.gfDataElement());
		}

		rPanelStyle =
			DataElementUI.applyElementStyle(rDataElement, rPanelStyle);

		build(rPanelManager, rPanelStyle);
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

//		if (rDataElementList.getElementCount() > 1)
		{
			eDisplayMode =
				rDataElementList.getProperty(LIST_DISPLAY_MODE,
											 ListDisplayMode.TABS);

			assert !((eDisplayMode == ListDisplayMode.DOCK ||
					  eDisplayMode == ListDisplayMode.SPLIT) &&
					 rDataElementList.getElementCount() > 3) : "Element count for PLAIN or SPLIT mode must be <= 3";

			aPanelBuilder = createPanel(rBuilder, rStyleData, eDisplayMode);
		}
//		else
//		{
//			aPanelBuilder = rBuilder.addPanel(rStyleData, new FillLayout());
//		}

		return (ContainerBuilder<Panel>) aPanelBuilder;
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
	protected ContainerBuilder<? extends Panel> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		ListDisplayMode		eDisplayMode)
	{
		return null;
	}

	/***************************************
	 * Returns the panel managers of this instance.
	 *
	 * @return The panel managers
	 */
	protected final List<DataElementPanelManager> getPanelManagers()
	{
		return aPanelManagers;
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

	/***************************************
	 * Checks whether the order of a list of data elements needs to be changed
	 * to comply with layout constraints. Also creates the corresponding layout
	 * styles and puts them in the argument map.
	 *
	 * @param  rDataElementList The list of data elements
	 * @param  rElementStyles   The map to store the data element styles in
	 *
	 * @return An mapping from the data elements (reordered if necessary) to the
	 *         associated layout style data
	 */
	private Collection<DataElement<?>> checkReorderElements(
		DataElementList				   rDataElementList,
		Map<DataElement<?>, StyleData> rElementStyles)
	{
		boolean bVertical     = rDataElementList.hasFlag(VERTICAL);
		int     nElementCount = rDataElementList.getElementCount();

		if (rElementStyles == null)
		{
			rElementStyles = new LinkedHashMap<>(nElementCount);
		}

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

		return rElementStyles.keySet();
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

		if (rDataElement instanceof DataElementList)
		{
			DataElementList rElementList = (DataElementList) rDataElement;

			ListDisplayMode eListElementDisplayMode =
				rDataElement.getProperty(LIST_DISPLAY_MODE,
										 ListDisplayMode.GRID);

			if (eListElementDisplayMode == ListDisplayMode.GRID)
			{
				aPanelManager =
					new DataElementGridPanelManager(this, rElementList);
			}
			else
			{
				aPanelManager = newInstance(this, rElementList);
			}
		}
		else //if (eDisplayMode != null)
		{
			aPanelManager = new SingleDataElementManager(this, rDataElement);
		}
//		else
//		{
//			aElements	  = Arrays.<DataElement<?>>asList(rDataElement);
//			aPanelManager =
//				new DataElementGridPanelManager(this, sName, aElements);
//		}

		return aPanelManager;
	}

	/***************************************
	 * Prepares the child data elements that need to be displayed in this
	 * instance.
	 *
	 * @return A mapping from child data elements to the corresponding styles
	 */
	private Map<DataElement<?>, StyleData> prepareChildDataElements()
	{
		Map<DataElement<?>, StyleData> rDataElementStyles =
			new LinkedHashMap<>();

		if (ORDERED_DISPLAY_MODES.contains(eDisplayMode))
		{
			checkReorderElements(rDataElementList, rDataElementStyles);
		}

//		else if (LAYOUT_DISPLAY_MODES.contains(eDisplayMode) &&
//				 rDataElementList.getElementCount() > 1 &&
//				 !(rDataElementList instanceof DataElementRow))
//		{
//			DataElementList aLayoutRow = null;
//
//			String sRowName = rDataElementList.getResourceId() + "Row";
//
//			for (DataElement<?> rDataElement : rDataElementList)
//			{
//				boolean bNewRow = !rDataElement.hasFlag(SAME_ROW);
//
//				if (aLayoutRow == null || bNewRow)
//				{
//					aLayoutRow = new DataElementRow("DataElementRow");
//					rDataElementStyles.put(aLayoutRow, StyleData.DEFAULT);
//				}
//
//				aLayoutRow.addElement(rDataElement);
//			}
//		}
		else
		{
			for (DataElement<?> rDataElement : rDataElementList)
			{
				rDataElementStyles.put(rDataElement, StyleData.DEFAULT);
			}
		}

		return rDataElementStyles;
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An internal data element list subclass that serves as a container for the
	 * data elements in a row of a UI layout.
	 *
	 * @author eso
	 */
	private static class DataElementRow extends DataElementList
	{
		//~ Static fields/initializers -----------------------------------------

		private static final long serialVersionUID = 1L;

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param sName The instance name
		 */
		public DataElementRow(String sName)
		{
			super(sName, null);

			setProperty(LIST_DISPLAY_MODE, ListDisplayMode.FLOW);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Overriden to prevent an update of the parent of the child data
		 * element.
		 *
		 * @see DataElementList#addElement(int, DataElement)
		 */
		@Override
		public void addElement(int nIndex, DataElement<?> rElement)
		{
			getList().add(nIndex, rElement);
		}
	}
}
