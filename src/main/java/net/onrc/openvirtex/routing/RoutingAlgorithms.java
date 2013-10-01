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
package net.onrc.openvirtex.routing;

public enum RoutingAlgorithms {
	NONE((short) 0), SFP((short) 1);

	protected short value;
	/** the routable */
	protected Routable routing;

	/** the types of routable mapping to each */
	static Routable[] routingmap;

	private RoutingAlgorithms(final short value) {
		this.value = value;
		RoutingAlgorithms.setRoutable(this.value, this);
	}

	private static void setRoutable(final Short value,
			final RoutingAlgorithms algo) {
		if (RoutingAlgorithms.routingmap == null) {
			RoutingAlgorithms.routingmap = new Routable[2];
			RoutingAlgorithms.routingmap[0] = new ManualRoute();
			RoutingAlgorithms.routingmap[1] = new ShortestPath();
		}
		algo.routing = RoutingAlgorithms.routingmap[value];
	}

	/**
	 * @return the value
	 */
	public short getValue() {
		return this.value;
	}

	/**
	 * @return the Routable associated with the algorithm
	 */
	public Routable getRoutable() {
		return this.routing;
	}

}
