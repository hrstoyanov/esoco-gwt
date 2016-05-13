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

import de.esoco.data.element.BigDecimalDataElement;
import de.esoco.data.element.BooleanDataElement;
import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;
import de.esoco.data.element.DateDataElement;
import de.esoco.data.element.EntityDataElement;
import de.esoco.data.element.IntegerDataElement;
import de.esoco.data.element.PeriodDataElement;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.data.validate.HasValueList;

import java.util.HashMap;
import java.util.Map;


/********************************************************************
 * The central factory for new instances of {@link DataElementUI}.
 *
 * @author eso
 */
public class DataElementUIFactory
{
	//~ Static fields/initializers ---------------------------------------------

	private static Map<String, DataElementUICreator<?>> aDataElementRegistry =
		new HashMap<>();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private DataElementUIFactory()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * A factory method that creates a new data element user interface object
	 * for a certain data element.
	 *
	 * @param  rPanelManager The parent panel manager of the new element UI
	 * @param  rElement      The data element to create the user interface for
	 *
	 * @return An instance of a subclass for the given data element
	 *
	 * @throws IllegalArgumentException If no matching UI could be created
	 */
	@SuppressWarnings("unchecked")
	public static <T, D extends DataElement<T>> DataElementUI<?> create(
		DataElementPanelManager rPanelManager,
		D						rElement)
	{
		DataElementUI<?> aUI = null;

		DataElementUICreator<?> rUICreator =
			aDataElementRegistry.get(rElement.getClass().getName());

		if (rUICreator != null)
		{
			aUI = rUICreator.create();
		}
		else if (rElement instanceof BooleanDataElement)
		{
			aUI = new BooleanDataElementUI();
		}
		else if (rElement instanceof IntegerDataElement)
		{
			aUI = new IntegerDataElementUI();
		}
		else if (rElement instanceof BigDecimalDataElement)
		{
			aUI = new BigDecimalDataElementUI();
		}
		else if (rElement instanceof DateDataElement)
		{
			aUI = new DateDataElementUI();
		}
		else if (rElement instanceof PeriodDataElement)
		{
			aUI = new PeriodDataElementUI();
		}
		else if (rElement instanceof EntityDataElement)
		{
			aUI = new EntityDataElementUI();
		}
		else if (rElement instanceof SelectionDataElement)
		{
			aUI = new SelectionDataElementUI();
		}
		else if (rElement instanceof DataElementList)
		{
			aUI = new DataElementListUI();
		}
		else if (rElement.getValidator() instanceof HasValueList<?>)
		{
			aUI = new ValueListDataElementUI();
		}
		else
		{
			aUI = new DataElementUI<DataElement<?>>();
		}

		if (aUI == null)
		{
			throw new IllegalArgumentException("No UI for data element " +
											   rElement);
		}

		((DataElementUI<D>) aUI).init(rPanelManager, rElement);

		return aUI;
	}

	/***************************************
	 * Registers a data element UI creator for a certain data element type. This
	 * must be invoked once before a user interface for the given types shall be
	 * created.
	 *
	 * @param rDataElementClass The class of the data element type
	 * @param rCreator          The creator instance for the type
	 */
	public static <D extends DataElement<?>, U extends DataElementUI<D>> void registerDataElementUI(
		Class<D>				rDataElementClass,
		DataElementUICreator<U> rCreator)
	{
		aDataElementRegistry.put(rDataElementClass.getName(), rCreator);
	}

	//~ Inner Interfaces -------------------------------------------------------

	/********************************************************************
	 * An interface that defines the instantiation of data element UIs due to
	 * the lack of reflection in GWT.
	 *
	 * @author eso
	 */
	public static interface DataElementUICreator<U extends DataElementUI<?>>
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * Creates a new data element UI for the given type.
		 *
		 * @return A new {@link DataElementUI} instance
		 */
		public U create();
	}
}
