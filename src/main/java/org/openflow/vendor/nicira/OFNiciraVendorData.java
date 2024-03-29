/**
 *    Copyright 2011, Big Switch Networks, Inc. 
 *    Originally created by David Erickson & Rob Sherwood, Stanford University
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package org.openflow.vendor.nicira;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.vendor.OFVendorData;

/**
 * Base class for vendor data corresponding to a Nicira vendor extension. Nicira
 * vendor data always starts with a 4-byte integer data type value.
 * 
 * @author Rob Vaterlaus (rob.vaterlaus@bigswitch.com)
 */
public class OFNiciraVendorData implements OFVendorData {

	public static final int NX_VENDOR_ID = 0x00002320;
	/**
	 * The value of the integer data type at the beginning of the vendor data
	 */
	protected int dataType;

	/**
	 * Construct empty (i.e. unspecified data type) Nicira vendor data.
	 */
	public OFNiciraVendorData() {
	}

	/**
	 * Contruct Nicira vendor data with the specified data type
	 * 
	 * @param dataType
	 *            the data type value at the beginning of the vendor data.
	 */
	public OFNiciraVendorData(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * Get the data type value at the beginning of the vendor data
	 * 
	 * @return the integer data type value
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * Set the data type value
	 * 
	 * @param dataType
	 *            the integer data type value at the beginning of the vendor
	 *            data.
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * Get the length of the vendor data. This implementation will normally be
	 * the superclass for another class that will override this to return the
	 * overall vendor data length. This implementation just returns the length
	 * of the part that includes the 4-byte integer data type value at the
	 * beginning of the vendor data.
	 */
	@Override
	public int getLength() {
		return 4;
	}

	/**
	 * Read the vendor data from the ChannelBuffer
	 * 
	 * @param data
	 *            the channel buffer from which we're deserializing
	 * @param length
	 *            the length to the end of the enclosing message
	 */
	@Override
	public void readFrom(ChannelBuffer data, int length) {
		dataType = data.readInt();
	}

	/**
	 * Write the vendor data to the ChannelBuffer
	 * 
	 * @param data
	 *            the channel buffer to which we're serializing
	 */
	@Override
	public void writeTo(ChannelBuffer data) {
		data.writeInt(dataType);
	}
}
