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
import de.esoco.data.element.ListDataElement;

import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Button;
import de.esoco.ewt.component.ComboBox;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.ListControl;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.layout.FlowLayout;
import de.esoco.ewt.layout.GenericLayout;
import de.esoco.ewt.layout.GridLayout;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.StyleFlag;

import de.esoco.gwt.client.res.EsocoGwtResources;

import de.esoco.lib.property.Selectable;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.Layout;
import de.esoco.lib.property.UserInterfaceProperties.ListStyle;

import java.util.ArrayList;
import java.util.List;

import static de.esoco.data.element.DataElement.ALLOWED_VALUES_CHANGED;

import static de.esoco.ewt.style.StyleData.WEB_ADDITIONAL_STYLES;

import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.UserInterfaceProperties.LIST_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.ROWS;


/********************************************************************
 * A data element UI implementation for data elements that are constrained to a
 * list of allowed values.
 *
 * @author eso
 */
public class ValueListDataElementUI extends DataElementUI<DataElement<?>>
{
	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected Component createInputUI(ContainerBuilder<?> rBuilder,
									  StyleData			  rInputStyle,
									  DataElement<?>	  rDataElement)
	{
		return createListComponent(rBuilder, rInputStyle, rDataElement);
	}

	/***************************************
	 * Updates the value of the element component if the data element value has
	 * changed.
	 */
	@Override
	protected void updateValue()
	{
		Component    rElementComponent = getElementComponent();
		List<String> rValues		   =
			getListValues(rElementComponent.getContext(), getDataElement());

		boolean bAllowedValuesChanged =
			getDataElement().hasFlag(ALLOWED_VALUES_CHANGED);

		if (rElementComponent instanceof ListControl)
		{
			updateList(rValues, bAllowedValuesChanged);
		}
		else if (rElementComponent instanceof ComboBox)
		{
			updateComboBox((ComboBox) rElementComponent,
						   rValues,
						   bAllowedValuesChanged);
		}
		else if (rElementComponent instanceof Container)
		{
			updateButtons(rValues, bAllowedValuesChanged);
		}
	}

	/***************************************
	 * Applies the indices of currently selected values to a list of components.
	 * Only components that implement the {@link Selectable} interface will be
	 * considered, any other components will be ignored.
	 *
	 * @param rSelection  The indices of the currently selected values
	 * @param rComponents A list of components
	 */
	private void applyCurrentSelection(
		int[]					  rSelection,
		List<? extends Component> rComponents)
	{
		int nSelectable     = 0;
		int nSelectionIndex = 0;

		if (rSelection != null && rSelection.length > 0)
		{
			for (Component rComponent : rComponents)
			{
				if (rComponent instanceof Selectable)
				{
					boolean bSelected =
						nSelectionIndex < rSelection.length &&
						nSelectable == rSelection[nSelectionIndex];

					((Selectable) rComponent).setSelected(bSelected);

					if (bSelected)
					{
						++nSelectionIndex;
					}

					nSelectable++;
				}
			}
		}
	}

	/***************************************
	 * Creates and initializes a {@link de.esoco.ewt.component.List} component.
	 *
	 * @param  rBuilder     The builder to add the list with
	 * @param  rStyle       The style for the list
	 * @param  rDataElement The data element to create the list for
	 *
	 * @return The list component
	 */
	private de.esoco.ewt.component.List createList(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		DataElement<?>		rDataElement)
	{
		de.esoco.ewt.component.List aList = rBuilder.addList(rStyle);
		int						    nRows =
			rDataElement.getIntProperty(ROWS, -1);

		if (nRows > 1)
		{
			aList.setVisibleItems(nRows);
		}

		return aList;
	}

