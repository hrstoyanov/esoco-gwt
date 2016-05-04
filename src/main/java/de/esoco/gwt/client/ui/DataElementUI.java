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
import de.esoco.data.element.DataElementList.Layout;
import de.esoco.data.element.ListDataElement;
import de.esoco.data.element.SelectionDataElement;
import de.esoco.data.element.StringDataElement;
import de.esoco.data.validate.HasValueList;
import de.esoco.data.validate.ListValidator;
import de.esoco.data.validate.StringListValidator;
import de.esoco.data.validate.Validator;

import de.esoco.ewt.EWT;
import de.esoco.ewt.UserInterfaceContext;
import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Button;
import de.esoco.ewt.component.CheckBox;
import de.esoco.ewt.component.ComboBox;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Container;
import de.esoco.ewt.component.Control;
import de.esoco.ewt.component.FileChooser;
import de.esoco.ewt.component.Label;
import de.esoco.ewt.component.ListControl;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.component.SelectableButton;
import de.esoco.ewt.component.TextArea;
import de.esoco.ewt.component.TextComponent;
import de.esoco.ewt.component.TextField;
import de.esoco.ewt.event.EWTEvent;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.event.KeyCode;
import de.esoco.ewt.event.ModifierKeys;
import de.esoco.ewt.layout.FlowLayout;
import de.esoco.ewt.layout.GenericLayout;
import de.esoco.ewt.layout.GridLayout;
import de.esoco.ewt.style.StyleData;
import de.esoco.ewt.style.StyleFlag;

import de.esoco.gwt.client.res.EsocoGwtCss;
import de.esoco.gwt.client.res.EsocoGwtResources;

import de.esoco.lib.property.Selectable;
import de.esoco.lib.property.TextAttribute;
import de.esoco.lib.property.UserInterfaceProperties;
import de.esoco.lib.property.UserInterfaceProperties.ContentType;
import de.esoco.lib.property.UserInterfaceProperties.LabelStyle;
import de.esoco.lib.property.UserInterfaceProperties.ListStyle;
import de.esoco.lib.text.TextConvert;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import static de.esoco.data.element.DataElement.ALLOWED_VALUES_CHANGED;
import static de.esoco.data.element.DataElement.HIDDEN_URL;
import static de.esoco.data.element.DataElement.INTERACTION_URL;
import static de.esoco.data.element.DataElement.ITEM_RESOURCE_PREFIX;

import static de.esoco.ewt.style.StyleData.WEB_ADDITIONAL_STYLES;

import static de.esoco.lib.property.UserInterfaceProperties.CARET_POSITION;
import static de.esoco.lib.property.UserInterfaceProperties.COLUMNS;
import static de.esoco.lib.property.UserInterfaceProperties.CONTENT_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.CSS_STYLES;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED;
import static de.esoco.lib.property.UserInterfaceProperties.DISABLED_ELEMENTS;
import static de.esoco.lib.property.UserInterfaceProperties.EDITABLE;
import static de.esoco.lib.property.UserInterfaceProperties.FORMAT;
import static de.esoco.lib.property.UserInterfaceProperties.HAS_IMAGES;
import static de.esoco.lib.property.UserInterfaceProperties.HEIGHT;
import static de.esoco.lib.property.UserInterfaceProperties.HIDDEN;
import static de.esoco.lib.property.UserInterfaceProperties.INPUT_CONSTRAINT;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.LIST_STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.MIME_TYPE;
import static de.esoco.lib.property.UserInterfaceProperties.NO_INTERACTION_LOCK;
import static de.esoco.lib.property.UserInterfaceProperties.NO_RESOURCE_PREFIX;
import static de.esoco.lib.property.UserInterfaceProperties.NO_WRAP;
import static de.esoco.lib.property.UserInterfaceProperties.PLACEHOLDER;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE;
import static de.esoco.lib.property.UserInterfaceProperties.RESOURCE_ID;
import static de.esoco.lib.property.UserInterfaceProperties.ROWS;
import static de.esoco.lib.property.UserInterfaceProperties.STYLE;
import static de.esoco.lib.property.UserInterfaceProperties.TOOLTIP;
import static de.esoco.lib.property.UserInterfaceProperties.URL;
import static de.esoco.lib.property.UserInterfaceProperties.VALUE_CHANGED;
import static de.esoco.lib.property.UserInterfaceProperties.VALUE_RESOURCE_PREFIX;
import static de.esoco.lib.property.UserInterfaceProperties.VERTICAL;
import static de.esoco.lib.property.UserInterfaceProperties.WIDTH;
import static de.esoco.lib.property.UserInterfaceProperties.WRAP;


/********************************************************************
 * An base class for the implementation of user interfaces for single data
 * element objects. All methods have standard implementations that are
 * sufficient for data elements with simple datatypes which implement the method
 * {@link DataElement#setStringValue(String)}.
 *
 * @author eso
 */
public class DataElementUI<D extends DataElement<?>>
{
	//~ Static fields/initializers ---------------------------------------------

	private static final EsocoGwtCss CSS = EsocoGwtResources.INSTANCE.css();

	/** The default prefix for label resource IDs. */
	protected static final String LABEL_RESOURCE_PREFIX = "$lbl";

	/** The default suffix for label strings. */
	protected static final String LABEL_SUFFIX = ":";

	/** The default gap between components. */
	protected static final int DEFAULT_COMPONENT_GAP = 5;

	private static Layout eButtonPanelDefaultLayout =
		Layout.TABLE;

	private static final int[] PHONE_NUMBER_FIELD_SIZES =
		new int[] { 3, 5, 8, 4 };

	private static final String[] PHONE_NUMBER_FIELD_TOOLTIPS =
		new String[]
		{
			"$ttPhoneCountryCode", "$ttPhoneAreaCode", "$ttPhoneNumber",
			"$ttPhoneExtension"
		};

	//~ Instance fields --------------------------------------------------------

	private DataElementPanelManager rPanelManager;
	private D					    rDataElement;

	private StyleData rBaseStyle;

	private Label     aElementLabel;
	private Component aElementComponent;
	private String    sToolTip			  = null;
	private String    sHiddenLabelHint    = null;
	private String    sTemporaryTextStore = null;
	private CheckBox  rOptionalCheckBox   = null;

	private boolean bInteractionEnabled = true;
	private boolean bUIEnabled		    = true;

	private DataElementInteractionHandler<D> aInteractionHandler;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 */
	public DataElementUI()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Applies the style properties of a data element to a style data object.
	 *
	 * @param  rDataElement The data element
	 * @param  rStyle       The style data to apply the element styles to
	 *
	 * @return A new style data object
	 */
	public static StyleData applyElementStyle(
		DataElement<?> rDataElement,
		StyleData	   rStyle)
	{
		String sStyle    = rDataElement.getProperty(STYLE, null);
		String sMimeType = rDataElement.getProperty(MIME_TYPE, null);

		Map<String, String> rCss = rDataElement.getProperty(CSS_STYLES, null);

		if (rCss != null)
		{
			rStyle = rStyle.set(CSS_STYLES, rCss);
		}

		if (sStyle != null)
		{
			rStyle = rStyle.append(WEB_ADDITIONAL_STYLES, sStyle);
		}

		if (sMimeType != null)
		{
			rStyle = rStyle.set(MIME_TYPE, sMimeType);
		}

		if (rDataElement.hasProperty(WIDTH))
		{
			rStyle = rStyle.w(rDataElement.getIntProperty(WIDTH, 0));
		}

		if (rDataElement.hasProperty(HEIGHT))
		{
			rStyle = rStyle.h(rDataElement.getIntProperty(HEIGHT, 0));
		}

		if (rDataElement.hasFlag(VERTICAL))
		{
			rStyle = rStyle.setFlags(StyleFlag.VERTICAL);
		}

		if (rDataElement.hasFlag(WRAP))
		{
			rStyle = rStyle.setFlags(StyleFlag.WRAP);
		}

		if (rDataElement.hasFlag(NO_WRAP))
		{
			rStyle = rStyle.setFlags(StyleFlag.NO_WRAP);
		}

		if (rDataElement.hasFlag(RESOURCE))
		{
			rStyle = rStyle.setFlags(StyleFlag.RESOURCE);
		}

		return rStyle;
	}

