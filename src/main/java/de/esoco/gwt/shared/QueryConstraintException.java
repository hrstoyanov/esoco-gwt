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

/********************************************************************
 * An exception class that signals an invalid storage query constraint. The
 * exception message will be the name of the invalid constraint and the cause
 * will be initialized to the exception that signaled the error.
 *
 * @author eso
 */
public class QueryConstraintException extends Exception
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sConstraintName The name of the erroneous constraint
	 * @param eCause          The causing exception
	 */
	public QueryConstraintException(String sConstraintName, Exception eCause)
	{
		super(sConstraintName, eCause);
	}

	/***************************************
	 * Empty constructor for serialization.
	 */
	QueryConstraintException()
	{
	}
}
