package net.onrc.openvirtex.api.service.handlers.monitoring;

import java.util.Map;

import net.onrc.openvirtex.api.service.handlers.ApiHandler;
import net.onrc.openvirtex.api.service.handlers.HandlerUtils;
import net.onrc.openvirtex.api.service.handlers.MonitoringHandler;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitchSerializer;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.OVXPortSerializer;
import net.onrc.openvirtex.exceptions.MissingRequiredField;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParamsType;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * Get the virtual topology in json format
 * 
 * @return vitual topology in json format
 */
public class GetVirtualTopology extends ApiHandler<Map<String, Object>> {

	@Override
	public JSONRPC2Response process(final Map<String, Object> params) {
		String result;
		JSONRPC2Response resp = null;

		try {
			Number tid = HandlerUtils.<Number>fetchField(MonitoringHandler.TENANT, params, true, null);
			OVXNetwork vnet = OVXMap.getInstance().getVirtualNetwork(tid.intValue());
			// TODO: gson objects can be shared with other methods
			final GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setPrettyPrinting();
			gsonBuilder.excludeFieldsWithoutExposeAnnotation();
			gsonBuilder.registerTypeAdapter(OVXSwitch.class,
					new OVXSwitchSerializer());
			gsonBuilder.registerTypeAdapter(OVXPort.class,
					new OVXPortSerializer());
			final Gson gson = gsonBuilder.create();
			result = gson.toJson(vnet);
			resp = new JSONRPC2Response(result, 0);
			return resp;
		} catch (ClassCastException | MissingRequiredField e) {
			resp = new JSONRPC2Response(new JSONRPC2Error(
					JSONRPC2Error.INVALID_PARAMS.getCode(), this.cmdName()
							+ ": Unable to fetch virtual topology : "
							+ e.getMessage()), 0);
		}
		return resp;
	}

	@Override
	public JSONRPC2ParamsType getType() {
		return JSONRPC2ParamsType.OBJECT;
	}

}