	/***************************************
	 * Returns the default layout mode used for button panels.
	 *
	 * @return The default layout mode
	 */
	public static Layout getButtonPanelDefaultLayout()
	{
		return eButtonPanelDefaultLayout;
	}

	/***************************************
	 * Returns the resource ID prefix for a value item of a certain data
	 * element.
	 *
	 * @param  rDataElement The data element
	 *
	 * @return The prefix for a value item
	 */
	public static String getValueItemPrefix(DataElement<?> rDataElement)
	{
		String sItemPrefix = ITEM_RESOURCE_PREFIX;

		if (!rDataElement.hasFlag(NO_RESOURCE_PREFIX))
		{
			String sValuePrefix =
				rDataElement.getProperty(VALUE_RESOURCE_PREFIX, null);

			sItemPrefix +=
				sValuePrefix != null ? sValuePrefix
									 : rDataElement.getResourceId();
		}

		return sItemPrefix;
	}

	/***************************************
	 * Returns a value item resource string for a certain data element value. If
	 * the value has already been converted to a item resource it will be
	 * returned unchanged.
	 *
	 * @param  rDataElement The data element
	 * @param  sValue       The value to convert
	 *
	 * @return The value item string
	 */
	public static String getValueItemString(
		DataElement<?> rDataElement,
		String		   sValue)
	{
		if (sValue.length() > 0 && sValue.charAt(0) != '$')
		{
			sValue =
				getValueItemPrefix(rDataElement) +
				TextConvert.capitalizedIdentifier(sValue);
		}

		return sValue;
	}

	/***************************************
	 * Sets the default layout mode to be used for button panels.
	 *
	 * @param eLayoutMode The new button panel default layout mode
	 */
	public static void setButtonPanelDefaultLayout(Layout eLayoutMode)
	{
		eButtonPanelDefaultLayout = eLayoutMode;
	}

