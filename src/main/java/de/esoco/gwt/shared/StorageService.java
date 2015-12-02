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
package de.esoco.gwt.shared;

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.IntegerDataElement;
import de.esoco.data.element.QueryResultElement;
import de.esoco.data.element.StringDataElement;
import de.esoco.data.element.StringMapDataElement;

import de.esoco.lib.model.DataModel;
import de.esoco.lib.model.SearchableDataModel;
import de.esoco.lib.model.SortableDataModel.SortMode;


/********************************************************************
 * Base interface for services that are based on persistent objects from a
 * storage and/or for messages on a mail server.
 *
 * @author eso
 */
public interface StorageService extends AuthenticatedService
{
	//~ Static fields/initializers ---------------------------------------------

	/**
	 * The name of the {@link IntegerDataElement} that contains the start index
	 * for the {@link #QUERY} command.
	 */
	public static final String QUERY_START = "QueryStart";

	/**
	 * The name of the {@link IntegerDataElement} that contains the maximum
	 * number of elements to be queried by the {@link #QUERY} command.
	 */
	public static final String QUERY_LIMIT = "QueryLimit";

	/**
	 * The name of the {@link IntegerDataElement} that contains the query depth
	 * for the {@link #QUERY} command.
	 */
	public static final String QUERY_DEPTH = "QueryDepth";

	/**
	 * The name of the {@link StringMapDataElement} containing sort criteria.
	 */
	public static final String QUERY_SORT = "QuerySort";

	/**
	 * The name of the {@link StringMapDataElement} containing search
	 * constraints.
	 */
	public static final String QUERY_SEARCH = "QuerySearch";

	/**
	 * The name of the {@link StringDataElement} containing search constraints.
	 */
	public static final String DOWNLOAD_FILE_NAME = "DownloadFileName";

	/** An error token for exceptions to indicate a locked entity. */
	public static final String ERROR_ENTITY_LOCKED = "EntityLocked";

	/** An error token for exceptions to indicate the ID a locked entity. */
	public static final String ERROR_LOCKED_ENTITY_ID = "LockedEntityId";

	//- Commands ---------------------------------------------------------------

	/**
	 * A command that executes a query on a database storage or on a mail
	 * server. If the query yields less elements than defined by the given limit
	 * the resulting array will contain fewer elements than requested and may
	 * even be empty. The argument to the query command is a data element list
	 * named with the query key and containing the following data elements
	 * (elements with a default value are optional):
	 *
	 * <ul>
	 *   <li>{@link #QUERY_START}: The index of the first element to return
	 *     (zero-based)</li>
	 *   <li>{@link #QUERY_LIMIT}: The maximum number of elements to
	 *     return.</li>
	 *   <li>{@link #QUERY_DEPTH}: The maximum depth up to which child entities
	 *     shall be read (Default: 0).</li>
	 *   <li>{@link #QUERY_SORT}: Must be a {@link StringMapDataElement} that
	 *     contains a mapping from column IDs to the name of a {@link SortMode}
	 *     (Default: null, i.e. no specific sorting).</li>
	 *   <li>{@link #QUERY_SEARCH}: Must be a {@link StringMapDataElement} that
	 *     contains a mapping from column IDs to search constraints as defined
	 *     for the method {@link SearchableDataModel#getConstraint(String)}
	 *     (Default: null, i.e. no search constraints).</li>
	 * </ul>
	 *
	 * <p>This command returns a {@link QueryResultElement} that contains string
	 * {@link DataModel DataModels} that represent the queried rows. The string
	 * values in the row models contain the queried column values.</p>
	 */
	public static final Command<DataElementList,
								QueryResultElement<DataModel<String>>> QUERY =
		Command.newInstance("QUERY");

	/**
	 * A command to prepare the download of data for a certain storage query.
	 * The input argument is the exactly the same data element list as that of
	 * the {@value #QUERY} command. An additional string data element with the
	 * name {@link #DOWNLOAD_FILE_NAME} can be set to define the file name of
	 * the prepared download.
	 *
	 * <p>The command return value is a string data element containing the
	 * relative download URL for the generated data.</p>
	 */
	public static final Command<DataElementList, StringDataElement> PREPARE_DOWNLOAD =
		Command.newInstance("PREPARE_DOWNLOAD");
}
