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
package de.esoco.gwt.client.data;

import de.esoco.data.element.DataElementList;
import de.esoco.data.element.QueryResultElement;
import de.esoco.data.element.StringDataElement;
import de.esoco.data.element.StringMapDataElement;

import de.esoco.gwt.client.ServiceRegistry;
import de.esoco.gwt.shared.StorageService;

import de.esoco.lib.model.Callback;
import de.esoco.lib.model.DataModel;
import de.esoco.lib.model.Downloadable;
import de.esoco.lib.model.RemoteDataModel;
import de.esoco.lib.model.SearchableDataModel;
import de.esoco.lib.model.SortableDataModel;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;


/********************************************************************
 * A remote data model implementation that performs entity queries through the
 * {@link StorageService} interface.
 *
 * @author eso
 */
public class QueryDataModel implements RemoteDataModel<DataModel<String>>,
									   SortableDataModel<DataModel<String>>,
									   SearchableDataModel<DataModel<String>>,
									   Downloadable, Serializable
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Instance fields --------------------------------------------------------

	private String sQueryId;
	private int    nQuerySize;

	private transient int					  nWindowSize;
	private transient int					  nWindowStart;
	private transient List<DataModel<String>> aCurrentData;

	private transient StringMapDataElement aSortFields		  =
		new StringMapDataElement(StorageService.QUERY_SORT);
	private transient StringMapDataElement aSearchConstraints =
		new StringMapDataElement(StorageService.QUERY_SEARCH);

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance for a certain query. If the query size is not
	 * known it should be set to zero.
	 *
	 * @param sQueryId   The query ID
	 * @param nQuerySize The (initial) query size (zero if unknown)
	 */
	public QueryDataModel(String sQueryId, int nQuerySize)
	{
		this.sQueryId   = sQueryId;
		this.nQuerySize = nQuerySize;
	}

	/***************************************
	 * Default constructor for GWT serialization.
	 */
	QueryDataModel()
	{
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * @see RemoteDataModel#getAvailableElementCount()
	 */
	@Override
	public int getAvailableElementCount()
	{
		return aCurrentData.size();
	}

	/***************************************
	 * @see SearchableDataModel#getConstraint(String)
	 */
	@Override
	public String getConstraint(String sFieldId)
	{
		return aSearchConstraints.get(sFieldId);
	}

	/***************************************
	 * @see SearchableDataModel#getConstraints()
	 */
	@Override
	public Map<String, String> getConstraints()
	{
		return aSearchConstraints.getMap();
	}

	/***************************************
	 * @see DataModel#getElement(int)
	 */
	@Override
	public DataModel<String> getElement(int nIndex)
	{
		return aCurrentData.get(nIndex - nWindowStart);
	}

	/***************************************
	 * @see DataModel#getElementCount()
	 */
	@Override
	public int getElementCount()
	{
		return nQuerySize;
	}

	/***************************************
	 * Returns the query ID of this model.
	 *
	 * @return The query ID
	 */
	public final String getQueryId()
	{
		return sQueryId;
	}

	/***************************************
	 * @see SortableDataModel#getSortMode(String)
	 */
	@Override
	public SortMode getSortMode(String sFieldId)
	{
		String sSortMode = aSortFields.get(sFieldId);

		return sSortMode != null ? SortMode.valueOf(sSortMode) : null;
	}

	/***************************************
	 * @see RemoteDataModel#getWindowSize()
	 */
	@Override
	public int getWindowSize()
	{
		return nWindowSize;
	}

	/***************************************
	 * @see RemoteDataModel#getWindowStart()
	 */
	@Override
	public int getWindowStart()
	{
		return nWindowStart;
	}

	/***************************************
	 * Returns an iterator over the current data set that has been retrieved by
	 * the last call to {@link #setWindow(int, Callback)}.
	 *
	 * @see DataModel#iterator()
	 */
	@Override
	public Iterator<DataModel<String>> iterator()
	{
		return aCurrentData.iterator();
	}

	/***************************************
	 * The integer limit parameter defines the maximum number of rows to
	 * download.
	 *
	 * @see Downloadable#prepareDownload(String, int, Callback)
	 */
	@Override
	public void prepareDownload(String				   sFileName,
								int					   nMaxRows,
								final Callback<String> rCallback)
	{
		if (aCurrentData != null)
		{
			DataElementList aQueryData = createQueryData(0, nMaxRows);

			aQueryData.add(StorageService.DOWNLOAD_FILE_NAME, sFileName);

			ServiceRegistry.getStorageService()
						   .executeCommand(StorageService.PREPARE_DOWNLOAD,
										   aQueryData,
				new AsyncCallback<StringDataElement>()
				{
					@Override
					public void onFailure(Throwable e)
					{
						rCallback.onError(e);
					}

					@Override
					public void onSuccess(StringDataElement rDownloadUrl)
					{
						rCallback.onSuccess(rDownloadUrl.getValue());
					}
				});
		}
	}

	/***************************************
	 * @see SearchableDataModel#removeConstraints()
	 */
	@Override
	public void removeConstraints()
	{
		aSearchConstraints.clear();
	}

	/***************************************
	 * @see SortableDataModel#removeSorting()
	 */
	@Override
	public void removeSorting()
	{
		aSortFields.clear();
	}

	/***************************************
	 * Resets the query size to force a re-initialization.
	 */
	public void resetQuerySize()
	{
		nQuerySize = 0;
	}

	/***************************************
	 * @see SearchableDataModel#setConstraint(String, String)
	 */
	@Override
	public void setConstraint(String sFieldId, String sConstraint)
	{
		if (sConstraint != null)
		{
			aSearchConstraints.put(sFieldId, sConstraint);
		}
		else
		{
			aSearchConstraints.remove(sFieldId);
		}
	}

	/***************************************
	 * @see SearchableDataModel#setConstraints(Map)
	 */
	@Override
	public void setConstraints(Map<String, String> rConstraints)
	{
		aSearchConstraints.getMap().clear();
		aSearchConstraints.getMap().putAll(rConstraints);
	}

	/***************************************
	 * @see SortableDataModel#setSortMode(String, SortMode)
	 */
	@Override
	public void setSortMode(String sFieldId, SortMode rMode)
	{
		if (rMode != null)
		{
			aSortFields.put(sFieldId, rMode.name());
		}
		else
		{
			aSortFields.remove(sFieldId);
		}
	}

	/***************************************
	 * @see RemoteDataModel#setWindow(int, int, Callback)
	 */
	@Override
	public void setWindow(
		int												   nQueryStart,
		int												   nQueryLimit,
		final Callback<RemoteDataModel<DataModel<String>>> rCallback)
	{
		if (aCurrentData == null)
		{
			aCurrentData = new ArrayList<DataModel<String>>(nQueryLimit);
		}
		else if (nQueryLimit != nWindowSize)
		{
			aCurrentData.clear();
		}

		nWindowSize = nQueryLimit;

		int nWindowEnd = nWindowStart + aCurrentData.size();
		int nEnd	   = nQueryStart + nWindowSize;

		if (nQueryStart > nWindowStart && nQueryStart < nWindowEnd)
		{
			nQueryLimit -= nWindowEnd - nQueryStart;
			nQueryStart = nWindowEnd;
		}
		else if (nEnd >= nWindowStart && nEnd < nWindowEnd)
		{
			nQueryLimit -= nEnd - nWindowStart;
		}

		DataElementList aQueryData = createQueryData(nQueryStart, nQueryLimit);

		executeQuery(aQueryData, nQueryStart, nQueryLimit, rCallback);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + sQueryId + "]";
	}

	/***************************************
	 * Lets this model use the sort and search constraints of another query
	 * model instance.
	 *
	 * @param rOtherModel The other model
	 */
	public void useConstraints(QueryDataModel rOtherModel)
	{
		aSearchConstraints = rOtherModel.aSearchConstraints;
		aSortFields		   = rOtherModel.aSortFields;
	}

	/***************************************
	 * Create a data element list that contains the query data for this model's
	 * filter criteria.
	 *
	 * @param  nQueryStart The index of the first object to query
	 * @param  nQueryLimit The maximum number of objects to query
	 *
	 * @return
	 */
	private DataElementList createQueryData(int nQueryStart, int nQueryLimit)
	{
		DataElementList aQueryData = new DataElementList(sQueryId, null);

		aQueryData.add(StorageService.QUERY_START, nQueryStart);
		aQueryData.add(StorageService.QUERY_LIMIT, nQueryLimit);

		if (aSortFields.getMapSize() > 0)
		{
			aQueryData.addElement(aSortFields);
		}

		if (aSearchConstraints.getMapSize() > 0)
		{
			aQueryData.addElement(aSearchConstraints);
		}

		return aQueryData;
	}

	/***************************************
	 * Executes a query from {@link #prepareWindow(int, Callback)}.
	 *
	 * @param aQueryData The query data
	 * @param nStart     The start index for the query
	 * @param nCount     The number of data model elements to query
	 * @param rCallback  The callback to be invoked when the query is finished
	 */
	private void executeQuery(
		DataElementList									   aQueryData,
		final int										   nStart,
		final int										   nCount,
		final Callback<RemoteDataModel<DataModel<String>>> rCallback)
	{
		ServiceRegistry.getStorageService()
					   .executeCommand(StorageService.QUERY,
									   aQueryData,
			new AsyncCallback<QueryResultElement<DataModel<String>>>()
			{
				@Override
				public void onFailure(Throwable e)
				{
					rCallback.onError(e);
				}

				@Override
				public void onSuccess(
					QueryResultElement<DataModel<String>> rResult)
				{
					setCurrentData(rResult, nStart, nCount);

					rCallback.onSuccess(QueryDataModel.this);
				}
			});
	}

	/***************************************
	 * Sets the current data of this model by converting query data elements
	 * into data models.
	 *
	 * @param rQueryResult The data elements to convert
	 * @param nStart       The starting index of the new elements
	 * @param nCount
	 */
	private void setCurrentData(
		QueryResultElement<DataModel<String>> rQueryResult,
		int									  nStart,
		int									  nCount)
	{
		nQuerySize = rQueryResult.getQuerySize();

		if (nCount == nWindowSize ||
			nCount == nQuerySize ||
			nCount != rQueryResult.getElementCount())
		{
			aCurrentData.clear();
			nWindowStart = nStart;

			for (DataModel<String> rRow : rQueryResult)
			{
				aCurrentData.add(rRow);
			}
		}
		else if (nStart < nWindowStart)
		{
			int nLast   = nWindowSize - 1;
			int nInsert = 0;

			nWindowStart = nStart;

			for (DataModel<String> rRow : rQueryResult)
			{
				aCurrentData.remove(nLast);
				aCurrentData.add(nInsert++, rRow);
			}
		}
		else if (nStart > nWindowStart)
		{
			nWindowStart += nCount;

			for (DataModel<String> rRow : rQueryResult)
			{
				aCurrentData.remove(0);
				aCurrentData.add(rRow);
			}
		}
	}
}