	/***************************************
	 * Returns the UI label text for a certain data element.
	 *
	 * @param  rContext        The user interface context to expand resources
	 * @param  rDataElement    The data element
	 * @param  sResourcePrefix The prefix for resource IDs (should typically
	 *                         start with a '$' character)
	 *
	 * @return The expanded label text (can be empty but will never be null)
	 */
	static String getLabelText(UserInterfaceContext rContext,
							   DataElement<?>		rDataElement,
							   String				sResourcePrefix)
	{
		String sLabel = rDataElement.getProperty(LABEL, null);

		if (sLabel == null)
		{
			sLabel = rDataElement.getProperty(RESOURCE_ID, null);

			if (sLabel == null)
			{
				sLabel = rDataElement.getResourceId();
			}

			if (sLabel.length() > 0)
			{
				sLabel = sResourcePrefix + sLabel;
			}
		}

		if (sLabel != null && sLabel.length() > 0)
		{
			sLabel = rContext.expandResource(sLabel);
		}

		return sLabel;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Adds an event handler to the element component. If the element component
	 * is a container the event handler will be added to the container children
	 * (in the case of button panels). The source of events received from this
	 * registration will be the element component, not this instance.
	 *
	 * @param eEventType The event type to add the handler for
	 * @param rHandler   The event handler
	 */
	public void addEventListener(EventType		 eEventType,
								 EWTEventHandler rHandler)
	{
		Component rComponent = getElementComponent();

		if (rComponent instanceof Container)
		{
			List<Component> rComponents =
				((Container) rComponent).getComponents();

			for (Component rChild : rComponents)
			{
				rChild.addEventListener(eEventType, rHandler);
			}
		}
		else
		{
			rComponent.addEventListener(eEventType, rHandler);
		}
	}

	/***************************************
	 * Builds the user interface for the data element. This method Invokes
	 * {@link #buildDataElementUI(ContainerBuilder, StyleData)} which can be
	 * overridden by subclasses to modify the default building if necessary.
	 *
	 * @param rBuilder The container builder to create the components with
	 * @param rStyle   The style data for display components
	 */
	public final void buildUserInterface(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle)
	{
		rBaseStyle		  = rStyle;
		rStyle			  = applyElementStyle(rDataElement, rStyle);
		aElementComponent = buildDataElementUI(rBuilder, rStyle);

		applyElementProperties();
	}

	/***************************************
	 * This method must be invoked externally to collect the values from an
	 * input user interface into the associated data element. Only after this
	 * will the data element contain any new values that have been input by the
	 * user. If this instance is not for input the call will be ignored.
	 * Therefore it won't harm to invoke this method on display user interfaces
	 * too.
	 */
	public void collectInput()
	{
		if (!rDataElement.isImmutable())
		{
			if (rOptionalCheckBox != null)
			{
				rDataElement.setSelected(rOptionalCheckBox.isSelected());
			}

			transferInputToDataElement(aElementComponent, rDataElement);
		}
	}

	/***************************************
	 * Returns the base style data object for this instance. This is the style
	 * before applying any styles from the data element properties.
	 *
	 * @return The base style data
	 */
	public final StyleData getBaseStyle()
	{
		return rBaseStyle;
	}

	/***************************************
	 * Returns the data element of this instance.
	 *
	 * @return The data element
	 */
	public final D getDataElement()
	{
		return rDataElement;
	}

	/***************************************
	 * Returns the user interface component that is associated with the data
	 * element value.
	 *
	 * @return The user interface component for the data element
	 */
	public final Component getElementComponent()
	{
		return aElementComponent;
	}

	/***************************************
	 * Returns the text for a label that describes the data element.
	 *
	 * @param  rContext rBuilder
	 *
	 * @return The element label (can be empty but will never be null)
	 */
	public String getElementLabelText(UserInterfaceContext rContext)
	{
		return getLabelText(rContext, rDataElement, LABEL_RESOURCE_PREFIX);
	}

	/***************************************
	 * Returns the style name for this UI's data element.
	 *
	 * @return
	 */
	public String getElementStyleName()
	{
		return rDataElement.getResourceId();
	}

	/***************************************
	 * Returns the parent date element panel manager of this instance.
	 *
	 * @return The parent panel manager
	 */
	public final DataElementPanelManager getParent()
	{
		return rPanelManager;
	}

	/***************************************
	 * Sets the input focus on the input component of this instance if possible.
	 * This method may be overridden by complex user interface implementations
	 * to set the input focus to a specific component.
	 *
	 * @return TRUE if the input focus could be set
	 */
	public boolean requestFocus()
	{
		boolean bIsControl = (aElementComponent instanceof Control);

		if (bIsControl)
		{
			((Control) aElementComponent).requestFocus();
		}

		return bIsControl;
	}

	/***************************************
	 * Sets the component size as HTML string values.
	 *
	 * @param sWidth  The component width
	 * @param sHeight The component height
	 */
	public void setComponentSize(String sWidth, String sHeight)
	{
		aElementComponent.getWidget().setSize("100%", "100%");
	}

	/***************************************
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return TextConvert.lastElementOf(getClass().getName()) + "[" +
			   TextConvert.lastElementOf(rDataElement.getName()) +
			   "]";
	}

	/***************************************
	 * Updates the element component display from the data element value. Uses
	 * {@link #transferDataElementValueToComponent(DataElement, Component)} to
	 * display the new value. This method must be overridden by subclasses that
	 * provide additional component renderings.
	 *
	 * <p><b>Attention</b>: Invoking this method will replace any values entered
	 * into an input component with the model value stored in the data element.
	 * Therefore it should only be invoked on display UIs or when the resetting
	 * of input fields is explicitly desired.</p>
	 */
	public void update()
	{
		if (aElementComponent != null)
		{
			if (rDataElement.hasFlag(VALUE_CHANGED))
			{
				if (rDataElement.getValidator() instanceof HasValueList)
				{
					List<String> rValues =
						getListValues(aElementComponent.getContext(),
									  rDataElement);

					boolean bAllowedValuesChanged =
						rDataElement.hasFlag(ALLOWED_VALUES_CHANGED);

					if (aElementComponent instanceof ListControl)
					{
						updateList(rValues, bAllowedValuesChanged);
					}
					else if (aElementComponent instanceof ComboBox)
					{
						updateComboBox((ComboBox) aElementComponent,
									   rValues,
									   bAllowedValuesChanged);
					}
					else if (aElementComponent instanceof Container)
					{
						updateButtons(rValues, bAllowedValuesChanged);
					}
				}
				else if (aElementComponent instanceof TextComponent)
				{
					updateTextComponent((TextComponent) aElementComponent);
				}

				transferDataElementValueToComponent(rDataElement,
													aElementComponent);

				rDataElement.clearFlag(VALUE_CHANGED);
				rDataElement.clearFlag(ALLOWED_VALUES_CHANGED);

				String sInteractionUrl =
					rDataElement.getProperty(INTERACTION_URL, null);

				if (sInteractionUrl != null)
				{
					rDataElement.removeProperty(INTERACTION_URL);
					openUrl(sInteractionUrl, rDataElement.hasFlag(HIDDEN_URL));
				}
			}

			applyElementProperties();
			aElementComponent.applyStyle(applyElementStyle(rDataElement,
														   getBaseStyle()));
			enableComponent(bUIEnabled);
			aElementComponent.repaint();
		}
	}

	/***************************************
	 * Creates a checkbox to select an optional component.
	 *
	 * @param rBuilder The builder to add the component with
	 */
	protected void addOptionSelector(ContainerBuilder<?> rBuilder)
	{
		rOptionalCheckBox = rBuilder.addCheckBox(StyleData.DEFAULT, "", null);

		rOptionalCheckBox.setSelected(false);
		rOptionalCheckBox.addEventListener(EventType.ACTION,
			new EWTEventHandler()
			{
				@Override
				public void handleEvent(EWTEvent rEvent)
				{
					setEnabled(rOptionalCheckBox.isSelected());
				}
			});
	}

	/***************************************
	 * Applies certain element properties to the UI component(s) which are not
	 * reflected in the {@link StyleData} object for the component.
	 */
	protected void applyElementProperties()
	{
		boolean bVisible = !rDataElement.hasFlag(HIDDEN);

		if (aElementComponent != null)
		{
			sToolTip = rDataElement.getProperty(TOOLTIP, sHiddenLabelHint);

			aElementComponent.setVisible(bVisible);
			rPanelManager.setElementVisibility(this, bVisible);

			if (sToolTip != null && sToolTip.length() > 0)
			{
				aElementComponent.setToolTip(sToolTip);
			}
		}

		if (aElementLabel != null)
		{
			String sLabel =
				appendLabelSuffix(getElementLabelText(aElementLabel
													  .getContext()));

			aElementLabel.setProperties(sLabel);
			aElementLabel.setVisible(bVisible);
		}
	}

	/***************************************
	 * Builds the UI for a data element. Depending on the immutable state of the
	 * data element it invokes either {@link #createInputUI(ContainerBuilder,
	 * StyleData)} or {@link #createDisplayUI(ContainerBuilder, StyleData,
	 * DataElement)}.
	 *
	 * @param  rBuilder The container builder to create the components with
	 * @param  rStyle   The style data for display components
	 *
	 * @return
	 */
	protected Component buildDataElementUI(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle)
	{
		if (rDataElement.isImmutable())
		{
			aElementComponent = createDisplayUI(rBuilder, rStyle, rDataElement);
		}
		else
		{
			aElementComponent = createInputUI(rBuilder, rStyle, rDataElement);

			if (aElementComponent != null)
			{
				setupInteractionHandling(aElementComponent, true);
			}

			setEnabled(rOptionalCheckBox == null);
		}

		return aElementComponent;
	}

	/***************************************
	 * Checks if an error message is present for the data element of this
	 * instance.
	 *
	 * @param rElementErrors The mapping from data element names to error
	 *                       message (NULL for no errors)
	 */
	protected void checkElementError(Map<String, String> rElementErrors)
	{
		String sErrorMessage = null;

		if (rElementErrors != null)
		{
			sErrorMessage = rElementErrors.get(rDataElement.getName());

			if (sErrorMessage != null)
			{
				if (!sErrorMessage.startsWith("$"))
				{
					sErrorMessage = "$msg" + sErrorMessage;
				}
			}
		}

		setErrorMessage(sErrorMessage);
	}

	/***************************************
	 * A helper method that checks whether a certain validator contains a list
	 * of resource IDs.
	 *
	 * @param  rValidator The validator to check
	 *
	 * @return TRUE if the validator contains resource IDs
	 */
	protected boolean containsResourceIds(Validator<?> rValidator)
	{
		return rValidator instanceof StringListValidator &&
			   ((StringListValidator) rValidator).isResourceIds();
	}

	/***************************************
	 * Returns a string representation of a value that is related to a certain
	 * data element. Related means that it is either the data element value
	 * itself or of the element's validator. This method can be overridden by
	 * subclasses to modify the standard display of data elements. It will be
	 * invoked by the default implementations of the component creation methods.
	 *
	 * <p>The default implementation returns the toString() result for the value
	 * or an empty string if the value is NULL.</p>
	 *
	 * @param  rDataElement The data element to convert the value for
	 * @param  rValue       The value to convert
	 *
	 * @return The element display string
	 */
	protected String convertValueToString(
		DataElement<?> rDataElement,
		Object		   rValue)
	{
		String  sValue	    = "";
		boolean bImageValue = false;

		if (rValue instanceof Date)
		{
			sValue = formatDate(rDataElement, (Date) rValue);
		}
		else if (rValue instanceof BigDecimal)
		{
			String sFormat =
				rDataElement.getProperty(FORMAT,
										 BigDecimalDataElementUI.DEFAULT_FORMAT);

			sValue =
				NumberFormat.getFormat(sFormat).format((BigDecimal) rValue);
		}
		else if (rValue instanceof ListDataElement<?>)
		{
			sValue = ((ListDataElement<?>) rValue).getElements().toString();
		}
		else if (rValue instanceof DataElement<?>)
		{
			sValue =
				convertValueToString(rDataElement,
									 ((DataElement<?>) rValue).getValue());
		}
		else if (rValue instanceof Enum)
		{
			sValue	    = ((Enum<?>) rValue).name();
			sValue	    = getValueItemString(rDataElement, sValue);
			bImageValue = rDataElement.hasFlag(HAS_IMAGES);
		}
		else if (rValue != null)
		{
			sValue	    = rValue.toString();
			bImageValue = rDataElement.hasFlag(HAS_IMAGES);
		}

		if (containsResourceIds(rDataElement.getValidator()))
		{
			sValue = getValueItemString(rDataElement, sValue);
		}

		if (sValue.length() > 0 && bImageValue)
		{
			if (sValue.startsWith("data:"))
			{
				sValue = "#" + sValue;
			}
			else if (Component.COMPOUND_PROPERTY_CHARS.indexOf(sValue.charAt(0)) <
					 0)
			{
				sValue = "%" + sValue;
			}
		}

		return sValue;
	}

	/***************************************
	 * This method can be overridden by subclasses to create the user interface
	 * for a data element that is either immutable or for display only. The
	 * default implementation creates a label with the string-converted value of
	 * the data element as returned by {@link #convertValueToString(DataElement,
	 * Object)}.
	 *
	 * @param  rBuilder      The container builder to create the components with
	 * @param  rDisplayStyle The default style data for the display components
	 * @param  rDataElement  The data element to create the UI for
	 *
	 * @return The display user interface component
	 */
	protected Component createDisplayUI(ContainerBuilder<?> rBuilder,
										StyleData			rDisplayStyle,
										D					rDataElement)
	{
		int		    nRows		 = rDataElement.getIntProperty(ROWS, 1);
		int		    nCols		 = rDataElement.getIntProperty(COLUMNS, -1);
		ContentType eContentType = rDataElement.getProperty(CONTENT_TYPE, null);
		Object	    rValue		 = rDataElement.getValue();
		Component   aComponent;

		if (rDataElement instanceof ListDataElement<?>)
		{
			de.esoco.ewt.component.List aList = rBuilder.addList(rDisplayStyle);

			for (Object rItem : (ListDataElement<?>) rDataElement)
			{
				aList.add(convertValueToString(rDataElement, rItem));
			}

			aComponent = aList;
		}
		else if (eContentType == ContentType.WEBSITE)
		{
			aComponent = rBuilder.addWebsite(rDisplayStyle, rValue.toString());
		}
		else if (eContentType == ContentType.HYPERLINK)
		{
			aComponent =
				createHyperlinkDisplayComponent(rBuilder,
												rDisplayStyle,
												rDataElement);
		}
		else if (eContentType == ContentType.ABSOLUTE_URL ||
				 eContentType == ContentType.RELATIVE_URL)
		{
			aComponent =
				createUrlComponent(rBuilder, rDisplayStyle, eContentType);
		}
		else if (nRows != 1)
		{
			TextArea aTextArea =
				rBuilder.addTextArea(rDisplayStyle,
									 convertValueToString(rDataElement,
														  rDataElement));

			aComponent = aTextArea;

			aTextArea.setEditable(false);

			if (nRows > 0)
			{
				aTextArea.setRows(nRows);
			}

			if (nCols != -1)
			{
				aTextArea.setColumns(nCols);
			}
		}
		else
		{
			LabelStyle eLabelStyle =
				rDataElement.getProperty(LABEL_STYLE, null);

			if (eLabelStyle != null)
			{
				rDisplayStyle = rDisplayStyle.set(LABEL_STYLE, eLabelStyle);
			}

			Label aLabel =
				rBuilder.addLabel(rDisplayStyle,
								  convertValueToString(rDataElement,
													   rDataElement),
								  null);

			aComponent = aLabel;
		}

		return aComponent;
	}

	/***************************************
	 * Creates the standard input component to edit the value of a data element
	 * that has no list validator. Can be overridden by subclasses to modify the
	 * standard UI. The default implementation creates either an instance of
	 * {@link TextField} or, if the string representation of the element's value
	 * contains multiple lines, a {@link TextArea}.
	 *
	 * @param  rBuilder     The builder to add the input component with
	 * @param  rStyle       The style data for the component
	 * @param  rDataElement The data element to create the component for
	 *
	 * @return A new list component
	 */
	protected Component createEditComponent(ContainerBuilder<?> rBuilder,
											StyleData			rStyle,
											D					rDataElement)
	{
		String sValue = convertValueToString(rDataElement, rDataElement);

		ContentType eContentType = rDataElement.getProperty(CONTENT_TYPE, null);
		Component   aComponent;

		if (eContentType == ContentType.PHONE_NUMBER)
		{
			aComponent =
				createPhoneNumberInputComponent(rBuilder, rStyle, sValue);
		}
		else if (eContentType == ContentType.FILE_UPLOAD)
		{
			aComponent =
				rBuilder.addFileChooser(rStyle,
										rDataElement.getProperty(URL, null),
										"$btn" + rDataElement.getResourceId());
		}
		else if (eContentType == ContentType.WEBSITE)
		{
			aComponent =
				rBuilder.addWebsite(rStyle, rDataElement.getValue().toString());
		}
		else if (eContentType == ContentType.HYPERLINK)
		{
			aComponent =
				rBuilder.addLabel(rStyle.setFlags(StyleFlag.HYPERLINK),
								  rDataElement.getValue().toString(),
								  null);
		}
		else
		{
			aComponent =
				createTextInputComponent(rBuilder,
										 rStyle,
										 rDataElement,
										 sValue,
										 eContentType);
		}

		return aComponent;
	}

	/***************************************
	 * Creates a label for this instance's data element with the given container
	 * builder.
	 *
	 * @param rBuilder The container builder to add the label with
	 * @param rStyle   The default label style
	 */
	protected void createElementLabel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle)
	{
		String sLabel =
			appendLabelSuffix(getElementLabelText(rBuilder.getContext()));

		aElementLabel = rBuilder.addLabel(rStyle, sLabel, null);
	}

