//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-gwt' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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
package de.esoco.gwt.server;

import de.esoco.data.element.DataElement;
import de.esoco.data.element.DataElementList;

import de.esoco.entity.Entity;

import de.esoco.gwt.shared.GwtApplicationService;
import de.esoco.gwt.shared.ProcessDescription;

import de.esoco.process.ProcessDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.obrel.core.RelationTypes;


/********************************************************************
 * Implementation of the service interface of a GWT application RPC.
 *
 * @author eso
 */
public abstract class GwtApplicationServiceImpl<E extends Entity>
	extends ProcessServiceImpl<E> implements GwtApplicationService
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * A NULL process definition reference to be used as a placeholder for
	 * separators in process listings.
	 */
	protected static final Class<? extends ProcessDefinition> PROCESS_SEPARATOR =
		null;

	static
	{
		RelationTypes.init(GwtApplicationServiceImpl.class);
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Converts an array of {@link ProcessDefinition} classes into a data
	 * element list that contains instances of {@link ProcessDescription}
	 * entries. {@link #PROCESS_SEPARATOR} entries will be mapped to a list
	 * separator process description.
	 *
	 * @param  sName             The name of the returned data element list
	 * @param  rProcessClasses   The array of process classes to convert
	 * @param  rAllowedProcesses The allowed processes for the current user
	 *
	 * @return The list of process descriptions
	 */
	protected static DataElementList createProcessList(
		String										   sName,
		Collection<Class<? extends ProcessDefinition>> rProcessClasses,
		Collection<Class<? extends ProcessDefinition>> rAllowedProcesses)
	{
		List<ProcessDescription> aDescriptions =
			new ArrayList<ProcessDescription>(rProcessClasses.size());

		for (Class<? extends ProcessDefinition> rProcessDef : rProcessClasses)
		{
			if (rProcessDef == PROCESS_SEPARATOR)
			{
				aDescriptions.add(ProcessDescription.createSeparator());
			}
			else if (rAllowedProcesses.contains(rProcessDef))
			{
				createProcessDescriptions(rProcessDef, aDescriptions);
			}
		}

		return new DataElementList(sName,
								   null,
								   aDescriptions,
								   EnumSet.of(DataElement.Flag.IMMUTABLE));
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Can be invoked by subclasses to set the main application process into the
	 * given user data element.
	 *
	 * @param rUserData   The user data
	 * @param rAppProcess The class of the main application process
	 */
	protected void setApplicationProcess(
		DataElementList					   rUserData,
		Class<? extends ProcessDefinition> rAppProcess)
	{
		{
			ProcessDescription aAppProcessDescription =
				createProcessDescriptions(rAppProcess, null);

			rUserData.setElement(new DataElementList(APPLICATION_PROCESS,
													 null,
													 aAppProcessDescription));
		}
	}
}
