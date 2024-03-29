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

package net.onrc.openvirtex.messages;

import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;
import net.onrc.openvirtex.exceptions.DroppedMessageException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.messages.actions.VirtualizableAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFError.OFFlowModFailedCode;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;

public class OVXFlowMod extends OFFlowMod implements Devirtualizable {

    private final Logger         log  = LogManager.getLogger(OVXFlowMod.class
	                                      .getName());

    private OVXSwitch            sw   = null;
    private final List<OFAction> approvedActions = new LinkedList<OFAction>();

    @Override
    public void devirtualize(final OVXSwitch sw) {
	
	this.sw = sw;
	
	int bufferId = OVXPacketOut.BUFFER_ID_NONE;
	if (sw.getFromBufferMap(this.bufferId)  != null) {
	    bufferId = sw.getFromBufferMap(this.bufferId).getBufferId();
	}
	final short inport = this.getMatch().getInputPort();

	for (final OFAction act : this.getActions()) {
	    try {
		((VirtualizableAction) act).virtualize(sw, approvedActions, this.match);
	    } catch (final ActionVirtualizationDenied e) {
		this.log.warn("Action {} could not be virtualized; error: {}",
		        act, e.getMessage());
		sw.sendMsg(OVXMessageUtil.makeError(e.getErrorCode(), this), sw);
		return;
	    } catch (DroppedMessageException e) {
		log.debug("Dropping flowmod {}", this);
		return;
	    }
	}
	
	


	final OVXPort ovxInPort = sw.getPort(inport);
	this.setBufferId(bufferId);
	
	if (ovxInPort == null) {
	    log.error("Unknown virtual port id {}; dropping flowmod {}", inport, this);
	    sw.sendMsg(OVXMessageUtil.makeErrorMsg(OFFlowModFailedCode.OFPFMFC_EPERM, this), sw);
	    return;
	} else {
	    OVXMessageUtil.translateXid(this, ovxInPort);
	    this.getMatch().setInputPort(ovxInPort.getPhysicalPortNumber());
	    if (ovxInPort.isEdge()) {
		this.prependRewriteActions();
		computeLength();
		if (sw instanceof OVXBigSwitch)
		    ((OVXBigSwitch) sw).sendSouthBS(this, ovxInPort);
		else
		    sw.sendSouth(this);
		return;
	    } else {
		this.rewriteMatch();
	    }
	}
	OVXMessageUtil.translateXid(this, ovxInPort);
	computeLength();
	if (sw instanceof OVXBigSwitch)
	    ((OVXBigSwitch) sw).sendSouthBS(this, ovxInPort);
	else
	    sw.sendSouth(this);

    }
    
    
    private void computeLength() {
	this.setActions(this.approvedActions);	
	this.setLengthU(OVXFlowMod.MINIMUM_LENGTH);
	for (final OFAction act : this.approvedActions) {
	    this.setLengthU(this.getLengthU() + act.getLengthU());
	}
    }



    private void prependRewriteActions() {
	final Mappable map = this.sw.getMap();

	if (!this.match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
	    final OVXIPAddress vip = new OVXIPAddress(this.sw.getTenantId(),
		    this.match.getNetworkSource());
	    PhysicalIPAddress pip = map.getPhysicalIP(vip,
		    this.sw.getTenantId());
	    if (pip == null) {
		pip = new PhysicalIPAddress(map.getVirtualNetwork(
		        this.sw.getTenantId()).nextIP());
		this.log.debug(
		        "Adding IP mapping {} -> {} for tenant {} at switch {}",
		        vip, pip, this.sw.getTenantId(), this.sw.getName());
		map.addIP(pip, vip);
	    }
	    final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource();
	    srcAct.setNetworkAddress(pip.getIp());
	    this.approvedActions.add(0,srcAct);

	}

	if (!this.match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
	    final OVXIPAddress vip = new OVXIPAddress(this.sw.getTenantId(),
		    this.match.getNetworkDestination());
	    PhysicalIPAddress pip = map.getPhysicalIP(vip,
		    this.sw.getTenantId());
	    if (pip == null) {
		pip = new PhysicalIPAddress(map.getVirtualNetwork(
		        this.sw.getTenantId()).nextIP());
		this.log.debug(
		        "Adding IP mapping {} -> {} for tenant {} at switch {}",
		        vip, pip, this.sw.getTenantId(), this.sw.getName());
		map.addIP(pip, vip);
	    }
	    final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination();
	    dstAct.setNetworkAddress(pip.getIp());
	    this.approvedActions.add(0,dstAct);

	}
    }

    private void rewriteMatch() {
	final Mappable map = this.sw.getMap();

	// TODO: handle IP ranges
	if (!this.match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
	    final OVXIPAddress vip = new OVXIPAddress(this.sw.getTenantId(),
		    this.match.getNetworkSource());
	    PhysicalIPAddress pip = map.getPhysicalIP(vip,
		    this.sw.getTenantId());
	    if (pip == null) {
		pip = new PhysicalIPAddress(map.getVirtualNetwork(
		        this.sw.getTenantId()).nextIP());
		this.log.debug(
		        "Adding IP mapping {} -> {} for tenant {} at switch {}",
		        vip, pip, this.sw.getTenantId(), this.sw.getName());
		map.addIP(pip, vip);
	    }
	    this.getMatch().setNetworkSource(pip.getIp());
	}

	if (!this.match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
	    final OVXIPAddress vip = new OVXIPAddress(this.sw.getTenantId(),
		    this.match.getNetworkDestination());
	    PhysicalIPAddress pip = map.getPhysicalIP(vip,
		    this.sw.getTenantId());
	    if (pip == null) {
		pip = new PhysicalIPAddress(map.getVirtualNetwork(
		        this.sw.getTenantId()).nextIP());
		this.log.debug(
		        "Adding IP mapping {} -> {} for tenant {} at switch {}",
		        vip, pip, this.sw.getTenantId(), this.sw.getName());
		map.addIP(pip, vip);
	    }
	    this.getMatch().setNetworkDestination(pip.getIp());
	}

    }

}