	/***************************************
	 * Creates a display component to render hyperlinks.
	 *
	 * @param  rBuilder     The container builder
	 * @param  rStyle       The style data
	 * @param  rDataElement The data element
	 *
	 * @return The new hyperlink component
	 */
	protected Component createHyperlinkDisplayComponent(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		D					rDataElement)
	{
		final String sURL   = rDataElement.getValue().toString();
		final String sTitle =
			rBuilder.getContext().expandResource(rDataElement.getResourceId());

		rStyle = rStyle.setFlags(StyleFlag.HYPERLINK);

		Component aComponent = rBuilder.addLabel(rStyle, sURL, null);

		aComponent.addEventListener(EventType.ACTION,
			new EWTEventHandler()
			{
				@Override
				public void handleEvent(EWTEvent rEvent)
				{
					Window.open(sURL, sTitle, "");
				}
			});

		return aComponent;
	}

	/***************************************
	 * Must be overridden by subclasses to modify the creation of the user
	 * interface components for the editing of the data element's value. The
	 * default implementation creates either a {@link TextField} with the value
	 * returned by {@link #convertValueToString(DataElement, Object)} or a list
	 * if the data element's value is constrained by a {@link ListValidator}.
	 * The list values will be read from the validator, converted with the above
	 * method, and finally expanded as resources. The creation of these
	 * components is handled by {@link #createEditComponent(ContainerBuilder,
	 * StyleData, DataElement)} and {@link
	 * #createSelectionComponent(ContainerBuilder, StyleData, DataElement,
	 * HasValueList)}, respectively.
	 *
	 * @param  rBuilder     The container builder to create the components with
	 * @param  rInputStyle  The default style data for the input components
	 * @param  rDataElement The data element to create the UI for
	 *
	 * @return The input user interface component or NULL if it shall not be
	 *         handled by this instance
	 */
	protected Component createInputUI(ContainerBuilder<?> rBuilder,
									  StyleData			  rInputStyle,
									  D					  rDataElement)
	{
		Component aComponent;

		if (rDataElement.getValidator() instanceof HasValueList<?>)
		{
			aComponent =
				createListComponent(rBuilder, rInputStyle, rDataElement);
		}
		else
		{
			aComponent =
				createEditComponent(rBuilder, rInputStyle, rDataElement);
		}

		return aComponent;
	}

