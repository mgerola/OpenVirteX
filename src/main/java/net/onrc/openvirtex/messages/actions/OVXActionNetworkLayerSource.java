/**
 *  Copyright (c) 2013 Open Networking Laboratory
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 * 
 */

package net.onrc.openvirtex.messages.actions;

import java.util.List;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.address.PhysicalIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.exceptions.ActionVirtualizationDenied;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionNetworkLayerSource;

public class OVXActionNetworkLayerSource extends OFActionNetworkLayerSource 
			implements VirtualizableAction {

    private Logger log = 
	    LogManager.getLogger(OVXActionNetworkLayerSource.class.getName());

    
    @Override

    public void virtualize(OVXSwitch sw, List<OFAction> approvedActions, OFMatch match) throws ActionVirtualizationDenied {
	Mappable map = sw.getMap();
	OVXIPAddress vip = new OVXIPAddress(sw.getTenantId(), 
		this.networkAddress);
	PhysicalIPAddress pip = map.getPhysicalIP(vip, sw.getTenantId());
	if (pip == null) {
	    pip = new PhysicalIPAddress(map.getVirtualNetwork(sw.getTenantId()).nextIP());
	    log.debug("Adding IP mapping {} -> {} for tenant {} at switch {}", vip, pip, 
		    sw.getTenantId(), sw.getName());
	    map.addIP(pip, vip);
	}
	this.networkAddress = pip.getIp();
	approvedActions.add(this);
    }

}
