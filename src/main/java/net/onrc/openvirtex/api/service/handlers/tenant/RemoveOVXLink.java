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
package net.onrc.openvirtex.api.service.handlers.tenant;

import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.TenantHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.exceptions.VirtualLinkException;

public class RemoveOVXLink extends ApiHandler<Map<String, Object>> {

    Logger log = LogManager.getLogger(RemoveOVXLink.class.getName());

    @Override
    public JSONRPC2Response process(final Map<String, Object> params) {
	JSONRPC2Response resp = null;

	try {
	    final Number tenantId = HandlerUtils.<Number> fetchField(
		    TenantHandler.TENANT, params, true, null);
	    final Number linkId = HandlerUtils.<Number> fetchField(
		    TenantHandler.LINK, params, true, null);

	    HandlerUtils.isValidTenantId(tenantId.intValue());
	    HandlerUtils.isValidLinkId(tenantId.intValue(), linkId.intValue());

	    final OVXMap map = OVXMap.getInstance();
	    final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
		    .intValue());
	    
	    LinkedList<OVXLink> linkList = virtualNetwork.getLinksById(linkId.intValue());
	    for (OVXLink link : linkList) {
		link.unregister();
	    }
	    //TODO: Karthik doesn't put a return code in the unregister functs... Add it
	    if (linkList == null) {
		resp = new JSONRPC2Response(-1, 0);
	    } else {
		this.log.info("Removed virtual link {} in virtual network {}",
			linkId, tenantId);
		resp = new JSONRPC2Response(1, 0);
	    }

	} catch (final MissingRequiredField e) {
	    resp = new JSONRPC2Response(new JSONRPC2Error(
		    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
		    + ": Unable to create virtual network : "
		    + e.getMessage()), 0);
	} catch (final VirtualLinkException e) {
	    resp = new JSONRPC2Response(new JSONRPC2Error(
		    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
		    + ": Invalid virtual link : " + e.getMessage()), 0);
	} catch (final InvalidTenantIdException e) {
	    resp = new JSONRPC2Response(new JSONRPC2Error(
		    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
		    + ": Invalid tenant id : " + e.getMessage()), 0);
	}

	return resp;
    }

    @Override
    public JSONRPC2ParamsType getType() {
	return JSONRPC2ParamsType.OBJECT;
    }

}
