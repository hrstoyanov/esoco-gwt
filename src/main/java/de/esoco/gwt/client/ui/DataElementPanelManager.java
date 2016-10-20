//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2016 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 3.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-3.0
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
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.SelectableButton;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;

import de.esoco.gwt.client.res.EsocoGwtCss;
import de.esoco.gwt.client.res.EsocoGwtResources;

import de.esoco.lib.property.InteractionEventType;
import de.esoco.lib.property.Layout;
import de.esoco.lib.property.SingleSelection;
import de.esoco.lib.property.StateProperties;
import de.esoco.lib.text.TextConvert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.esoco.lib.property.LayoutProperties.LAYOUT;
import static de.esoco.lib.property.StateProperties.SELECTION_DEPENDENCY;
import static de.esoco.lib.property.StateProperties.SELECTION_DEPENDENCY_REVERSE_PREFIX;


/********************************************************************
 * The base class for panel managers that display and handle data elements.
 *
 * @author eso
 */
public abstract class DataElementPanelManager
	extends PanelManager<Container, PanelManager<?, ?>>
{
	//~ Static fields/initializers ---------------------------------------------

	static final EsocoGwtCss CSS = EsocoGwtResources.INSTANCE.css();

	static final StyleData ELEMENT_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElement());

	static final StyleData ELEMENT_LABEL_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElementLabel());

	static final StyleData HEADER_LABEL_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElementHeader());

	private static final Set<Layout> ORDERED_LAYOUTS =
		EnumSet.of(Layout.DOCK, Layout.SPLIT);

	private static final Set<Layout> SWITCH_LAYOUTS =
		EnumSet.of(Layout.TABS, Layout.STACK, Layout.DECK);

	private static final Set<Layout> GRID_LAYOUTS =
		EnumSet.of(Layout.GRID, Layout.FORM, Layout.GROUP);

	//~ Instance fields --------------------------------------------------------

	private DataElementList rDataElementList;
	private Layout		    eLayout;

	private Map<String, DataElementUI<?>> aDataElementUIs;

	private InteractiveInputHandler rInteractiveInputHandler = null;
	private boolean				    bHandlingSelectionEvent  = false;

	private DataElementInteractionHandler<DataElementList> aInteractionHandler;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent          The parent panel manager
	 * @param rDataElementList The data elements to display grouped
	 */
	protected DataElementPanelManager(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		super(rParent, createPanelStyle(rDataElementList));

		this.rDataElementList = rDataElementList;
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Checks whether two lists of data elements contains the same data
	 * elements. The elements must be the same regargind their name and order.
	 *
	 * @param  rList1 The first list of data elements to compare
	 * @param  rList2 The second list of data elements to compare
	 *
	 * @return TRUE if the argument lists differ at least in one data element
	 */
	public static boolean containsSameElements(
		List<DataElement<?>> rList1,
		List<DataElement<?>> rList2)
	{
		int     nCount			 = rList1.size();
		boolean bHasSameElements = (rList2.size() == nCount);

		if (bHasSameElements)
		{
			for (int i = 0; i < nCount; i++)
			{
				bHasSameElements =
					rList1.get(i).getName().equals(rList2.get(i).getName());

				if (!bHasSameElements)
				{
					break;
				}
			}
		}

		return bHasSameElements;
	}

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
	public static DataElementPanelManager newInstance(
		PanelManager<?, ?> rParent,
		DataElementList    rDataElementList)
	{
		DataElementPanelManager aPanelManager = null;

		Layout eLayout = rDataElementList.getProperty(LAYOUT, Layout.TABLE);

		if (eLayout == Layout.TABLE)
		{
			aPanelManager =
				new DataElementTablePanelManager(rParent, rDataElementList);
		}
		else if (eLayout == Layout.INLINE)
		{
			aPanelManager =
				new DataElementInlinePanelManager(rParent, rDataElementList);
		}
		else if (SWITCH_LAYOUTS.contains(eLayout))
		{
			aPanelManager =
				new DataElementSwitchPanelManager(rParent, rDataElementList);
		}
		else if (ORDERED_LAYOUTS.contains(eLayout))
		{
			aPanelManager =
				new DataElementOrderedPanelManager(rParent, rDataElementList);
		}
		else if (GRID_LAYOUTS.contains(eLayout))
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

	/***************************************
	 * Static helper method to create the panel manager's style name.
	 *
	 * @param  rDataElementList The data element list to create the style name
	 *                          for
	 *
	 * @return
	 */
	protected static String createPanelStyle(DataElementList rDataElementList)
	{
		StringBuilder aStyle = new StringBuilder(CSS.gfDataElementPanel());

		{
			Layout eDisplayMode =
				rDataElementList.getProperty(LAYOUT, Layout.TABLE);

			aStyle.append(" gf-DataElement");
			aStyle.append(TextConvert.capitalizedIdentifier(eDisplayMode
															.name()));
			aStyle.append("Panel");
		}

		aStyle.append(' ');
		aStyle.append(rDataElementList.getResourceId());

		return aStyle.toString();
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Registers an event listener for events on all data element components
	 * that are displayed in this panel. The listener will be added by means of
	 * {@link DataElementUI#addEventListener(EventType, EWTEventHandler)}.
	 *
	 * @param rEventType The event type the listener shall be registered for
	 * @param rListener  The event listener to be notified of events
	 */
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
	 * Registers an event listener for events of a certain data element
	 * component that is displayed in this panel. The listener will be added
	 * with {@link DataElementUI#addEventListener(EventType, EWTEventHandler)}.
	 *
	 * @param rDataElement The data element to register the event listener for
	 * @param rEventType   The event type the listener shall be registered for
	 * @param rListener    The event listener to be notified of events
	 */
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
	 * Hierarchically checks all data element UIs for dependencies in the root
	 * data element panel manager.
	 *
	 * @see PanelManager#buildIn(ContainerBuilder, StyleData)
	 */
	@Override
	public void buildIn(ContainerBuilder<?> rBuilder, StyleData rStyle)
	{
		super.buildIn(rBuilder, rStyle);

		// only check selection dependencies from the root after all child data
		// element UIs have been initialized
		if (!(getParent() instanceof DataElementPanelManager))
		{
			checkSelectionDependencies(this, getDataElements());
		}
	}

	/***************************************
	 * Collects the values from the input components into the corresponding data
	 * elements.
	 */
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
	 * @see PanelManager#dispose()
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
	 * Enables or disables interactions through this panel manager's user
	 * interface.
	 *
	 * @param bEnable TRUE to enable interactions, FALSE to disable them
	 */
	public void enableInteraction(boolean bEnable)
	{
		for (DataElementUI<?> rUI : aDataElementUIs.values())
		{
			rUI.enableInteraction(bEnable);
		}
	}

	/***************************************
	 * Searches for a data element with a certain name in this manager's
	 * hierarchy.
	 *
	 * @param  sName The name of the data element to search
	 *
	 * @return The matching data element or NULL if no such element exists
	 */
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
	 * Returns the data elements that are displayed by this instance.
	 *
	 * @return The list of data elements
	 */
	public Collection<DataElement<?>> getDataElements()
	{
		return Arrays.<DataElement<?>>asList(rDataElementList);
	}

	/***************************************
	 * Returns the UI for a certain data element from the hierarchy of this
	 * instance. Implementations must search child panel manager too if they
	 * don't contain the data element themselves.
	 *
	 * @param  rDataElement The data element to return the UI for
	 *
	 * @return The data element UI for the given element or NULL if not found
	 */
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
	 * Returns the root panel manager of this instance's hierarchy. If this
	 * instance is already the root of the hierarchy (i.e. it has no parent) it
	 * will be returned directly.
	 *
	 * @return The root panel manager
	 */
	public DataElementPanelManager getRootDataElementPanelManager()
	{
		PanelManager<?, ?> rParent = getParent();

		return rParent instanceof DataElementPanelManager
			   ? ((DataElementPanelManager) rParent)
			   .getRootDataElementPanelManager() : this;
	}

	/***************************************
	 * Sets the visibility of a data element. The default implementation does
	 * nothing but subclasses can override this method if the need to modify
	 * their state on visibility changes.
	 *
	 * @param rElementUI The UI of the data element
	 * @param bVisible   The visibility of the data element
	 */
	public void setElementVisibility(
		DataElementUI<?> rElementUI,
		boolean			 bVisible)
	{
	}

	/***************************************
	 * Sets the handler of interactive input events for data elements.
	 *
	 * @param rHandler The interactive input handler
	 */
	public final void setInteractiveInputHandler(
		InteractiveInputHandler rHandler)
	{
		rInteractiveInputHandler = rHandler;
	}

	/***************************************
	 * Updates the data elements of this instance with new values and then the
	 * data element UIs.
	 *
	 * @param rNewDataElements The list containing the new data elements
	 * @param rErrorMessages   A mapping from the names of data elements with
	 *                         errors to error messages or NULL to clear all
	 *                         error messages
	 * @param bUpdateUI        TRUE to also update the UI, FALSE to only update
	 *                         the data element references
	 */
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

			if (aInteractionHandler != null)
			{
				aInteractionHandler.updateDataElement(rDataElementList);
			}

			List<DataElement<?>> rOrderedElements =
				new ArrayList<>(prepareChildDataElements(rDataElementList)
								.keySet());

			int nIndex = 0;

			for (DataElementUI<?> rUI : aDataElementUIs.values())
			{
				DataElement<?> rNewElement = rOrderedElements.get(nIndex++);

				rUI.updateDataElement(rNewElement, rErrorMessages, bUpdateUI);
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
	 * A convenience method to update a panel manager from a single data
	 * element. The default implementation wraps the element into a list and
	 * then forwards it to {@link #updateDataElements(List, Map, boolean)}.
	 *
	 * @see #updateDataElements(List, Map, boolean)
	 */
	public void updateFromDataElement(DataElement<?>	  rNewDataElement,
									  Map<String, String> rErrorMessages,
									  boolean			  bUpdateUI)
	{
		List<DataElement<?>> aUpdateList =
			Arrays.<DataElement<?>>asList(rNewDataElement);

		updateDataElements(aUpdateList, rErrorMessages, bUpdateUI);
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
	 * @param  rBuilder   The builder to create the panel with
	 * @param  rStyleData The style to create the panel with
	 * @param  eLayout    The layout of the data element list
	 *
	 * @return A container builder instance for the new panel
	 */
	protected abstract ContainerBuilder<?> createPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData,
		Layout				eLayout);

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
	 * When a {@link StateProperties#SELECTION_DEPENDENCY} property exists in
	 * the given data element the corresponding event handling is initialized
	 * for all concerned data elements.
	 *
	 * @param rRootManager The root manager to search for dependent elements
	 * @param rDataElement The data element to check for dependencies
	 */
	protected void checkSelectionDependency(
		DataElementPanelManager rRootManager,
		DataElement<?>			rDataElement)
	{
		String sDependendElements =
			rDataElement.getProperty(SELECTION_DEPENDENCY, null);

		if (sDependendElements != null)
		{
			String[] sElementNames = sDependendElements.split(",");

			DataElementUI<?> rMainUI = getDataElementUI(rDataElement);

			SelectionDependencyHandler aSelectionHandler =
				new SelectionDependencyHandler(rMainUI);

			for (String sElement : sElementNames)
			{
				boolean bReverseState =
					sElement.startsWith(SELECTION_DEPENDENCY_REVERSE_PREFIX);

				if (bReverseState)
				{
					sElement = sElement.substring(1);
				}

				DataElement<?> rElement =
					rRootManager.findDataElement(sElement);

				if (rElement != null)
				{
					DataElementUI<?> rUI =
						rRootManager.getDataElementUI(rElement);

					if (rUI != null)
					{
						aSelectionHandler.addDependency(rUI, bReverseState);
					}
					else
					{
						assert false : "No UI for " + sElement;
					}
				}
				else
				{
					assert false : "No DataElement with name " + sElement;
				}
			}
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ContainerBuilder<Container> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		ContainerBuilder<? extends Container> aPanelBuilder = null;

		aDataElementUIs =
			new LinkedHashMap<>(rDataElementList.getElementCount());

		rStyleData =
			DataElementUI.applyElementStyle(rDataElementList, rStyleData);

		eLayout = rDataElementList.getProperty(LAYOUT, Layout.TABS);

		aPanelBuilder = createPanel(rBuilder, rStyleData, eLayout);

		return (ContainerBuilder<Container>) aPanelBuilder;
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
	 * Returns the layout of this panel.
	 *
	 * @return The layout
	 */
	protected final Layout getLayout()
	{
		return eLayout;
	}

	/***************************************
	 * Handles the occurrence of an interactive input event for a data element
	 * that is a child of this manager. Will be invoked by the event handler of
	 * the child's data element UI.
	 *
	 * @param rDataElement The data element in which the event occurred
	 * @param eEventType   bActionEvent TRUE for an action event, FALSE for a
	 *                     continuous (selection) event
	 */
	protected void handleInteractiveInput(
		DataElement<?>		 rDataElement,
		InteractionEventType eEventType)
	{
		if (!bHandlingSelectionEvent)
		{
			if (rInteractiveInputHandler != null)
			{
				rInteractiveInputHandler.handleInteractiveInput(rDataElement,
																eEventType);
			}
			else
			{
				PanelManager<?, ?> rParent = getParent();

				if (rParent instanceof DataElementPanelManager)
				{
					((DataElementPanelManager) rParent).handleInteractiveInput(rDataElement,
																			   eEventType);
				}
			}
		}
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
		DataElementInteractionHandler<DataElementList> aEventHandler =
			new DataElementInteractionHandler<>(this, rDataElementList);

		if (aEventHandler.setupEventHandling(getContainer(), false))
		{
			aInteractionHandler = aEventHandler;
		}
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
	 * Checks the given data elements for selection dependencies with other data
	 * elements and initializes the element UIs accordingly.
	 *
	 * @param rRootManager The root manager to search for dependent elements
	 * @param rElements    The data elements to check
	 */
	void checkSelectionDependencies(
		DataElementPanelManager    rRootManager,
		Collection<DataElement<?>> rElements)
	{
		for (DataElement<?> rDataElement : rElements)
		{
			checkSelectionDependency(rRootManager, rDataElement);

			if (rDataElement instanceof DataElementList)
			{
				List<DataElement<?>> rChildElements =
					((DataElementList) rDataElement).getElements();

				checkSelectionDependencies(rRootManager, rChildElements);
			}
		}
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface to handle interactive input events for data elements.
	 *
	 * @author eso
	 */
	public static interface InteractiveInputHandler
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Handles the occurrence of an interactive input event for a certain
		 * data element.
		 *
		 * @param rDataElement The data element that caused the event
		 * @param eEventType   The interaction event that occurred
		 */
		void handleInteractiveInput(
			DataElement<?>		 rDataElement,
			InteractionEventType eEventType);
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An inner class that handles selection events for components that are
	 * referenced by a {@link DataElement#SELECTION_DEPENDENCY} property. The
	 * dependency can either be the mutual exclusion of components that
	 * implement the {@link SingleSelection} interface of the enabling and
	 * disabling of components that are referenced by a button or another
	 * selectable component.
	 *
	 * @author eso
	 */
	private class SelectionDependencyHandler implements EWTEventHandler
	{
		//~ Instance fields ----------------------------------------------------

		private final DataElementUI<?> rMainUI;

		private Map<DataElementUI<?>, Boolean> rUIs =
			new LinkedHashMap<DataElementUI<?>, Boolean>(2);

		//~ Constructors -------------------------------------------------------

		/***************************************
		 * Creates a new instance.
		 *
		 * @param rMainUI The main data element user interface
		 */
		@SuppressWarnings("boxing")
		public SelectionDependencyHandler(DataElementUI<?> rMainUI)
		{
			this.rMainUI = rMainUI;

			Component rComponent = rMainUI.getElementComponent();

			rUIs.put(rMainUI, false);

			if (rComponent instanceof SingleSelection)
			{
				rComponent.addEventListener(EventType.SELECTION, this);
			}
			else
			{
				rComponent.addEventListener(EventType.ACTION, this);
			}
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Adds a component to be handled by this instance.
		 *
		 * @param rUI           The dependent UI
		 * @param bReverseState TRUE to reverse the state of the dependent UI
		 */
		@SuppressWarnings("boxing")
		public void addDependency(DataElementUI<?> rUI, boolean bReverseState)
		{
			Component rMain		 = rMainUI.getElementComponent();
			Component rDependent = rUI.getElementComponent();

			boolean bIsSingleSelection = rDependent instanceof SingleSelection;
			boolean bIsMutualSelection = rMain instanceof SingleSelection;

			rUIs.put(rUI, bReverseState);

			if (bIsSingleSelection && bIsMutualSelection)
			{
				rDependent.addEventListener(EventType.SELECTION, this);
			}
			else
			{
				boolean bEnabled = false;

				if (rMain instanceof SelectableButton)
				{
					bEnabled = ((SelectableButton) rMain).isSelected();
				}
				else if (rMain instanceof SingleSelection)
				{
					bEnabled =
						((SingleSelection) rMain).getSelectionIndex() >= 0;
				}

				rUI.setEnabled(bReverseState ? !bEnabled : bEnabled);
			}
		}

		/***************************************
		 * @see EWTEventHandler#handleEvent(EWTEvent)
		 */
		@Override
		@SuppressWarnings("boxing")
		public void handleEvent(EWTEvent rEvent)
		{
			// prevent re-invocation due to selection change in dependent UIs
			if (!bHandlingSelectionEvent)
			{
				bHandlingSelectionEvent = true;

				for (Entry<DataElementUI<?>, Boolean> rEntry : rUIs.entrySet())
				{
					DataElementUI<?> rTargetUI     = rEntry.getKey();
					boolean			 bReverseState = rEntry.getValue();

					Object rSourceComponent = rEvent.getSource();

					if (rTargetUI.getElementComponent() != rSourceComponent)
					{
						if (rEvent.getType() == EventType.ACTION)
						{
							handleActionEvent(rSourceComponent,
											  rTargetUI,
											  bReverseState);
						}
						else
						{
							handleSelectionEvent(rSourceComponent,
												 rTargetUI,
												 bReverseState);
						}
					}
				}

				bHandlingSelectionEvent = false;
			}
		}

		/***************************************
		 * Performs the dependency changes caused by an action event.
		 *
		 * @param rSourceComponent The component that has been selected
		 * @param rTargetUI        The target data element UI
		 * @param bReverseState    TRUE to reverse the state of the source
		 *                         component in the target component
		 */
		private void handleActionEvent(Object			rSourceComponent,
									   DataElementUI<?> rTargetUI,
									   boolean			bReverseState)
		{
			if (rSourceComponent instanceof SelectableButton)
			{
				setEnabled(rTargetUI,
						   ((SelectableButton) rSourceComponent).isSelected(),
						   bReverseState);
			}
			else
			{
				setEnabled(rTargetUI,
						   !rTargetUI.getElementComponent().isEnabled(),
						   bReverseState);
			}
		}

		/***************************************
		 * Performs the dependency changes caused by a selection event.
		 *
		 * @param rSourceComponent The component that has been selected
		 * @param rTargetUI        The target data element UI
		 * @param bReverseState    TRUE to reverse the state of the source
		 *                         component in the target component
		 */
		private void handleSelectionEvent(Object		   rSourceComponent,
										  DataElementUI<?> rTargetUI,
										  boolean		   bReverseState)
		{
			Component rTargetComponent = rTargetUI.getElementComponent();

			if (rTargetComponent instanceof SingleSelection)
			{
				((SingleSelection) rTargetComponent).setSelection(-1);
			}
			else
			{
				int nSelection =
					((SingleSelection) rSourceComponent).getSelectionIndex();

				boolean bEnabled = nSelection >= 0;

				setEnabled(rTargetUI, bEnabled, bReverseState);
			}
		}

		/***************************************
		 * Sets the enabled state of a dependent component.
		 *
		 * @param rDependentUI  The dependent component
		 * @param bEnabled      bState rSelectedComponent The selected component
		 * @param bReverseState TRUE to reverse the enabled state
		 */
		private void setEnabled(DataElementUI<?> rDependentUI,
								boolean			 bEnabled,
								boolean			 bReverseState)
		{
			rDependentUI.setEnabled(bReverseState ? !bEnabled : bEnabled);
		}
	}
}
