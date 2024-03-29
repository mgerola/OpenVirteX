/**
 * Copyright 2011, Big Switch Networks, Inc.
 * Originally created by David Erickson, Stanford University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 **/

package net.onrc.openvirtex.packet;

import java.nio.ByteBuffer;

/**
 * This class represents an Link Local Control header that is used in Ethernet
 * 802.3.
 * 
 * @author alexreimers
 * 
 */
public class LLC extends BasePacket {
	private byte dsap = 0;
	private byte ssap = 0;
	private byte ctrl = 0;

	public byte getDsap() {
		return this.dsap;
	}

	public void setDsap(final byte dsap) {
		this.dsap = dsap;
	}

	public byte getSsap() {
		return this.ssap;
	}

	public void setSsap(final byte ssap) {
		this.ssap = ssap;
	}

	public byte getCtrl() {
		return this.ctrl;
	}

	public void setCtrl(final byte ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public byte[] serialize() {
		final byte[] data = new byte[3];
		final ByteBuffer bb = ByteBuffer.wrap(data);
		bb.put(this.dsap);
		bb.put(this.ssap);
		bb.put(this.ctrl);
		return data;
	}

	@Override
	public IPacket deserialize(final byte[] data, final int offset,
			final int length) {
		final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
		this.dsap = bb.get();
		this.ssap = bb.get();
		this.ctrl = bb.get();
		return this;
	}
}