	/***************************************
	 * Creates the interaction handler for this instances. Subclasses may
	 * override this method to return their own type of interaction handler.
	 *
	 * @param  rPanelManager
	 * @param  rDataElement
	 *
	 * @return The data element interaction handler
	 */
	protected DataElementInteractionHandler<D> createInteractionHandler(
		DataElementPanelManager rPanelManager,
		D						rDataElement)
	{
		return new DataElementInteractionHandler<D>(rPanelManager,
													rDataElement);
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
	protected de.esoco.ewt.component.List createList(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		D					rDataElement)
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
	protected Component createListButtonPanel(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		D					rDataElement,
		List<String>		rButtonLabels,
		ListStyle			eListStyle,
		int[]				rCurrentSelection)
	{
		int nColumns = rDataElement.getIntProperty(COLUMNS, 1);

		Layout eDisplayMode =
			rDataElement.getProperty(DataElementList.LAYOUT,
									 eButtonPanelDefaultLayout);

		String sAddStyle = rStyle.getProperty(WEB_ADDITIONAL_STYLES, "");

		sAddStyle += " " + CSS.gfButtonPanel();

		GenericLayout aPanelLayout =
			eDisplayMode == Layout.TABLE ? new GridLayout(nColumns)
												  : new FlowLayout();

		rBuilder =
			rBuilder.addPanel(rStyle.set(WEB_ADDITIONAL_STYLES, sAddStyle),
							  aPanelLayout);

		final List<Component> aButtons =
			createListButtons(rBuilder,
							  rDataElement,
							  rButtonLabels,
							  eListStyle,
							  rStyle.hasFlag(StyleFlag.MULTISELECT));

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
	 * @param  bMultiselect  TRUE for multi-selection, FALSE for
	 *                       single-selection
	 *
	 * @return A list containing the buttons that have been created
	 */
	protected List<Component> createListButtons(
		ContainerBuilder<?> rBuilder,
		D					rDataElement,
		List<String>		rButtonLabels,
		ListStyle			eListStyle,
		boolean				bMultiselect)
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

		String sDisabled = rDataElement.getProperty(DISABLED_ELEMENTS, "");

		StyleData rButtonStyle = StyleData.DEFAULT;
		int		  nValueIndex  = 0;

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
	protected ComboBox createListComboBox(ContainerBuilder<?> rBuilder,
										  StyleData			  rStyle,
										  D					  rDataElement,
										  List<String>		  rValues)
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
	protected Component createListComponent(ContainerBuilder<?> rBuilder,
											StyleData			rStyle,
											D					rDataElement)
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
	 * Creates a component for the input of a phone number.
	 *
	 * @param  rBuilder The builder to add the input component with
	 * @param  rStyle   The style data for the component
	 * @param  sValue   The initial value
	 *
	 * @return The new component
	 */
	protected Component createPhoneNumberInputComponent(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		String				sValue)
	{
		final List<TextField> aNumberFields  = new ArrayList<TextField>(4);
		String				  sPartSeparator = "+";

		EWTEventHandler rEventHandler =
			new EWTEventHandler()
			{
				@Override
				public void handleEvent(EWTEvent rEvent)
				{
					handlePhoneNumberEvent(rEvent, aNumberFields);
				}
			};

		rBuilder =
			rBuilder.addPanel(rStyle.setFlags(StyleFlag.HORIZONTAL_ALIGN_CENTER),
							  new FlowLayout(true));

		for (int i = 0; i < 4; i++)
		{
			if (i == 1)
			{
				sPartSeparator += " 0";
			}

			rBuilder.addLabel(StyleData.DEFAULT, sPartSeparator, null);

			TextField aField = rBuilder.addTextField(StyleData.DEFAULT, "");

			aField.addEventListener(EventType.KEY_PRESSED, rEventHandler);
			aField.addEventListener(EventType.KEY_TYPED, rEventHandler);
			aField.addEventListener(EventType.KEY_RELEASED, rEventHandler);
			aField.setColumns(PHONE_NUMBER_FIELD_SIZES[i]);
			aField.setToolTip(PHONE_NUMBER_FIELD_TOOLTIPS[i]);

			aNumberFields.add(aField);
			sPartSeparator = "-";
		}

		setPhoneNumber(aNumberFields, sValue);

		return rBuilder.getContainer();
	}

	/***************************************
	 * Creates a component for the input of a string value.
	 *
	 * @param  rBuilder     The builder to add the input component with
	 * @param  rStyle       The style data for the component
	 * @param  rDataElement The data element to create the component for
	 * @param  sValue       The initial value
	 * @param  eContentType
	 *
	 * @return The new component
	 */
	protected TextComponent createTextInputComponent(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyle,
		D					rDataElement,
		String				sValue,
		ContentType			eContentType)
	{
		int nRows = rDataElement.getIntProperty(ROWS, 1);

		TextComponent aTextComponent;

		if (nRows > 1 || nRows == -1)
		{
			aTextComponent = rBuilder.addTextArea(rStyle, sValue);

			if (nRows > 1)
			{
				((TextArea) aTextComponent).setRows(nRows);
			}
		}
		else
		{
			if (eContentType == ContentType.PASSWORD)
			{
				rStyle = rStyle.setFlags(StyleFlag.PASSWORD);
			}

			aTextComponent = rBuilder.addTextField(rStyle, sValue);
		}

		updateTextComponent(aTextComponent);

		return aTextComponent;
	}

	/***************************************
	 * Adds the component for the display of data elements with a URL content
	 * type.
	 *
	 * @param  rBuilder      The container builder to create the components with
	 * @param  rDisplayStyle The default style data for the display components
	 * @param  eContentType  TRUE for a relative and FALSE for an absolute URL
	 *
	 * @return
	 */
	protected Component createUrlComponent(ContainerBuilder<?> rBuilder,
										   StyleData		   rDisplayStyle,
										   ContentType		   eContentType)
	{
		String sText = "$btn" + rDataElement.getResourceId();

		if (rDataElement.hasFlag(HAS_IMAGES))
		{
			sText = "+" + sText;
		}

		Button aButton = rBuilder.addButton(rDisplayStyle, sText, null);

		aButton.addEventListener(EventType.ACTION,
			new EWTEventHandler()
			{
				@Override
				public void handleEvent(EWTEvent rEvent)
				{
					openUrl(rDataElement.getValue().toString(),
							rDataElement.getProperty(CONTENT_TYPE, null) ==
							ContentType.RELATIVE_URL);
				}
			});

		return aButton;
	}

	/***************************************
	 * Enables or disables the user interface component(s) of this instance. If
	 * the component is an instance of {@link Container} the enabled state of
	 * it's child elements will be changed instead.
	 *
	 * @param bEnable TRUE to enable the user interface
	 */
	protected void enableComponent(boolean bEnable)
	{
		enableComponent(aElementComponent,
						bEnable && !rDataElement.hasFlag(DISABLED));
	}

	/***************************************
	 * Enables or disables a certain component. If the component is an instance
	 * of {@link Container} the enabled state of it's child elements will be
	 * changed instead.
	 *
	 * @param rComponent The component to enable or disable
	 * @param bEnabled   TRUE to enable the user interface
	 */
	protected void enableComponent(Component rComponent, boolean bEnabled)
	{
		if (rComponent instanceof Container)
		{
			String sElements =
				rDataElement.getProperty(DISABLED_ELEMENTS, null);
			int    nIndex    = 0;

			for (Component rChild : ((Container) rComponent).getComponents())
			{
				if (sElements != null)
				{
					rChild.setEnabled(bEnabled &&
									  !sElements.contains("(" + nIndex++ + ")"));
				}
				else
				{
					rChild.setEnabled(bEnabled);
				}
			}
		}
		else if (rComponent != null)
		{
			rComponent.setEnabled(bEnabled);
		}
	}

	/***************************************
	 * Enables or disables interactions through this panel manager's user
	 * interface. This default implementation stores the current enabled state
	 * and then disables or restores the enabled state of the element component
	 * through the method {@link #enableComponent(boolean)}.
	 *
	 * @param bEnabled TRUE to enable interaction, FALSE to disable
	 */
	protected void enableInteraction(boolean bEnabled)
	{
		bEnabled = bEnabled || rDataElement.hasFlag(NO_INTERACTION_LOCK);

		bInteractionEnabled = bEnabled;

		if (bEnabled)
		{
			bEnabled = bUIEnabled;
		}

		enableComponent(bEnabled);
	}

	/***************************************
	 * Formats a date value from a data element. If the data element has the
	 * property {@link UserInterfaceProperties#FORMAT} this format string will
	 * be used to format the date value. Else a standard format will be used and
	 * if the property {@link UserInterfaceProperties#DATE_TIME} is set the
	 * resulting string will contain time and date values.
	 *
	 * @param  rDataElement The data element the value has been read from
	 * @param  rDate        The date value of the data element
	 *
	 * @return A string containing the formatted date value
	 */
	protected String formatDate(DataElement<?> rDataElement, Date rDate)
	{
		String		   sFormat = rDataElement.getProperty(FORMAT, null);
		DateTimeFormat rFormat;

		if (sFormat != null)
		{
			rFormat = DateTimeFormat.getFormat(sFormat);
		}
		else
		{
			ContentType eContentType =
				rDataElement.getProperty(CONTENT_TYPE, null);

			rFormat =
				DateTimeFormat.getFormat(eContentType == ContentType.DATE_TIME
										 ? PredefinedFormat.DATE_TIME_MEDIUM
										 : PredefinedFormat.DATE_MEDIUM);
		}

		return rFormat.format(rDate);
	}

	/***************************************
	 * Returns an array with the indices of the currently selected values. The
	 * returned array may be empty but will never be NULL. The index values will
	 * be sorted in ascending order.
	 *
	 * @param  rContext
	 * @param  rDataElement The data element to read the current values from
	 * @param  rAllValues   The list of all values to calculate the selection
	 *                      indexes from
	 *
	 * @return The indices of the currently selected values
	 */
	protected int[] getCurrentSelection(UserInterfaceContext rContext,
										D					 rDataElement,
										List<String>		 rAllValues)
	{
		List<?> rCurrentValues;
		int[]   aCurrentValueIndexes = null;
		int     i					 = 0;

		if (rDataElement instanceof ListDataElement)
		{
			rCurrentValues = ((ListDataElement<?>) rDataElement).getElements();
		}
		else
		{
			Object rValue = rDataElement.getValue();

			rCurrentValues =
				rValue != null ? Arrays.asList(rValue)
							   : Collections.emptyList();
		}

		aCurrentValueIndexes = new int[rCurrentValues.size()];

		for (Object rValue : rCurrentValues)
		{
			String sValue =
				rContext.expandResource(convertValueToString(rDataElement,
															 rValue));

			int nIndex = rAllValues.indexOf(sValue);

			if (nIndex >= 0)
			{
				aCurrentValueIndexes[i++] = nIndex;
			}
		}

		Arrays.sort(aCurrentValueIndexes);

		return aCurrentValueIndexes;
	}

	/***************************************
	 * Returns the display values for a list from a value list. If the list
	 * values are resource IDs they will be converted accordingly. If the data
	 * element has the flag {@link UserInterfaceProperties#SORT} set and the
	 * values are of the type {@link Comparable} the returned list will be
	 * sorted by their natural order.
	 *
	 * @param  rContext     The user interface context for resource expansion
	 * @param  rDataElement The data element
	 *
	 * @return The resulting list of display values
	 */
	protected List<String> getListValues(
		UserInterfaceContext rContext,
		D					 rDataElement)
	{
		Validator<?> rValidator = rDataElement.getValidator();

		List<?>		 rRawValues  = ((HasValueList<?>) rValidator).getValues();
		List<String> aListValues = new ArrayList<String>();

		for (Object rValue : rRawValues)
		{
			String sValue = convertValueToString(rDataElement, rValue);

			aListValues.add(rContext.expandResource(sValue));
		}

		if (rDataElement.hasFlag(UserInterfaceProperties.SORT) &&
			aListValues.size() > 1)
		{
			Collections.sort(aListValues);
		}

		return aListValues;
	}

	/***************************************
	 * Returns the enabled state of this UIs component(s).
	 *
	 * @return The current enabled state
	 */
	protected boolean isEnabled()
	{
		boolean bEnabled = false;

		if (aElementComponent instanceof Container)
		{
			List<Component> rComponents =
				((Container) aElementComponent).getComponents();

			if (rComponents.size() > 0)
			{
				bEnabled = rComponents.get(0).isEnabled();
			}
		}
		else if (aElementComponent != null)
		{
			bEnabled = aElementComponent.isEnabled();
		}

		return bEnabled;
	}

	/***************************************
	 * Opens the URL that is stored in the current data element.
	 *
	 * @param sUrl    The URL to open
	 * @param bHidden TRUE to open the URL in a hidden frame
	 */
	protected void openUrl(String sUrl, boolean bHidden)
	{
		if (bHidden)
		{
			EWT.openHiddenUrl(sUrl);
		}
		else
		{
			EWT.openUrl(sUrl, null, null);
		}
	}

	/***************************************
	 * Sets the enabled state of this UI's component(s) and stores the enabled
	 * state internally.
	 *
	 * @param bEnabled The new enabled state
	 */
	protected void setEnabled(boolean bEnabled)
	{
		bUIEnabled = bEnabled;

		if (bInteractionEnabled)
		{
			enableComponent(bEnabled);
		}
	}

	/***************************************
	 * Shows or hides an error message for an error of the data element value.
	 * The default implementation sets the message as the element component's
	 * tooltip.
	 *
	 * @param sMessage The error message or NULL to clear
	 */
	protected void setErrorMessage(String sMessage)
	{
		Widget rWidget = aElementComponent.getWidget();

		if (sMessage == null)
		{
			sMessage = sToolTip;
			rWidget.removeStyleName(CSS.error());

			if (aElementLabel != null)
			{
				aElementLabel.getWidget().removeStyleName(CSS.error());
			}
		}
		else
		{
			rWidget.addStyleName(CSS.error());

			if (aElementLabel != null)
			{
				aElementLabel.getWidget().addStyleName(CSS.error());
			}
		}

		aElementComponent.setToolTip(sMessage);
	}

	/***************************************
	 * Sets a hint for the component if the element label is not visible. The
	 * default implementation sets the label text as the component tooltip but
	 * subclasses can override this method.
	 *
	 * @param rContext The builder the element UI has been built with
	 */
	protected void setHiddenLabelHint(UserInterfaceContext rContext)
	{
		sHiddenLabelHint = getElementLabelText(rContext);

		if (sToolTip == null &&
			sHiddenLabelHint != null &&
			sHiddenLabelHint.length() > 0)
		{
			aElementComponent.setToolTip(sHiddenLabelHint);
		}
	}

	/***************************************
	 * Sets the values and the selection of a {@link ListControl} component.
	 *
	 * @param  rListControl      The component
	 * @param  rValues           The values
	 * @param  rCurrentSelection The current selection
	 *
	 * @return
	 */
	protected ListControl setListControlValues(ListControl  rListControl,
											   List<String> rValues,
											   int[]		rCurrentSelection)
	{
		for (String sValue : rValues)
		{
			rListControl.add(sValue);
		}

		if (rCurrentSelection != null)
		{
			rListControl.setSelection(rCurrentSelection);
		}

		return rListControl;
	}

	/***************************************
	 * Initializes the handling of interaction events for a certain component if
	 * necessary.
	 *
	 * @param rComponent           The component to setup the input handling for
	 * @param bOnContainerChildren TRUE to setup the input handling for the
	 *                             children if the component is a container
	 */
	protected void setupInteractionHandling(
		Component rComponent,
		boolean   bOnContainerChildren)
	{
		aInteractionHandler =
			createInteractionHandler(rPanelManager, rDataElement);

		aInteractionHandler.setupEventHandling(rComponent,
											   bOnContainerChildren);
	}

	/***************************************
	 * Transfers the value of a data element into a component. Must be
	 * overridden by subclasses that use special components to render data
	 * elements. This default implementation handles the standard components
	 * that are created by this base class.
	 *
	 * <p>This method will be invoked from the {@link #update()} method if the
	 * UI needs to be updated from the model data (i.e. from the data
	 * element).</p>
	 *
	 * @param rDataElement The data element to transfer the value of
	 * @param rComponent   The component to set the value of
	 */
	protected void transferDataElementValueToComponent(
		D		  rDataElement,
		Component rComponent)
	{
		ContentType eContentType = rDataElement.getProperty(CONTENT_TYPE, null);

		if (rComponent instanceof TextAttribute)
		{
			if (eContentType != ContentType.ABSOLUTE_URL &&
				eContentType != ContentType.RELATIVE_URL &&
				eContentType != ContentType.FILE_UPLOAD &&
				!(rComponent instanceof SelectableButton ||
				  rDataElement instanceof ListDataElement))
			{
				rComponent.setProperties(convertValueToString(rDataElement,
															  rDataElement));
			}
		}
		else if (eContentType == ContentType.PHONE_NUMBER)
		{
			Object rValue	    = rDataElement.getValue();
			String sPhoneNumber = rValue != null ? rValue.toString() : null;

			List<Component> rComponents   =
				((Panel) rComponent).getComponents();
			List<TextField> aNumberFields = new ArrayList<TextField>(4);

			for (int i = 1; i < 8; i += 2)
			{
				aNumberFields.add((TextField) rComponents.get(i));
			}

			setPhoneNumber(aNumberFields, sPhoneNumber);
		}
	}

	/***************************************
	 * Must be overridden by subclasses to modify the default transfer of input
	 * values from the user interface components to the data element. This
	 * method will only be invoked if this instance is in input mode and the
	 * data element is not immutable.
	 *
	 * <p>The default implementation expects either a text field or a list as
	 * created by {@link #createInputUI(ContainerBuilder, StyleData)}. In the
	 * first case it invokes the {@link DataElement#setStringValue(String)}
	 * method with the field value. For a list the selected value will be read
	 * from the element's list validator.</p>
	 *
	 * @param rComponent   The component to read the input from
	 * @param rDataElement The data element to set the value of
	 */
	protected void transferInputToDataElement(
		Component rComponent,
		D		  rDataElement)
	{
		if (rComponent instanceof ComboBox &&
			rDataElement instanceof ListDataElement)
		{
			Set<String> rValues = ((ComboBox) rComponent).getValues();

			@SuppressWarnings("unchecked")
			ListDataElement<String> rListDataElement =
				(ListDataElement<String>) rDataElement;

			// update validator to allow new values entered into the combo box
			((StringListValidator) rListDataElement.getValidator()).getValues()
																   .addAll(rValues);
			rListDataElement.clear();
			rListDataElement.addAll(rValues);
		}
		else if (rComponent instanceof FileChooser)
		{
			rDataElement.setStringValue(((FileChooser) rComponent)
										.getFilename());
		}
		else if (rComponent instanceof TextAttribute)
		{
			transferTextInput((TextAttribute) rComponent, rDataElement);
		}
		else if (rComponent instanceof ListControl)
		{
			setListSelection(rComponent, rDataElement);
		}
		else if (rComponent instanceof Panel)
		{
			if (rDataElement.getProperty(CONTENT_TYPE, null) ==
				ContentType.PHONE_NUMBER)
			{
				rDataElement.setStringValue(getPhoneNumber(rComponent));
			}
		}
		else
		{
			throw new UnsupportedOperationException("Cannot transfer input to " +
													rDataElement);
		}
	}

	/***************************************
	 * Transfers the input from a component with a text attribute to a data
	 * element.
	 *
	 * @param rComponent   The source text attribute component
	 * @param rDataElement The target data element
	 */
	protected void transferTextInput(TextAttribute rComponent, D rDataElement)
	{
		String sText = rComponent.getText();

		if (sText != null)
		{
			sText = sText.trim();
		}

		try
		{
			rDataElement.setStringValue(sText);
		}
		catch (Exception e)
		{
			// ignore parsing errors TODO: check if obsolete
		}

		if (rComponent instanceof TextComponent &&
			rDataElement.hasProperty(CARET_POSITION))
		{
			rDataElement.setProperty(CARET_POSITION,
									 ((TextComponent) rComponent)
									 .getCaretPosition());
		}
	}

	/***************************************
	 * Updates the state of a text component from the properties of the data
	 * element.
	 *
	 * @param rTextComponent The component to update
	 */
	protected void updateTextComponent(TextComponent rTextComponent)
	{
		String sConstraint  = rDataElement.getProperty(INPUT_CONSTRAINT, null);
		String sPlaceholder = rDataElement.getProperty(PLACEHOLDER, null);
		int    nColumns     = rDataElement.getIntProperty(COLUMNS, -1);
		int    nCaretPos    = rDataElement.getIntProperty(CARET_POSITION, -1);

		if (nColumns > 0)
		{
			rTextComponent.setColumns(nColumns);
		}

		if (nCaretPos >= 0)
		{
			rTextComponent.setCaretPosition(nCaretPos);
		}

		if (sConstraint != null)
		{
			rTextComponent.setInputConstraint(sConstraint);
		}

		if (sPlaceholder != null)
		{
			rTextComponent.setPlaceholder(sPlaceholder);
		}

		if (rDataElement.hasProperty(EDITABLE))
		{
			rTextComponent.setEditable(rDataElement.hasFlag(EDITABLE));
		}
	}

	/***************************************
	 * Will be invoked by the panel manager if a UI is removed.
	 */
	void dispose()
	{
	}

	/***************************************
	 * Handles an event in an input f4ield of a composite phone number UI.
	 *
	 * @param rEvent        The event that occurred
	 * @param rNumberFields The list of the input fields for the phone number
	 *                      parts
	 */
	void handlePhoneNumberEvent(EWTEvent rEvent, List<TextField> rNumberFields)
	{
		TextField    rField		   = (TextField) rEvent.getSource();
		String		 sText		   = rField.getText();
		String		 sSelectedText = rField.getSelectedText();
		KeyCode		 eKeyCode	   = rEvent.getKeyCode();
		ModifierKeys rModifiers    = rEvent.getModifiers();

		boolean bNoSelection =
			(sSelectedText == null || sSelectedText.length() == 0);

		if (rEvent.getType() == EventType.KEY_PRESSED)
		{
			sTemporaryTextStore = null;

			if (rModifiers == ModifierKeys.CTRL)
			{
				if (eKeyCode == KeyCode.C || eKeyCode == KeyCode.X)
				{
					if (bNoSelection)
					{
						// set field content to full text for cur or copy on release
						String sPhoneNumber =
							getPhoneNumber(rField.getParent());

						sTemporaryTextStore = sText;
						rField.setText(sPhoneNumber);
						rField.setSelection(0, sPhoneNumber.length());
					}
				}
			}
		}
		else if (rEvent.getType() == EventType.KEY_RELEASED)
		{
			if (rModifiers == ModifierKeys.CTRL)
			{
				switch (eKeyCode)
				{
					case X:
						if (sTemporaryTextStore != null)
						{
							setPhoneNumber(rNumberFields, "");
						}

						break;

					case C:
						if (sTemporaryTextStore != null)
						{
							// restore original text after cut or copy
							rField.setText(sTemporaryTextStore);
						}

						break;

					case V:
						setPhoneNumber(rNumberFields, sText);
						break;

					default:
						// ignore other
				}
			}
		}
		else if (rEvent.getType() == EventType.KEY_TYPED)
		{
			char nChar = rEvent.getKeyChar();

			if (nChar != 0)
			{
				if (Character.isDigit(nChar))
				{
					if (bNoSelection &&
						rNumberFields.indexOf(rField) == 0 &&
						sText.length() == 3)
					{
						rEvent.cancel();
					}
				}
				else if (rModifiers == ModifierKeys.NONE ||
						 rModifiers == ModifierKeys.SHIFT)
				{
					rEvent.cancel();
				}
			}
		}
	}

	/***************************************
	 * Initializes this instance for a certain parent and data element.
	 *
	 * @param rParent  The parent panel manager
	 * @param rElement The data element to be displayed
	 */
	void init(DataElementPanelManager rParent, D rElement)
	{
		this.rPanelManager = rParent;
		this.rDataElement  = rElement;
	}

	/***************************************
	 * Sets the value of the data element from the selection of a button in a
	 * discrete style list component.
	 *
	 * @param rAllButtons The list of all buttons
	 * @param rButton     The selected button
	 */
	void setButtonSelection(List<Component> rAllButtons, Button rButton)
	{
		boolean bSelected =
			rButton instanceof Selectable ? ((Selectable) rButton).isSelected()
										  : false;

		List<?> rValues =
			((HasValueList<?>) rDataElement.getValidator()).getValues();

		Object rButtonValue = rValues.get(rAllButtons.indexOf(rButton));

		setDataElementValueFromList(rButtonValue, bSelected);
	}

	/***************************************
	 * Package-internal method to update the data element of this instance.
	 *
	 * @param rNewElement    The new data element
	 * @param rElementErrors A mapping from data element names to error message
	 *                       (NULL for no errors)
	 * @param bUpdateUI      TRUE to also update the UI, FALSE to only update
	 *                       data element references
	 */
	@SuppressWarnings("unchecked")
	void updateDataElement(DataElement<?>	   rNewElement,
						   Map<String, String> rElementErrors,
						   boolean			   bUpdateUI)
	{
		rDataElement = (D) rNewElement;

		if (bUpdateUI)
		{
			update();
		}

		checkElementError(rElementErrors);
	}

	/***************************************
	 * Checks whether the {@link #LABEL_SUFFIX} needs to be appended to the
	 * given label.
	 *
	 * @param  sLabel The label text
	 *
	 * @return The label with the appended suffix if necessary
	 */
	private String appendLabelSuffix(String sLabel)
	{
		if (sLabel != null &&
			sLabel.length() > 0 &&
			"#%+".indexOf(sLabel.charAt(0)) == -1)
		{
			sLabel += LABEL_SUFFIX;
		}

		return sLabel;
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
	 * Returns the list style for a certain data element. If no explicit style
	 * is set a default will be determined from the value list size.
	 *
	 * @param  rDataElement The data element
	 * @param  rValues      The value list
	 *
	 * @return The list style
	 */
	private ListStyle getListStyle(D rDataElement, List<String> rValues)
	{
		ListStyle eListStyle =
			rValues.size() > 6 ? ListStyle.LIST : ListStyle.DROP_DOWN;

		eListStyle = rDataElement.getProperty(LIST_STYLE, eListStyle);

		return eListStyle;
	}

	/***************************************
	 * Collects the phone number string from a composite phone number field.
	 *
	 * @param  rComponent The parent component of the phone number input fields
	 *
	 * @return The phone number string
	 */
	private String getPhoneNumber(Component rComponent)
	{
		StringBuilder   aPhoneNumber = new StringBuilder();
		List<Component> rComponents  = ((Panel) rComponent).getComponents();
		char		    cSeparator   = '+';

		for (int i = 1; i < 8; i += 2)
		{
			TextField rNumberField = (TextField) rComponents.get(i);
			String    sPart		   = rNumberField.getText();

			if (i < 7 || sPart.length() > 0)
			{
				aPhoneNumber.append(cSeparator);
			}

			aPhoneNumber.append(sPart);

			cSeparator = i == 1 ? '.' : '-';
		}

		String sPhoneNumber = aPhoneNumber.toString();

		if (sPhoneNumber.equals("+.-"))
		{
			sPhoneNumber = "";
		}

		return sPhoneNumber;
	}

	/***************************************
	 * Sets a value from a list control to the data element of this instance.
	 *
	 * @param rValue The value to set
	 * @param bAdd   TRUE to add the value in a {@link ListDataElement}, FALSE
	 *               to remove
	 */
	@SuppressWarnings("unchecked")
	private void setDataElementValueFromList(Object rValue, boolean bAdd)
	{
		if (rDataElement instanceof ListDataElement)
		{
			ListDataElement<Object> rList =
				(ListDataElement<Object>) rDataElement;

			if (bAdd)
			{
				rList.addElement(rValue);
			}
			else
			{
				rList.removeElement(rValue);
			}
		}
		else
		{
			((DataElement<Object>) rDataElement).setValue(rValue);
		}
	}

	/***************************************
	 * Sets the selection for data elements that are based on a list validator.
	 *
	 * @param rComponent   The component to read the input from
	 * @param rDataElement The data element to set the value of
	 */
	private void setListSelection(Component rComponent, D rDataElement)
	{
		ListControl rList = (ListControl) rComponent;

		if (rDataElement instanceof SelectionDataElement)
		{
			String sSelection = Integer.toString(rList.getSelectionIndex());

			((SelectionDataElement) rDataElement).setValue(sSelection);
		}
		else if (rDataElement instanceof ListDataElement)
		{
			int[] rSelection = rList.getSelectionIndices();

			((ListDataElement<?>) rDataElement).clear();

			for (int nIndex : rSelection)
			{
				setListSelection(rList.getItem(nIndex), true);
			}
		}
		else
		{
			setListSelection(rList.getSelectedItem(), true);
		}
	}

	/***************************************
	 * Sets the selection for data elements that are based on a list validator.
	 *
	 * @param sSelection The string value of the selection to set
	 * @param bAdd       TRUE to add a value to a list data element, FALSE to
	 *                   remove
	 */
	@SuppressWarnings("unchecked")
	private void setListSelection(String sSelection, boolean bAdd)
	{
		UserInterfaceContext rContext = aElementComponent.getContext();

		List<?> rValues =
			((HasValueList<?>) rDataElement.getValidator()).getValues();

		if (sSelection == null)
		{
			if (!(rDataElement instanceof ListDataElement))
			{
				((DataElement<Object>) rDataElement).setValue(null);
			}
		}
		else
		{
			for (Object rValue : rValues)
			{
				String sValue =
					rContext.expandResource(convertValueToString(rDataElement,
																 rValue));

				if (sSelection.equals(sValue))
				{
					setDataElementValueFromList(rValue, bAdd);

					break;
				}
			}
		}
	}

	/***************************************
	 * Parses a phone number value and sets it into a set of input fields for
	 * the distinct number parts.
	 *
	 * @param aNumberFields The input fields for the number parts
	 * @param sNumber       The phone number value
	 */
	private void setPhoneNumber(
		final List<TextField> aNumberFields,
		String				  sNumber)
	{
		List<String> rNumberParts =
			StringDataElement.getPhoneNumberParts(sNumber);

		for (int i = 0; i < 4; i++)
		{
			aNumberFields.get(i).setText(rNumberParts.get(i));
		}
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
		Container				  rContainer = (Container) aElementComponent;
		List<? extends Component> rButtons   = rContainer.getComponents();

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

				boolean bMultiselect = rDataElement instanceof ListDataElement;

				ListStyle eListStyle =
					getListStyle(rDataElement, rButtonLabels);

				rContainer.clear();

				rButtons =
					createListButtons(aBuilder,
									  rDataElement,
									  rButtonLabels,
									  eListStyle,
									  bMultiselect);

				setupInteractionHandling(rContainer, true);
			}
		}

		int[] rCurrentSelection =
			getCurrentSelection(rContainer.getContext(),
								rDataElement,
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
		ListControl rListControl = (ListControl) aElementComponent;

		if (bListValuesChanged)
		{
			rListControl.removeAll();

			for (String sValue : rValues)
			{
				rListControl.add(sValue);
			}
		}

		rListControl.setSelection(getCurrentSelection(rListControl.getContext(),
													  rDataElement,
													  rValues));
	}
}
