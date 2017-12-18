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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;


/********************************************************************
 * A GWT custom field serializer for the {@link ProcessState} class.
 *
 * @author eso
 */
public class ProcessState_CFS
{
	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Not used, implemented in {@link #instantiate(SerializationStreamReader)}
	 * instead.
	 *
	 * @param  rReader The stream reader to read the object data from
	 * @param  rObject The object to de-serialize
	 *
	 * @throws SerializationException If the stream access fails
	 */
	public static void deserialize(
		SerializationStreamReader rReader,
		ProcessState			  rObject) throws SerializationException
	{
	}

	/***************************************
	 * Restores the complete object hierarchy from a serialization stream.
	 *
	 * @param  rReader The stream reader to read the object data from
	 *
	 * @return The restored object
	 *
	 * @throws SerializationException If the stream access fails
	 */
	public static ProcessState instantiate(SerializationStreamReader rReader)
		throws SerializationException
	{
		ProcessState aState = (ProcessState) rReader.readObject();

//		aState.sName = rReader.readString();
//		int nCount = rReader.readInt();

		return aState;
	}

	/***************************************
	 * Writes the complete object hierarchy to a serialization stream.
	 *
	 * @param  rWriter The stream writer to write the object data to
	 * @param  rState  The object to serialize
	 *
	 * @throws SerializationException If the stream access fails
	 */
	public static void serialize(
		SerializationStreamWriter rWriter,
		ProcessState			  rState) throws SerializationException
	{
//		rWriter.writeString(rState.getName());
//		rWriter.writeInt(rState.getInteractionParams().size());

		rWriter.writeObject(rState);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
//	@Override
	public void deserializeInstance(
		SerializationStreamReader rStreamReader,
		ProcessState			  rInstance) throws SerializationException
	{
		deserialize(rStreamReader, rInstance);
	}

	/***************************************
	 * {@inheritDoc}
	 */
//	@Override
	public boolean hasCustomInstantiateInstance()
	{
		return true;
	}

	/***************************************
	 * {@inheritDoc}
	 */
//	@Override
	public ProcessState instantiateInstance(
		SerializationStreamReader rStreamReader) throws SerializationException
	{
		return instantiate(rStreamReader);
	}

	/***************************************
	 * {@inheritDoc}
	 */
//	@Override
	public void serializeInstance(
		SerializationStreamWriter rStreamWriter,
		ProcessState			  rInstance) throws SerializationException
	{
		serialize(rStreamWriter, rInstance);
	}
}
