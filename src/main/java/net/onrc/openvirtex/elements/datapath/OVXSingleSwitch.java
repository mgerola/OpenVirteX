/**
 * Copyright (c) 2013 Open Networking Laboratory
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package net.onrc.openvirtex.elements.datapath;

import java.util.Collections;

import net.onrc.openvirtex.core.io.OVXSendMsg;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.messages.Devirtualizable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMessage;

public class OVXSingleSwitch extends OVXSwitch {

	private static Logger log = LogManager.getLogger(OVXSingleSwitch.class
			.getName());

	public OVXSingleSwitch(final long switchId, final int tenantId) {
		super(switchId, tenantId);
	}

	@Override
	public boolean removePort(final Short portNumber) {
		if (!this.portMap.containsKey(portNumber)) {
			return false;
		} else {
			// TODO: this should generate a portstatus message to the ctrl
			this.portMap.remove(portNumber);
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.onrc.openvirtex.core.io.OVXSendMsg#sendMsg(org.openflow.protocol.
	 * OFMessage, net.onrc.openvirtex.core.io.OVXSendMsg)
	 */
	@Override
	public void sendMsg(final OFMessage msg, final OVXSendMsg from) {
		if (this.isConnected && this.isActive) {
			this.channel.write(Collections.singletonList(msg));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.onrc.openvirtex.elements.datapath.Switch#handleIO(org.openflow.protocol
	 * .OFMessage)
	 */
	@Override
	public void handleIO(final OFMessage msg) {
		try {
			((Devirtualizable) msg).devirtualize(this);
		} catch (final ClassCastException e) {
			OVXSingleSwitch.log.error("Received illegal message : " + msg);
		}
	}

	@Override
	// TODO: this is probably not optimal
	public void sendSouth(final OFMessage msg) {
		final PhysicalSwitch sw = this.map.getPhysicalSwitches(this).get(0);
		sw.sendMsg(msg, this);
	}

	@Override
	public int translate(final OFMessage ofm, final OVXPort inPort) {
		// get new xid from only PhysicalSwitch tied to this switch
		PhysicalSwitch psw;
		if (inPort == null) {
			psw = this.map.getPhysicalSwitches(this).get(0);
		} else {
			psw = inPort.getPhysicalPort().getParentSwitch();
		}
		return psw.translate(ofm, this);
	}
}
