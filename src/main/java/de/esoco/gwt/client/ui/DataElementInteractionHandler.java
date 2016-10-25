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

import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.TextControl;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;

import de.esoco.lib.property.InteractionEventType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Timer;

import static de.esoco.lib.property.StateProperties.FOCUSED;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_DATA;
import static de.esoco.lib.property.StateProperties.INTERACTION_EVENT_TYPES;


/********************************************************************
 * A class that handles the interaction events for a certain {@link
 * DataElement}.
 *
 * @author eso
 */
public class DataElementInteractionHandler<D extends DataElement<?>>
	implements EWTEventHandler
{
	//~ Instance fields --------------------------------------------------------

	private DataElementPanelManager   rPanelManager;
	private D						  rDataElement;
	private Set<InteractionEventType> rEventTypes;

	private Timer aInputEventTimer;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rPanelManager The panel manager the data element belongs to
	 * @param rDataElement  The data element to handle events for
	 */
	public DataElementInteractionHandler(
		DataElementPanelManager rPanelManager,
		D						rDataElement)
	{
		this.rPanelManager = rPanelManager;
		this.rDataElement  = rDataElement;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns the data element.
	 *
	 * @return The data element
	 */
	public final D getDataElement()
	{
		return rDataElement;
	}

	/***************************************
	 * Returns the panel manager.
	 *
	 * @return The panel manager
	 */
	public final DataElementPanelManager getPanelManager()
	{
		return rPanelManager;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(final EWTEvent rEvent)
	{
		if (aInputEventTimer != null)
		{
			aInputEventTimer.cancel();
		}

		aInputEventTimer =
			new Timer()
			{
				@Override
				public void run()
				{
					processEvent(rEvent);
				}
			};

		boolean bLongDelay =
			(rEventTypes.contains(InteractionEventType.UPDATE) &&
			 rEvent.getType() == EventType.KEY_RELEASED);

		aInputEventTimer.schedule(bLongDelay ? 750 : 50);
	}

	/***************************************
	 * Initializes the handling of interactive input events for a certain
	 * component if necessary.
	 *
	 * @param  rComponent           The component to setup the input handling
	 *                              for
	 * @param  bOnContainerChildren TRUE to setup the input handling for the
	 *                              children if the component is a container
	 *
	 * @return TRUE if the event handling has been initialized, FALSE if no
	 *         event types have been registered and no event handling is
	 *         necessary
	 */
	public boolean setupEventHandling(
		Component rComponent,
		boolean   bOnContainerChildren)
	{
		rEventTypes =
			rDataElement.getProperty(INTERACTION_EVENT_TYPES,
									 Collections
									 .<InteractionEventType>emptySet());

		boolean bHasEventHandling = !rEventTypes.isEmpty();

		if (bHasEventHandling)
		{
			if (bOnContainerChildren && rComponent instanceof Container)
			{
				List<Component> rComponents =
					((Container) rComponent).getComponents();

				for (Component rChild : rComponents)
				{
					registerEventHandler(rChild, rEventTypes);
				}
			}
			else
			{
				registerEventHandler(rComponent, rEventTypes);
			}
		}

		return bHasEventHandling;
	}

	/***************************************
	 * Maps the interaction event types to the corresponding GEWT event types
	 * for a certain component.
	 *
	 * @param  rComponent             The component
	 * @param  rInteractionEventTypes The interaction event types to map
	 *
	 * @return The mapped GEWT event types
	 */
	protected Set<EventType> getInteractionEventTypes(
		Component				  rComponent,
		Set<InteractionEventType> rInteractionEventTypes)
	{
		Set<EventType> rEventTypes = EnumSet.noneOf(EventType.class);

		if (rComponent instanceof TextControl)
		{
			if (rInteractionEventTypes.contains(InteractionEventType.UPDATE))
			{
				rEventTypes.add(EventType.KEY_RELEASED);
			}

			if (rInteractionEventTypes.contains(InteractionEventType.ACTION))
			{
				rEventTypes.add(EventType.ACTION);
			}

			if (rInteractionEventTypes.contains(InteractionEventType.FOCUS_LOST))
			{
				rEventTypes.add(EventType.FOCUS_LOST);
			}
		}
		else
		{
			if (rInteractionEventTypes.contains(InteractionEventType.UPDATE))
			{
				rEventTypes.add(EventType.SELECTION);
			}

			if (rInteractionEventTypes.contains(InteractionEventType.ACTION))
			{
				rEventTypes.add(EventType.ACTION);
			}
		}

		return rEventTypes;
	}

	/***************************************
	 * Maps a GWT event type to the corresponding interaction event type.
	 *
	 * @param  eEventType The event type to map
	 *
	 * @return The matching interaction event type
	 */
	protected InteractionEventType mapToInteractionEventType(
		EventType eEventType)
	{
		InteractionEventType eInteractionEventType;

		if (eEventType == EventType.ACTION)
		{
			eInteractionEventType = InteractionEventType.ACTION;
		}
		else if (eEventType == EventType.FOCUS_LOST)
		{
			eInteractionEventType = InteractionEventType.FOCUS_LOST;
		}
		else
		{
			eInteractionEventType = InteractionEventType.UPDATE;
		}

		return eInteractionEventType;
	}

	/***************************************
	 * Processes a certain event and forwards it to the panel manager for
	 * interaction handling.
	 *
	 * @param rEvent The GEWT event that occurred
	 */
	protected void processEvent(EWTEvent rEvent)
	{
		EventType eEventType = rEvent.getType();
		Object    rEventData = rEvent.getElement();

		InteractionEventType eInteractionEventType =
			mapToInteractionEventType(eEventType);

		if (rEventData != null)
		{
			rDataElement.setProperty(INTERACTION_EVENT_DATA,
									 rEventData.toString());
		}

		// VALUE_CHANGED can occur if a text field looses focus
		if ((eEventType != EventType.VALUE_CHANGED &&
			 eEventType != EventType.FOCUS_LOST) ||
			!(rEvent.getSource() instanceof TextControl))
		{
			// this is needed to re-establish the input focus in certain
			// browsers (Webkit, IE)
			rDataElement.setFlag(FOCUSED);
		}

		if (eEventType != EventType.KEY_RELEASED ||
			hasValueChanged((TextControl) rEvent.getSource()))
		{
			rPanelManager.getRootDataElementPanelManager().collectInput();
			rPanelManager.handleInteractiveInput(rDataElement,
												 eInteractionEventType);
		}
	}

	/***************************************
	 * Registers an event listener for the handling of interactive input events
	 * with the given component.
	 *
	 * @param aComponent  The component
	 * @param rEventTypes The event types to register the event handlers for
	 */
	protected void registerEventHandler(
		Component				  aComponent,
		Set<InteractionEventType> rEventTypes)
	{
		for (EventType eEventType :
			 getInteractionEventTypes(aComponent, rEventTypes))
		{
			aComponent.addEventListener(eEventType, this);
		}
	}

	/***************************************
	 * Updates the data element to a new instance.
	 *
	 * @param rNewDataElement The new data element
	 */
	void updateDataElement(D rNewDataElement)
	{
		rDataElement = rNewDataElement;
	}

	/***************************************
	 * Checks whether the value of a text control component has changed compared
	 * to the data element.
	 *
	 * @param  rTextControl The component to check
	 *
	 * @return TRUE if the value has changed
	 */
	private boolean hasValueChanged(TextControl rTextControl)
	{
		return !rTextControl.getText().equals(rDataElement.getValue());
	}
}
