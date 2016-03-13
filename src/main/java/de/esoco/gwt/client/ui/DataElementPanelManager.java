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
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.component.SelectableButton;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;

import de.esoco.gwt.client.res.EsocoGwtCss;
import de.esoco.gwt.client.res.EsocoGwtResources;

import de.esoco.lib.property.SingleSelection;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.esoco.lib.property.UserInterfaceProperties.SELECTION_DEPENDENCY;
import static de.esoco.lib.property.UserInterfaceProperties.SELECTION_DEPENDENCY_REVERSE_PREFIX;


/********************************************************************
 * The base class for panel managers that display and handle data elements.
 *
 * @author eso
 */
public abstract class DataElementPanelManager
	extends PanelManager<Panel, PanelManager<?, ?>>
{
	//~ Static fields/initializers ---------------------------------------------

	static final EsocoGwtCss CSS = EsocoGwtResources.INSTANCE.css();

	static final StyleData ELEMENT_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElement());

	static final StyleData LABEL_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElementLabel());

	static final StyleData HEADER_LABEL_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElementHeader());

	//~ Instance fields --------------------------------------------------------

	private InteractiveInputHandler rInteractiveInputHandler = null;
	private boolean				    bHandlingSelectionEvent  = false;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rParent     The parent panel manager
	 * @param sPanelStyle The panel style
	 */
	public DataElementPanelManager(
		PanelManager<?, ?> rParent,
		String			   sPanelStyle)
	{
		super(rParent, sPanelStyle);
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

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Registers an event listener for events on all data element components
	 * that are displayed in this panel. The listener will be added by means of
	 * {@link DataElementUI#addEventListener(EventType, EWTEventHandler)}.
	 *
	 * @param rEventType The event type the listener shall be registered for
	 * @param rListener  The event listener to be notified of events
	 */
	public abstract void addElementEventListener(
		EventType		rEventType,
		EWTEventHandler rListener);

	/***************************************
	 * Registers an event listener for events of a certain data element
	 * component that is displayed in this panel. The listener will be added
	 * with {@link DataElementUI#addEventListener(EventType, EWTEventHandler)}.
	 *
	 * @param rDataElement The data element to register the event listener for
	 * @param rEventType   The event type the listener shall be registered for
	 * @param rListener    The event listener to be notified of events
	 */
	public abstract void addElementEventListener(DataElement<?>  rDataElement,
												 EventType		 rEventType,
												 EWTEventHandler rListener);

	/***************************************
	 * Must be implemented by subclasses to collect the values from the input
	 * components into the corresponding data elements.
	 */
	public abstract void collectInput();

	/***************************************
	 * Enables or disables interactions through this panel manager's user
	 * interface.
	 *
	 * @param bEnable TRUE to enable interactions, FALSE to disable them
	 */
	public abstract void enableInteraction(boolean bEnable);

	/***************************************
	 * Must be implemented by subclasses to search for a data element with a
	 * certain name in this manager's hierarchy.
	 *
	 * @param  sName The name of the data element to search
	 *
	 * @return The matching data element or NULL if no such element exists
	 */
	public abstract DataElement<?> findDataElement(String sName);

	/***************************************
	 * Returns the data elements that are displayed by this instance.
	 *
	 * @return The list of data elements
	 */
	public abstract Collection<DataElement<?>> getDataElements();

	/***************************************
	 * Returns the UI for a certain data element from the hierarchy of this
	 * instance. Implementations must search child panel manager too if they
	 * don't contain the data element themselves.
	 *
	 * @param  rDataElement The data element to return the UI for
	 *
	 * @return The data element UI for the given element or NULL if not found
	 */
	public abstract DataElementUI<?> getDataElementUI(
		DataElement<?> rDataElement);

	/***************************************
	 * Common method to update the data elements of this instance with new
	 * values and to update the data element UIs afterwards.
	 *
	 * @param rNewDataElements The list containing the new data elements
	 * @param rErrorMessages   A mapping from the names of data elements with
	 *                         errors to error messages or NULL to clear all
	 *                         error messages
	 * @param bUpdateUI        TRUE to also update the UI, FALSE to only update
	 *                         the data element references
	 */
	public abstract void updateDataElements(
		List<DataElement<?>> rNewDataElements,
		Map<String, String>  rErrorMessages,
		boolean				 bUpdateUI);

	/***************************************
	 * Overridden to hierarchically check all data element UIs for dependencies
	 * in the root data element panel manager.
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
	 * Returns the component that represents the content of this instance. The
	 * default implementation returns the same as {@link #getPanel()} but
	 * subclasses that redefine the content creation can override this method to
	 * return a different component.
	 *
	 * @return The component of the manager content
	 */
	public Component getContentComponent()
	{
		return getPanel();
	}

	/***************************************
	 * Returns the interactive input handler of this instance.
	 *
	 * @return The interactive input handler
	 */
	public final InteractiveInputHandler getInteractiveInputHandler()
	{
		return rInteractiveInputHandler;
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
	 * When a {@link DataElement#SELECTION_DEPENDENCY} property exists in the
	 * given data element the corresponding event handling is initialized for
	 * all concerned data elements.
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
	 * Handles the occurrence of an interactive input event for a data element
	 * that is a child of this manager. Will be invoked by the event handler of
	 * the child's data element UI.
	 *
	 * @param rDataElement The data element in which the event occurred
	 * @param bActionEvent TRUE for an action event, FALSE for a continuous
	 *                     (selection) event
	 */
	protected void handleInteractiveInput(
		DataElement<?> rDataElement,
		boolean		   bActionEvent)
	{
		if (!bHandlingSelectionEvent)
		{
			if (rInteractiveInputHandler != null)
			{
				rInteractiveInputHandler.handleInteractiveInput(rDataElement,
																bActionEvent);
			}
			else
			{
				PanelManager<?, ?> rParent = getParent();

				if (rParent instanceof DataElementPanelManager)
				{
					((DataElementPanelManager) rParent).handleInteractiveInput(rDataElement,
																			   bActionEvent);
				}
			}
		}
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
		 * @param rDataElemen  The data element that caused the event
		 * @param bActionEvent TRUE if the event was an action event
		 */
		void handleInteractiveInput(
			DataElement<?> rDataElemen,
			boolean		   bActionEvent);
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
