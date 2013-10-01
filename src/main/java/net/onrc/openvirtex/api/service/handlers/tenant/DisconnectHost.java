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
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.exceptions.InvalidTenantIdException;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.util.MACAddress;

public class DisconnectHost extends ApiHandler<Map<String, Object>> {

    Logger log = LogManager.getLogger(DisconnectHost.class.getName());

    @Override
    public JSONRPC2Response process(final Map<String, Object> params) {
	JSONRPC2Response resp = null;

	try {
	    final Number tenantId = HandlerUtils.<Number> fetchField(
		    TenantHandler.TENANT, params, true, null);
	    final Number dpid = HandlerUtils.<Number> fetchField(
		    TenantHandler.DPID, params, true, null);
	    final Number port = HandlerUtils.<Number> fetchField(
		    TenantHandler.PORT, params, true, null);
	    final String mac = HandlerUtils.<String> fetchField(
		    TenantHandler.MAC, params, true, null);

	    HandlerUtils.isValidTenantId(tenantId.intValue());
	    HandlerUtils.isValidEdgePort(tenantId.intValue(), dpid.longValue(),
		    port.shortValue());
	    final OVXMap map = OVXMap.getInstance();
	    final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId
		    .intValue());
	    final MACAddress macAddr = MACAddress.valueOf(mac);
	    final boolean result = virtualNetwork.removeHost(macAddr);
	    OVXPort virtualPort = virtualNetwork.getSwitch(dpid.longValue()).getPort(port.shortValue());
	    if (virtualPort == null) {
		resp = new JSONRPC2Response(-1, 0);
	    } else {
		virtualPort.unregister();
		this.log.info(
			"Removed edge port associated with mac address {} in virtual network {}",
			macAddr.toString(), virtualNetwork.getTenantId());
		resp = new JSONRPC2Response(1, 0);
	    }

	} catch (final MissingRequiredField e) {
	    resp = new JSONRPC2Response(new JSONRPC2Error(
		    JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
		    + ": Unable to create virtual network : "
		    + e.getMessage()), 0);
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