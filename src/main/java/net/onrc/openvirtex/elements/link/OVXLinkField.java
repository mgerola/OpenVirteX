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
package net.onrc.openvirtex.elements.link;

/**
 * The Enum OVXLinkField. Used to identify the field(s) used to encapsulate the
 * virtual link information inside the packet. Currently supported values are
 * mac addresses and vlan.
 */
public enum OVXLinkField {

    /** The mac address. */
    MAC_ADDRESS((byte) 0),

    /** The vlan. */
    VLAN((byte) 1);

    /** The value. */
    protected byte value;

    /**
     * Instantiates a new OVX link field.
     * 
     * @param value
     *            the value
     */
    private OVXLinkField(final byte value) {
	this.value = value;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public byte getValue() {
	return this.value;
    }
}
