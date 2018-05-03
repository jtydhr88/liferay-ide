/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.server.core.portal;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import org.eclipse.wst.server.core.ServerPort;

/**
 * @author Simon Jiang
 */
public class LiferayServerPort extends ServerPort {

	public static String defaultStoreInProperties = "properties";
	public static String defaultStoreInServer = "server";
	public static String defaultStoreInXML = "xml";

	public LiferayServerPort(ServerPort port, String storeLocation) {
		this(port.getId(), port.getName(), port.getPort(), port.getProtocol(), storeLocation);
	}

	public LiferayServerPort(String id, String name, int port, String protocol) {
		super(id, name, port, protocol);

		setStoreLocation(defaultStoreInXML);
	}

	public LiferayServerPort(String id, String name, int port, String protocol, String storeLocation) {
		super(id, name, port, protocol);

		setStoreLocation(storeLocation);
	}

	@JsonCreator
	public LiferayServerPort(
		@JsonProperty("storeLocation") String storeLocation, @JsonProperty("name") String name,
		@JsonProperty("id") String id, @JsonProperty("protocol") String protocol, @JsonProperty("port") int port,
		@JsonProperty("contentTypes") String[] contentTypes, @JsonProperty("advanced") boolean advanced) {

		super(id, name, port, protocol, contentTypes, advanced);

		setStoreLocation(storeLocation);
	}

	public String getStoreLocation() {
		return _storeLocation;
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode() + getPort();
	}

	public void setStoreLocation(String storeLocation) {
		_storeLocation = storeLocation;
	}

	private String _storeLocation;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServerPort))
			return false;
		ServerPort sp = (ServerPort) obj;
		return (sp.getPort() == getPort() && sp.getName().equals(getName()));
	}
}