	/***************************************
	 * Creates a panel that contains buttons for a list of labels.
	 *
	 * @param  rBuilder          The builder to create the button panel with
	 * @param  rStyle            The style for the button panel
	 * @param  rDataElement      The data element to create the panel for
	 * @param  rButtonLabels     The labels of the buttons to create
	 * @param  eListStyle        The list style
	 * @param  rCurrentSelection The indices of the currently selected Values
	 *
	 * @return The container containing the buttons
	 */
	private Component createListButtonPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		DataElement<?>		rDataElement,
		List<String>		rButtonLabels,
		ListStyle			eListStyle,
		int[]				rCurrentSelection)
	{
		int nColumns = rDataElement.getIntProperty(COLUMNS, 1);

		UserInterfaceProperties.Layout eLayout =
			rDataElement.getProperty(UserInterfaceProperties.LAYOUT,
									 getButtonPanelDefaultLayout());

		String sAddStyle = rStyle.getProperty(WEB_ADDITIONAL_STYLES, "");

		sAddStyle += " " + EsocoGwtResources.INSTANCE.css().gfButtonPanel();

		// insert menu buttons directly into enclosing panels
		if (eLayout != Layout.MENU)
		{
			GenericLayout aPanelLayout =
				eLayout == Layout.TABLE ? new GridLayout(nColumns)
										: new FlowLayout();

			rBuilder =
				rBuilder.addPanel(rStyle.set(WEB_ADDITIONAL_STYLES, sAddStyle),
								  aPanelLayout);
		}

		final List<Component> aButtons =
			createListButtons(rBuilder,
							  rDataElement,
							  rButtonLabels,
							  eListStyle);

		applyCurrentSelection(rCurrentSelection, aButtons);

		return rBuilder.getContainer();
	}

	/***************************************
	 * Creates buttons for a list of labels.
	 *
	 * @param  rBuilder      The builder to create the buttons with
	 * @param  rDataElement  The data element to create the buttons from
	 * @param  rButtonLabels The labels of the buttons to create
	 * @param  eListStyle    The list style of the buttons
	 *
	 * @return A list containing the buttons that have been created
	 */
	private List<Component> createListButtons(ContainerBuilder<?> rBuilder,
											  DataElement<?>	  rDataElement,
											  List<String>		  rButtonLabels,
											  ListStyle			  eListStyle)
	{
		final List<Component> aButtons = new ArrayList<>(rButtonLabels.size());

		EWTEventHandler aButtonEventHandler =
			new EWTEventHandler()
			{
				@Override
				public void handleEvent(EWTEvent rEvent)
				{
					setButtonSelection(aButtons, (Button) rEvent.getSource());
				}
			};

		String  sDisabled    = rDataElement.getProperty(DISABLED_ELEMENTS, "");
		boolean bMultiselect = rDataElement instanceof ListDataElement;

		StyleData rButtonStyle = createButtonStyle(rDataElement);

		int nValueIndex = 0;

		for (String sValue : rButtonLabels)
		{
			String    sText   = sValue;
			Component aButton;

			if (eListStyle == ListStyle.IMMEDIATE)
			{
				if (bMultiselect)
				{
					aButton =
						rBuilder.addToggleButton(rButtonStyle, sText, null);
				}
				else
				{
					aButton = rBuilder.addButton(rButtonStyle, sText, null);
				}
			}
			else
			{
				if (bMultiselect)
				{
					aButton = rBuilder.addCheckBox(rButtonStyle, sText, null);
				}
				else
				{
					aButton =
						rBuilder.addRadioButton(rButtonStyle, sText, null);
				}
			}

			if (sDisabled.contains("(" + nValueIndex++ + ")"))
			{
				aButton.setEnabled(false);
			}

			aButton.addEventListener(EventType.ACTION, aButtonEventHandler);
			aButtons.add(aButton);
		}

		return aButtons;
	}

	/***************************************
	 * Creates a {@link ComboBox} component for a list of values.
	 *
	 * @param  rBuilder     The builder to create the component with
	 * @param  rStyle       The default style for the component
	 * @param  rDataElement The data element to create the list for
	 * @param  rValues      The list of values to display
	 *
	 * @return The new component
	 */
	private ComboBox createListComboBox(ContainerBuilder<?> rBuilder,
										StyleData			rStyle,
										DataElement<?>		rDataElement,
										List<String>		rValues)
	{
		ComboBox aComboBox = rBuilder.addComboBox(rStyle, null);

		updateComboBox(aComboBox, rValues, true);

		return aComboBox;
	}

	/***************************************
	 * Creates a component to select the data element's value from a list of
	 * values that are defined in a list validator. Depending on the data
	 * element type and the component style different types of components will
	 * be created.
	 *
	 * @param  rBuilder     The builder to add the list with
	 * @param  rStyle       The style data for the list
	 * @param  rDataElement The data element to create the component for
	 *
	 * @return A new list component
	 */
	private Component createListComponent(ContainerBuilder<?> rBuilder,
										  StyleData			  rStyle,
										  DataElement<?>	  rDataElement)
	{
		UserInterfaceContext rContext = rBuilder.getContext();
		List<String>		 rValues  = getListValues(rContext, rDataElement);

		int[] rCurrentSelection =
			getCurrentSelection(rContext, rDataElement, rValues);

		ListStyle eListStyle = getListStyle(rDataElement, rValues);

		Component aComponent = null;

		if (rDataElement instanceof ListDataElement)
		{
			rStyle = rStyle.setFlags(StyleFlag.MULTISELECT);
		}

		switch (eListStyle)
		{
			case LIST:
				aComponent =
					setListControlValues(createList(rBuilder,
													rStyle,
													rDataElement),
										 rValues,
										 rCurrentSelection);
				break;

			case DROP_DOWN:
				aComponent =
					setListControlValues(rBuilder.addListBox(rStyle),
										 rValues,
										 rCurrentSelection);
				break;

			case EDITABLE:
				aComponent =
					createListComboBox(rBuilder, rStyle, rDataElement, rValues);
				break;

			case DISCRETE:
			case IMMEDIATE:
				aComponent =
					createListButtonPanel(rBuilder,
										  rStyle,
										  rDataElement,
										  rValues,
										  eListStyle,
										  rCurrentSelection);
				break;
		}

		return aComponent;
	}

	/***************************************
	 * Returns the list style for a certain data element. If no explicit style
	 * is set a default will be determined from the value list size.
	 *
	 * @param  rDataElement The data element
	 * @param  rValues      The value list
	 *
	 * @return The list style
	 */
	private ListStyle getListStyle(
		DataElement<?> rDataElement,
		List<String>   rValues)
	{
		ListStyle eListStyle =
			rValues.size() > 6 ? ListStyle.LIST : ListStyle.DROP_DOWN;

		eListStyle = rDataElement.getProperty(LIST_STYLE, eListStyle);

		return eListStyle;
	}

	/***************************************
	 * Updates the buttons in a list button panel.
	 *
	 * @param rButtonLabels   The button labels
	 * @param bButtonsChanged TRUE if button text has changed
	 */
	private void updateButtons(
		List<String> rButtonLabels,
		boolean		 bButtonsChanged)
	{
		Container rContainer = (Container) getElementComponent();

		List<? extends Component> rButtons = rContainer.getComponents();

		if (bButtonsChanged)
		{
			if (rButtons.size() == rButtonLabels.size())
			{
				int nIndex = 0;

				for (Component rChild : rButtons)
				{
					if (rChild instanceof Button)
					{
						((Button) rChild).setProperties(rButtonLabels.get(nIndex++));
					}
				}
			}
			else
			{
				ContainerBuilder<?> aBuilder =
					new ContainerBuilder<>(rContainer);

				DataElement<?> rDataElement = getDataElement();

				ListStyle eListStyle =
					getListStyle(rDataElement, rButtonLabels);

				rContainer.clear();

				rButtons =
					createListButtons(aBuilder,
									  rDataElement,
									  rButtonLabels,
									  eListStyle);

				setupInteractionHandling(rContainer, true);
			}
		}

		int[] rCurrentSelection =
			getCurrentSelection(rContainer.getContext(),
								getDataElement(),
								rButtonLabels);

		applyCurrentSelection(rCurrentSelection, rButtons);
	}

	/***************************************
	 * Updates a combo box component with new values.
	 *
	 * @param rComboBox       The combo box component
	 * @param rValues         The value list object
	 * @param bChoicesChanged TRUE if the combo box choices have changed
	 */
	private void updateComboBox(ComboBox	 rComboBox,
								List<String> rValues,
								boolean		 bChoicesChanged)
	{
		DataElement<?> rDataElement = getDataElement();

		if (bChoicesChanged)
		{
			rComboBox.clearChoices();

			for (String sValue : rValues)
			{
				rComboBox.addChoice(sValue);
			}
		}

		if (rDataElement instanceof ListDataElement)
		{
			rComboBox.clearValues();

			for (Object rItem : (ListDataElement<?>) rDataElement)
			{
				rComboBox.addValue(convertValueToString(rDataElement, rItem));
			}
		}
		else
		{
			rComboBox.setText(convertValueToString(rDataElement,
												   rDataElement.getValue()));
		}
	}

	/***************************************
	 * Updates a list control component with new values.
	 *
	 * @param rValues            The list values
	 * @param bListValuesChanged TRUE if the list values have changed, not only
	 *                           the current selection
	 */
	private void updateList(List<String> rValues, boolean bListValuesChanged)
	{
		ListControl rListControl = (ListControl) getElementComponent();

		if (bListValuesChanged)
		{
			rListControl.removeAll();

			for (String sValue : rValues)
			{
				rListControl.add(sValue);
			}
		}

		rListControl.setSelection(getCurrentSelection(rListControl.getContext(),
													  getDataElement(),
													  rValues));
	}
}
