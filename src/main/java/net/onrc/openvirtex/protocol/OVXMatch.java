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

package net.onrc.openvirtex.protocol;

import org.openflow.protocol.OFMatch;

/**
 * The Class OVXMatch. This class extends the OFMatch class, in order to carry some useful informations for OpenVirteX, 
 * as the cookie (used by flowMods messages) and the packet data (used by packetOut messages)
 */
public class OVXMatch extends OFMatch {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** The cookie. */
    protected long            cookie;
    
    /** The pkt data. */
    protected byte[]          pktData;

    /**
     * Instantiates a new void OVXatch.
     */
    public OVXMatch() {
	super();
	this.cookie = 0;
	this.pktData = null;
    }

    /**
     * Instantiates a new OVXmatch from an OFMatch instance.
     *
     * @param match the match
     */
    public OVXMatch(final OFMatch match) {
	this.wildcards = match.getWildcards();
	this.inputPort = match.getInputPort();
	this.dataLayerSource = match.getDataLayerSource();
	this.dataLayerDestination = match.getDataLayerDestination();
	this.dataLayerVirtualLan = match.getDataLayerVirtualLan();
	this.dataLayerVirtualLanPriorityCodePoint = match
	        .getDataLayerVirtualLanPriorityCodePoint();
	this.dataLayerType = match.getDataLayerType();
	this.networkTypeOfService = match.getNetworkTypeOfService();
	this.networkProtocol = match.getNetworkProtocol();
	this.networkSource = match.getNetworkSource();
	this.networkDestination = match.getNetworkDestination();
	this.transportSource = match.getTransportSource();
	this.transportDestination = match.getTransportDestination();
	this.cookie = 0;
	this.pktData = null;
    }

    /**
     * Get cookie.
     *
     * @return the cookie
     */
    public long getCookie() {
	return this.cookie;
    }

    /**
     * Set cookie.
     *
     * @param cookie the cookie
     * @return the oVX match
     */
    public OVXMatch setCookie(final long cookie) {
	this.cookie = cookie;
	return this;
    }

    /**
     * Gets the pkt data.
     *
     * @return the pkt data
     */
    public byte[] getPktData() {
	return this.pktData;
    }

    /**
     * Sets the pkt data.
     *
     * @param pktData the new pkt data
     */
    public void setPktData(final byte[] pktData) {
	this.pktData = pktData;
    }

    /**
     * Checks if this match belongs to a flow mod (e.g. the cookie is not zero).
     *
     * @return true, if is flow mod
     */
    public boolean isFlowMod() {
	return this.cookie != 0;
    }

    /**
     * Checks if this match belongs to a packet out (e.g. the packet data is not null).
     *
     * @return true, if is packet out
     */
    public boolean isPacketOut() {
	return this.pktData != null;
    }

}
