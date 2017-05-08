//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.data.element.IntegerDataElement;
import de.esoco.data.validate.IntegerRangeValidator;
import de.esoco.data.validate.Validator;

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.ProgressBar;
import de.esoco.ewt.component.Spinner;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.ContentType;

import static de.esoco.lib.property.ContentProperties.CONTENT_TYPE;


/********************************************************************
 * The user interface implementation for integer data elements.
 *
 * @author eso
 */
public class IntegerDataElementUI extends DataElementUI<IntegerDataElement>
{
	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * @see DataElementUI#DataElementUI()
	 */
	public IntegerDataElementUI()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected Component createDisplayUI(ContainerBuilder<?> rBuilder,
										StyleData			rStyle,
										IntegerDataElement  rDataElement)
	{
		Component rComponent;

		if (rDataElement.getProperty(CONTENT_TYPE, null) ==
			ContentType.PROGRESS)
		{
			ProgressBar  rProgressBar = rBuilder.addProgressBar(rStyle);
			Validator<?> rValidator   = rDataElement.getValidator();

			if (rValidator instanceof IntegerRangeValidator)
			{
				IntegerRangeValidator rRange =
					(IntegerRangeValidator) rValidator;

				rProgressBar.setMinimum(rRange.getMinimum());
				rProgressBar.setMaximum(rRange.getMaximum());
				rProgressBar.setValue(rDataElement.getValue().intValue());
			}

			rComponent = rProgressBar;
		}
		else
		{
			rComponent = super.createDisplayUI(rBuilder, rStyle, rDataElement);
		}

		return rComponent;
	}

	/***************************************
	 * @see DataElementUI#createInputUI(ContainerBuilder, StyleData,
	 *      de.esoco.data.element.DataElement)
	 */
	@Override
	@SuppressWarnings("boxing")
	protected Component createInputUI(ContainerBuilder<?> rBuilder,
									  StyleData			  rStyle,
									  IntegerDataElement  rDataElement)
	{
		Validator<?> rValidator = rDataElement.getValidator();
		Component    aComponent;

		if (rValidator instanceof IntegerRangeValidator)
		{
			IntegerRangeValidator rRange = (IntegerRangeValidator) rValidator;

			Spinner aSpinner =
				rBuilder.addSpinner(rStyle,
									rRange.getMinimum(),
									rRange.getMaximum(),
									1);

			if (rDataElement.getValue() != null)
			{
				aSpinner.setValue(rDataElement.getValue());
			}

			aComponent = aSpinner;
		}
		else
		{
			aComponent = super.createInputUI(rBuilder, rStyle, rDataElement);
		}

		return aComponent;
	}

	/***************************************
	 * @see DataElementUI#transferDataElementValueToComponent(de.esoco.data.element.DataElement,
	 *      Component)
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void transferDataElementValueToComponent(
		IntegerDataElement rElement,
		Component		   rComponent)
	{
		Integer rValue = rElement.getValue();

		if (rComponent instanceof Spinner)
		{
			((Spinner) rComponent).setValue(rValue != null ? rValue : 0);
		}
		else if (rComponent instanceof ProgressBar)
		{
			((ProgressBar) rComponent).setValue(rValue != null ? rValue : 0);
		}
		else
		{
			super.transferDataElementValueToComponent(rElement, rComponent);
		}
	}

	/***************************************
	 * @see DataElementUI#transferInputToDataElement(Component, de.esoco.data.element.DataElement)
	 */
	@Override
	@SuppressWarnings("boxing")
	protected void transferInputToDataElement(
		Component		   rComponent,
		IntegerDataElement rDataElement)
	{
		if (rComponent instanceof Spinner)
		{
			rDataElement.setValue(((Spinner) rComponent).getValue());
		}
		else
		{
			super.transferInputToDataElement(rComponent, rDataElement);
		}
	}
}
