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
import de.esoco.ewt.component.TextComponent;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;

import de.esoco.lib.property.SingleSelection;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Timer;

import static de.esoco.lib.property.UserInterfaceProperties.INTERACTIVE_INPUT_MODE;


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

	private DataElementPanelManager rPanelManager;
	private D					    rDataElement;
	private InteractiveInputMode    eInteractiveInputMode;

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
	 * Returns the interactive input mode.
	 *
	 * @return The interactive input mode
	 */
	public final InteractiveInputMode getInteractiveInputMode()
	{
		return eInteractiveInputMode;
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
	public void handleEvent(EWTEvent rEvent)
	{
		EventType     eEventType   = rEvent.getType();
		final boolean bActionEvent = (eEventType == EventType.ACTION);

		// only cause interactions if event is not caused by the parent's
		// interactive input handler to prevent recursion
		if ((eInteractiveInputMode == InteractiveInputMode.CONTINUOUS ||
			 eInteractiveInputMode == InteractiveInputMode.BOTH ||
			 bActionEvent))
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
						rPanelManager.getRootDataElementPanelManager()
									 .collectInput();
						rPanelManager.handleInteractiveInput(rDataElement,
															 bActionEvent);
					}
				};

			boolean bLongDelay =
				(eInteractiveInputMode == InteractiveInputMode.CONTINUOUS &&
				 eEventType == EventType.KEY_RELEASED);

			aInputEventTimer.schedule(bLongDelay ? 250 : 50);
		}
	}

	/***************************************
	 * Initializes the handling of interactive input events for a certain
	 * component if necessary.
	 *
	 * @param rComponent           The component to setup the input handling for
	 * @param bOnContainerChildren TRUE to setup the input handling for the
	 *                             children if the component is a container
	 */
	public void setupEventHandling(
		Component rComponent,
		boolean   bOnContainerChildren)
	{
		eInteractiveInputMode =
			rDataElement.getProperty(INTERACTIVE_INPUT_MODE,
									 InteractiveInputMode.NONE);

		if (eInteractiveInputMode != InteractiveInputMode.NONE)
		{
			if (bOnContainerChildren && rComponent instanceof Container)
			{
				List<Component> rComponents =
					((Container) rComponent).getComponents();

				for (Component rChild : rComponents)
				{
					registerEventHandler(rChild, eInteractiveInputMode);
				}
			}
			else
			{
				registerEventHandler(rComponent, eInteractiveInputMode);
			}
		}
	}

	/***************************************
	 * Returns the event types that need to be handles for interactions of the
	 * given component in a certain interactive input mode. Can be overridden by
	 * subclasses
	 *
	 * @param  aComponent The component
	 * @param  eInputMode The interactive input mode
	 *
	 * @return
	 */
	protected Set<EventType> getInteractionEventTypes(
		Component				   aComponent,
		final InteractiveInputMode eInputMode)
	{
		Set<EventType> rEventTypes = EnumSet.noneOf(EventType.class);

		if (aComponent instanceof TextComponent)
		{
			if (eInputMode == InteractiveInputMode.CONTINUOUS ||
				eInputMode == InteractiveInputMode.BOTH)
			{
				rEventTypes.add(EventType.VALUE_CHANGED);
				rEventTypes.add(EventType.KEY_RELEASED);
			}

			if (eInputMode == InteractiveInputMode.ACTION ||
				eInputMode == InteractiveInputMode.BOTH)
			{
				rEventTypes.add(EventType.ACTION);
			}
		}
		else if (aComponent instanceof SingleSelection)
		{
			if (eInputMode == InteractiveInputMode.CONTINUOUS ||
				eInputMode == InteractiveInputMode.BOTH)
			{
				rEventTypes.add(EventType.SELECTION);
			}

			if (eInputMode == InteractiveInputMode.ACTION ||
				eInputMode == InteractiveInputMode.BOTH)
			{
				rEventTypes.add(EventType.ACTION);
			}
		}
		else
		{
			rEventTypes.add(EventType.ACTION);
		}

		return rEventTypes;
	}

	/***************************************
	 * Registers an event listener for the handling of interactive input events
	 * with the given component.
	 *
	 * @param aComponent The component
	 * @param eInputMode The input mode
	 */
	protected void registerEventHandler(
		Component				   aComponent,
		final InteractiveInputMode eInputMode)
	{
		for (EventType rEventType :
			 getInteractionEventTypes(aComponent, eInputMode))
		{
			aComponent.addEventListener(rEventType, this);
		}
	}
}
