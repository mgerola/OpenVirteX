package net.onrc.openvirtex.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.address.IPAddress;
import net.onrc.openvirtex.elements.address.OVXIPAddress;
import net.onrc.openvirtex.elements.datapath.OVXBigSwitch;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.elements.link.OVXLink;
import net.onrc.openvirtex.elements.link.PhysicalLink;
import net.onrc.openvirtex.elements.network.OVXNetwork;
import net.onrc.openvirtex.elements.network.PhysicalNetwork;
import net.onrc.openvirtex.elements.port.OVXPort;
import net.onrc.openvirtex.elements.port.PhysicalPort;
import net.onrc.openvirtex.routing.RoutingAlgorithms;
import net.onrc.openvirtex.util.MACAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class APITenantManager {

    Logger log = LogManager.getLogger(APITenantManager.class.getName());

    /**
     * Creates a new OVXNetwork object that is registered in the OVXMap.
     * 
     * @param protocol
     * @param controllerAddress
     *            The IP address for the controller which controls this
     *            virtualNetwork
     * @param controllerPort
     *            The port which the controller and OVX will communicate on
     * @param networkAddress
     *            The IP address the virtual network uses
     * @param mask
     *            The IP range is defined using the mask
     * 
     * @return tenantId
     */
    public Integer createOVXNetwork(final String protocol,
	    final String controllerAddress, final int controllerPort,
	    final String networkAddress, final short mask) {
	final IPAddress addr = new OVXIPAddress(networkAddress, -1); 
	final OVXNetwork virtualNetwork = new OVXNetwork(protocol, controllerAddress,
	        controllerPort, addr, mask);
	virtualNetwork.register();
	this.log.info("Created virtual network {}",
	        virtualNetwork.getTenantId());
	return virtualNetwork.getTenantId();
    }

    /**
     * createOVXSwitch create a new switch object given a set of
     * physical dpid. This switch object will either be an OVXSwitch or
     * a OVXBigSwitch.
     * 
     * @param tenantId
     *            The tenantId will specify which virtual network the switch
     *            belongs to
     * @param dpids
     *            The list of physicalSwitch dpids to specify what the
     *            virtualSwitch is composed of
     * @return dpid Return the DPID of the virtualSwitch which we have just
     *         created
     */
    public long createOVXSwitch(final int tenantId, final List<String> dpids) {
	// TODO: check for min 1 element in dpids
	final OVXMap map = OVXMap.getInstance();
	final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId);
	final List<Long> longDpids = new ArrayList<Long>();
	for (final String dpid : dpids) {
	    final long longDpid = Long.parseLong(dpid);
	    longDpids.add(longDpid);
	}
	final OVXSwitch ovxSwitch = virtualNetwork.createSwitch(longDpids);
	if (ovxSwitch == null) {
	    return -1;
	} else {
	    this.log.info("Created virtual switch {} in virtual network {}",
		    ovxSwitch.getSwitchId(), virtualNetwork.getTenantId());
	    return ovxSwitch.getSwitchId();
	}
    }

    /**
     * To add a Host we have to create an edgePort which the host can connect
     * to.
     * So we create a new Port object and set the edge attribute to be True.
     * 
     * @param tenantId
     *            The tenantId is the integer to specify which virtualNetwork
     *            this host should be added to
     * @param dpid
     *            specify the virtual dpid for which switch to attach the host
     *            to
     * @param port
     *            Specify which port on the virtualSwitch the host should be
     *            connected to
     * @return portNumber The portNumber is a short that represents the port of
     *         the edge switch which this edgePort is using
     */
    public short createEdgePort(final int tenantId, final long dpid,
	    final short port, final String mac) {
	final OVXMap map = OVXMap.getInstance();
	// TODO: check if tenantId exists
	final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId);
	final MACAddress macAddr = MACAddress.valueOf(mac);
	final OVXPort edgePort = virtualNetwork.createHost(dpid, port, macAddr);
	if (edgePort == null) {
	    return -1;
	} else {
	    this.log.info(
		    "Created edge port {} on virtual switch {} in virtual network {}",
		    edgePort.getPortNumber(), edgePort.getParentSwitch()
		            .getSwitchId(), virtualNetwork.getTenantId());
	    return edgePort.getPortNumber();
	}
    }

    /**
     * Takes a path of physicalLinks in a string and creates the virtualLink
     * based on this data. Each virtualLink consists of a set of PhysicalLinks
     * that are all continuous in the PhysicalNetwork topology.
     * 
     * @param tenantId
     *            Specify which virtualNetwork that the link is being created in
     * @param pathString
     *            The list of physicalLinks that make up the virtualLink
     * @return virtualLink the OVXLink object that is created using the
     *         PhysicalLinks
     */
    public Integer createOVXLink(final int tenantId, final String pathString) {
	final List<PhysicalLink> physicalLinks = new LinkedList<PhysicalLink>();
	for (final String hop : pathString.split(",")) {
	    final String srcString = hop.split("-")[0];
	    final String dstString = hop.split("-")[1];
	    final String[] srcDpidPort = srcString.split("/");
	    final String[] dstDpidPort = dstString.split("/");
	    final PhysicalPort srcPort = PhysicalNetwork.getInstance()
		    .getSwitch(Long.valueOf(srcDpidPort[0]))
		    .getPort(Short.valueOf(srcDpidPort[1]));
	    final PhysicalPort dstPort = PhysicalNetwork.getInstance()
		    .getSwitch(Long.valueOf(dstDpidPort[0]))
		    .getPort(Short.valueOf(dstDpidPort[1]));
	    final PhysicalLink link = PhysicalNetwork.getInstance().getLink(
		    srcPort, dstPort);
	    physicalLinks.add(link);
	}
	final OVXMap map = OVXMap.getInstance();
	final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId);
	final OVXLink virtualLink = virtualNetwork.createLink(physicalLinks);
	if (virtualLink == null) {
	    return -1;
	} else {
	    this.log.info("Created virtual link {} in virtual network {}", virtualLink.getLinkId(),
		    virtualNetwork.getTenantId());
	    return virtualLink.getLinkId();
	}
    }

    /**
     * Creates and starts the network which is specified by the given
     * tenant id.
     * 
     * @param tenantId
     *            A unique Integer which identifies each virtual network
     */
    public boolean bootNetwork(final int tenantId) {
	// initialize the virtualNetwork using the given tenantId
	final OVXMap map = OVXMap.getInstance();
	final OVXNetwork virtualNetwork = map.getVirtualNetwork(tenantId);
	this.log.info("Booted virtual network {}", virtualNetwork.getTenantId());
	return virtualNetwork.boot();
    }

    public String saveConfig() {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * Creates a single route between two edge ports of a big switch, as 
     * specified by a list of physical links. 
     * 
     * @param tenantId the virtual network ID 
     * @param dpid the DPID of the virtual switch
     * @param inPort the ingress port on the virtual switch
     * @param outPort the egress port of the virutal switch
     * @param routeString a string list of physical links joining the in/egress ports through the virtual switch
     * @return the route identifier or -1 for failed route creation
     */
    public int createOVXSwitchRoute(int tenantId, String dpid, String inPort, 
            String outPort, String routeString) {
	final OVXMap map = OVXMap.getInstance();
	final OVXNetwork virtNetwork = map.getVirtualNetwork(tenantId);
	final PhysicalNetwork phyNetwork = PhysicalNetwork.getInstance();
	final OVXSwitch virtSwitch = virtNetwork.getSwitch(Long.parseLong(dpid));
	
	//only try route setup if it's a BigSwitch
	if (virtSwitch instanceof OVXBigSwitch) {	    
	    final OVXBigSwitch bigSwitch = (OVXBigSwitch) virtSwitch;
	    
	    //only allow if algorithm is NONE
	    if (!bigSwitch.getAlg().equals(RoutingAlgorithms.NONE)) {
		return -1;
	    } 
	    final HashSet<PhysicalSwitch> switchSet = new HashSet<PhysicalSwitch>(
		    bigSwitch.getMap().getPhysicalSwitches(bigSwitch)); 
	    
	    //find ingress/egress virtual ports to Big Switch 
	    final OVXPort ingress = virtSwitch.getPort(Short.valueOf(inPort)); //Pair[0]));
	    final OVXPort egress = virtSwitch.getPort(Short.valueOf(outPort)); //Pair[0]));
	    
	    final List<PhysicalLink> pathLinks = new ArrayList<PhysicalLink>();
	    final List<PhysicalLink> reverseLinks = new ArrayList<PhysicalLink>();
	    
	    //handle route string 
	    for (final String link : routeString.split(",")) {
		final String srcString = link.split("-")[0];
		final String dstString = link.split("-")[1];
		final String[] srcDpidPort = srcString.split("/");
		final String[] dstDpidPort = dstString.split("/");
		final PhysicalSwitch srcSwitch = 
			phyNetwork.getSwitch(Long.parseLong(srcDpidPort[0])); 
		final PhysicalSwitch dstSwitch = 
			phyNetwork.getSwitch(Long.parseLong(dstDpidPort[0]));
		
		//if either source or dst switch don't exist, quit
		if ((srcSwitch == null) || (dstSwitch == null)) {
		    return -1;
		}
		
		//for each link, check if switch is part of big switch
		if ((switchSet.contains(srcSwitch)) && (switchSet.contains(dstSwitch))) {
		    final PhysicalPort srcPort = srcSwitch.getPort(Short.valueOf(srcDpidPort[1]));
		    final PhysicalPort dstPort = dstSwitch.getPort(Short.valueOf(dstDpidPort[1]));
		    if ((srcPort == null) || (dstPort == null)) {
			return -1;
		    }
		    final PhysicalLink hop = phyNetwork.getLink(srcPort, dstPort);
		    final PhysicalLink revhop = phyNetwork.getLink(dstPort, srcPort);
		    pathLinks.add(hop);
		    reverseLinks.add(revhop);
		} else {
		    return -1;
		}
	    }
	    Collections.reverse(reverseLinks);
	    return bigSwitch.createRoute(ingress, egress, pathLinks, reverseLinks);
	}
	return -1;
    }
}
