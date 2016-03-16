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

import de.esoco.ewt.build.ContainerBuilder;
import de.esoco.ewt.component.Component;
import de.esoco.ewt.component.Panel;
import de.esoco.ewt.event.EWTEventHandler;
import de.esoco.ewt.event.EventType;
import de.esoco.ewt.style.StyleData;

import de.esoco.lib.property.UserInterfaceProperties.LabelStyle;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static de.esoco.lib.property.UserInterfaceProperties.HIDE_LABEL;
import static de.esoco.lib.property.UserInterfaceProperties.INITIAL_FOCUS;
import static de.esoco.lib.property.UserInterfaceProperties.LABEL_STYLE;


/********************************************************************
 * A data element panel manager implementation that manages a single data
 * element and it's UI.
 *
 * @author eso
 */
public class SingleDataElementManager extends DataElementPanelManager
{
	//~ Static fields/initializers ---------------------------------------------

	@SuppressWarnings("unused")
	private static final StyleData ELEMENT_LABEL_STYLE =
		addStyles(StyleData.DEFAULT, CSS.gfDataElementLabel()).set(LABEL_STYLE,
																   LabelStyle.INLINE);

	//~ Instance fields --------------------------------------------------------

	private DataElement<?>   rDataElement;
	private DataElementUI<?> aElementUI;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	public SingleDataElementManager(
		PanelManager<?, ?> rParent,
		DataElement<?>	   rDataElement)
	{
		super(rParent, rDataElement.getResourceId());

		this.rDataElement = rDataElement;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void addElementEventListener(
		EventType		rEventType,
		EWTEventHandler rListener)
	{
		aElementUI.addEventListener(rEventType, rListener);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void addElementEventListener(DataElement<?>  rDataElement,
										EventType		rEventType,
										EWTEventHandler rListener)
	{
		if (rDataElement == this.rDataElement)
		{
			aElementUI.addEventListener(rEventType, rListener);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void collectInput()
	{
		aElementUI.collectInput();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void enableInteraction(boolean bEnable)
	{
		aElementUI.enableInteraction(bEnable);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public DataElement<?> findDataElement(String sName)
	{
		return rDataElement.getName().equals(sName) ? rDataElement : null;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Component getContentComponent()
	{
		return aElementUI.getElementComponent();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public Collection<DataElement<?>> getDataElements()
	{
		return Arrays.<DataElement<?>>asList(rDataElement);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public DataElementUI<?> getDataElementUI(DataElement<?> rDataElement)
	{
		return aElementUI.getDataElement() == rDataElement ? aElementUI : null;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updateDataElements(List<DataElement<?>> rNewDataElements,
								   Map<String, String>  rErrorMessages,
								   boolean				bUpdateUI)
	{
		assert rNewDataElements.size() == 1;

		String		   sElementName    = rDataElement.getName();
		DataElement<?> rNewDataElement = rNewDataElements.get(0);

		if (rNewDataElement.getName().equals(sElementName))
		{
			rDataElement = rNewDataElement;
			aElementUI.updateDataElement(rDataElement,
										 rErrorMessages,
										 bUpdateUI);
		}
		else
		{
			assert false : "Replacing data element is not supported [" +
				   sElementName + "->" + rNewDataElement.getName() + "]";
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void updatePanel()
	{
		aElementUI.update();
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void addComponents()
	{
		aElementUI = DataElementUIFactory.create(this, rDataElement);

		ContainerBuilder<Panel> rBuilder = this;

		boolean   bHideLabel = rDataElement.hasFlag(HIDE_LABEL);
		String    sStyle     = aElementUI.getElementStyleName();
		StyleData aStyle     =
			addStyles(getBaseStyle(),
					  CSS.gfDataElement(),
					  CSS.gfSingleDataElement());

		if (rDataElement.isImmutable())
		{
			sStyle = CSS.readonly() + " " + sStyle;
		}

		aStyle = addStyles(aStyle, sStyle);

		if (!bHideLabel)
		{
			StyleData aElementLabelStyle =
				addStyles(ELEMENT_LABEL_STYLE,
						  aElementUI.getElementStyleName());

			aElementUI.createElementLabel(this, aElementLabelStyle);
		}

		aElementUI.buildUserInterface(rBuilder, aStyle);

		if (bHideLabel)
		{
			aElementUI.setHiddenLabelHint(getContext());
		}

		checkSelectionDependency(getRootDataElementPanelManager(),
								 rDataElement);

		if (rDataElement.hasFlag(INITIAL_FOCUS))
		{
			aElementUI.requestFocus();
		}
	}

	/***************************************
	 * @see DataElementPanelManager#createContainer(ContainerBuilder, StyleData)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected ContainerBuilder<Panel> createContainer(
		ContainerBuilder<?> rBuilder,
		StyleData			rStyleData)
	{
		return (ContainerBuilder<Panel>) rBuilder;
	}
}
