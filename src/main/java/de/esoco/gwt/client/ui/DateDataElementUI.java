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

import de.esoco.data.element.DateDataElement;
import de.esoco.data.element.DateDataElement.DateInputType;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Calendar;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.DateField;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.StyleFlag;

import de.esoco.lib.property.DateAttribute;
import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.InteractiveInputMode;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.esoco.data.element.DateDataElement.DATE_INPUT_TYPE;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;


/********************************************************************
 * The user interface implementation for date data elements.
 *
 * @author eso
 */
public class DateDataElementUI extends DataElementUI<DateDataElement>
{
	//~ Instance fields --------------------------------------------------------

	private Map<Date, String> rCalendarEvents = Collections.emptyMap();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public DateDataElementUI()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected Component createInputUI(ContainerBuilder<?> rBuilder,
									  StyleData			  rInputStyle,
									  DateDataElement	  rDataElement)
	{
		Date	  rDate		 = rDataElement.getValue();
		Component rComponent;

		DateInputType eDateInputType =
			rDataElement.getProperty(DATE_INPUT_TYPE, null);

		if (rDataElement.getProperty(CONTENT_TYPE, null) ==
			ContentType.DATE_TIME)
		{
			rInputStyle = rInputStyle.setFlags(StyleFlag.DATE_TIME);
		}

		if (eDateInputType == DateInputType.INPUT_FIELD)
		{
			DateField aDateField = rBuilder.addDateField(rInputStyle, rDate);

			aDateField.setColumns(rDataElement.getIntProperty(COLUMNS, 10));
			rComponent = aDateField;
		}
		else
		{
			Calendar aCalendar = rBuilder.addCalendar(rInputStyle, rDate);

			updateCalendar(aCalendar, rDataElement);
			rComponent = aCalendar;
		}

		return rComponent;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected DataElementInteractionHandler<DateDataElement> createInteractionHandler(
		DataElementPanelManager rPanelManager,
		DateDataElement			rDataElement)
	{
		return new DateInteractionHandler(rPanelManager, rDataElement);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void transferDataElementValueToComponent(
		DateDataElement rElement,
		Component		rComponent)
	{
		if (rComponent instanceof DateAttribute)
		{
			((DateAttribute) rComponent).setDate(rElement.getValue());

			if (rComponent instanceof Calendar)
			{
				updateCalendar((Calendar) rComponent, rElement);
			}
		}
		else
		{
			super.transferDataElementValueToComponent(rElement, rComponent);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void transferInputToDataElement(
		Component		rComponent,
		DateDataElement rElement)
	{
		if (rComponent instanceof DateAttribute)
		{
			rElement.setValue(((DateAttribute) rComponent).getDate());
		}
		else
		{
			super.transferInputToDataElement(rComponent, rElement);
		}
	}

	/***************************************
	 * Sets the highlights on a calendar component from the properties of a date
	 * data element.
	 *
	 * @param rCalendar    The calendar component
	 * @param rDataElement The date data element
	 */
	private void updateCalendar(
		Calendar		rCalendar,
		DateDataElement rDataElement)
	{
		for (Entry<Date, String> rEntry : rCalendarEvents.entrySet())
		{
			Date   rDate  = rEntry.getKey();
			String sStyle = rEntry.getValue();

			rCalendar.removeDateStyle(rDate, sStyle);
		}

		rCalendarEvents =
			rDataElement.getProperty(DateDataElement.DATE_HIGHLIGHTS,
									 Collections.<Date, String>emptyMap());

		for (Entry<Date, String> rEntry : rCalendarEvents.entrySet())
		{
			Date   rDate  = rEntry.getKey();
			String sStyle = rEntry.getValue();

			rCalendar.addDateStyle(rDate, sStyle);
		}
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * A date-specific interaction handler subclass.
	 *
	 * @author eso
	 */
	static class DateInteractionHandler
		extends DataElementInteractionHandler<DateDataElement>
	{
		//~ Constructors -------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		public DateInteractionHandler(
			DataElementPanelManager rPanelManager,
			DateDataElement			rDataElement)
		{
			super(rPanelManager, rDataElement);
		}

		//~ Methods ------------------------------------------------------------

		/***************************************
		 * {@inheritDoc}
		 */
		@Override
		protected Set<EventType> getInteractionEventTypes(
			Component			 aComponent,
			InteractiveInputMode eInputMode)
		{
			return EnumSet.of(EventType.VALUE_CHANGED);
		}
	}
}
