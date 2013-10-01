package net.onrc.openvirtex.api.service.handlers.monitoring;

import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.MonitoringHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitchSerializer;
import net.onrc.openvirtex.elements.host.Host;
import net.onrc.openvirtex.elements.host.HostSerializer;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.exceptions.MissingRequiredField;
import net.onrc.openvirtex.util.MACAddress;
import net.onrc.openvirtex.util.MACAddressSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public class ListHosts extends ApiHandler<Map<String, Object>> {

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		String result;
		JSONRPC2Response resp = null;

		try {
			final Number tid = HandlerUtils.<Number> fetchField(
					MonitoringHandler.TENANT, params, true, null);
			final OVXNetwork vnet = OVXMap.getInstance().getVirtualNetwork(
					tid.intValue());
			if (vnet == null) {
				resp = new JSONRPC2Response(new JSONRPC2Error(
						JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
								+ ": Invalid tenantId : " + tid), 0);
			} else {
				// TODO: gson objects can be shared with other methods
				final GsonBuilder gsonBuilder = new GsonBuilder();
				// gsonBuilder.setPrettyPrinting();
				gsonBuilder.registerTypeAdapter(Host.class,
						new HostSerializer());
				final Gson gson = gsonBuilder.create();
				result = gson.toJson(vnet.getHosts());
				resp = new JSONRPC2Response(result, 0);
			}
			return resp;
		} catch (ClassCastException | MissingRequiredField e) {
			resp = new JSONRPC2Response(
					new JSONRPC2Error(JSONRPC2Error.INVALID_PARAMS.getCode(),
							this.cmdName() + ": Unable to fetch host list : "
									+ e.getMessage()), 0);
		}
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
