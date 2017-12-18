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
package de.esoco.data.process;

import de.esoco.data.element.DataWriter;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;


/********************************************************************
 * A wrapper for a GWT {@link SerializationStreamWriter} that also implements
 * the {@link DataWriter} interface. Exceptions that may occur during writing
 * will be wrapped into runtime exceptions.
 *
 * @author eso
 */
public class SerializationDataWriter implements DataWriter
{
	//~ Instance fields --------------------------------------------------------

	private final SerializationStreamWriter rWriter;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param rWriter
	 */
	public SerializationDataWriter(SerializationStreamWriter rWriter)
	{
		this.rWriter = rWriter;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeBoolean(boolean bValue)
	{
		try
		{
			rWriter.writeBoolean(bValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeByte(byte nValue)
	{
		try
		{
			rWriter.writeByte(nValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeChar(char cValue)
	{
		try
		{
			rWriter.writeChar(cValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeDouble(double fValue)
	{
		try
		{
			rWriter.writeDouble(fValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeFloat(float fValue)
	{
		try
		{
			rWriter.writeFloat(fValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeInt(int nValue)
	{
		try
		{
			rWriter.writeInt(nValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeLong(long nValue)
	{
		try
		{
			rWriter.writeLong(nValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeObject(Object rValue)
	{
		try
		{
			rWriter.writeObject(rValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeShort(short nValue)
	{
		try
		{
			rWriter.writeShort(nValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public void writeString(String sValue)
	{
		try
		{
			rWriter.writeString(sValue);
		}
		catch (SerializationException e)
		{
			throw new RuntimeException(e);
		}
	}
}
