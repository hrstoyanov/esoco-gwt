//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
import de.esoco.data.element.IntegerDataElement;
import de.esoco.data.element.PeriodDataElement;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.data.validate.HasValueList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/********************************************************************
 * The central factory for new instances of {@link DataElementUI}.
 *
 * @author eso
 */
public class DataElementUIFactory
{
	//~ Static fields/initializers ---------------------------------------------

	private static Map<String, Supplier<?>> aDataElementRegistry =
		new HashMap<>();

	static
	{
		registerDataElementUI(DataElementList.class, DataElementListUI::new);
		registerDataElementUI(
			BooleanDataElement.class,
			BooleanDataElementUI::new);
		registerDataElementUI(
			IntegerDataElement.class,
			IntegerDataElementUI::new);
		registerDataElementUI(
			BigDecimalDataElement.class,
			BigDecimalDataElementUI::new);
		registerDataElementUI(DateDataElement.class, DateDataElementUI::new);
		registerDataElementUI(
			PeriodDataElement.class,
			PeriodDataElementUI::new);
		registerDataElementUI(
			SelectionDataElement.class,
			SelectionDataElementUI::new);
	}

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

		Supplier<?> rUiSupplier =
			aDataElementRegistry.get(rElement.getClass().getName());

		if (rUiSupplier != null)
		{
			aUI = (DataElementUI<D>) rUiSupplier.get();
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
			throw new IllegalArgumentException(
				"No UI for data element " +
				rElement);
		}

		((DataElementUI<D>) aUI).init(rPanelManager, rElement);

		return aUI;
	}

	/***************************************
	 * Returns the registered data element UI factory for a certain data element
	 * type. The returned value can be used to create UI factories with a
	 * fallback on previously registered UIs.
	 *
	 * @param  rDataElementClass The data element type class
	 *
	 * @return The factory function for the type or NULL if none has been
	 *         registered so far
	 */
	@SuppressWarnings("unchecked")
	public static <D extends DataElement<?>, U extends DataElementUI<D>> Supplier<U>
	getRegisteredUI(Class<D> rDataElementClass)
	{
		return (Supplier<U>) aDataElementRegistry.get(
			rDataElementClass.getName());
	}

	/***************************************
	 * Registers a data element UI factory for a certain data element type. This
	 * will replace any previously registered factory if such exists. If an
	 * existing factory should be extended instead the original can be queried
	 * with {@link #getRegisteredUI(Class)}.
	 *
	 * @param rDataElementClass The data element type class
	 * @param rCreator          The factory function for the type
	 */
	public static <D extends DataElement<?>, U extends DataElementUI<D>> void
	registerDataElementUI(Class<D> rDataElementClass, Supplier<U> rCreator)
	{
		aDataElementRegistry.put(rDataElementClass.getName(), rCreator);
	}
}
