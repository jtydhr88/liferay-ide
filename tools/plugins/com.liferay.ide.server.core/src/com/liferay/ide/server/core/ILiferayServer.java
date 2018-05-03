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

package com.liferay.ide.server.core;

import com.liferay.ide.core.util.StringPool;

import java.net.URL;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */
public interface ILiferayServer {

	public String getHost();

	public String getHttpPort();

	public String getId();

	public String getPassword();

	public URL getPluginContextURL(String context);

	public URL getPortalHomeUrl();

	public String getUsername();

	public URL getWebServicesListURL();

	public String ATTR_AGENT_PORT = "agent-port";

	public String ATTR_HTTP_PORT = "http-port";

	public String ATTR_JMX_PORT = "jmx-port";

	public String ATTR_PASSWORD = "password";

	public String ATTR_TELNET_PORT = "telnet-port";

	public String ATTR_USERNAME = "username";

	public IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(LiferayServerCore.PLUGIN_ID);

	public int DEFAULT_AGENT_PORT = defaultPrefs.getInt("default.agent.port", 29998);

	public String DEFAULT_HTTP_PORT = defaultPrefs.get("default.http.port", "8080");

	public int DEFAULT_JMX_PORT = defaultPrefs.getInt("default.jmx.port", 8099);

	public String DEFAULT_PASSWORD = defaultPrefs.get("default.password", StringPool.EMPTY);

	public int DEFAULT_TELNET_PORT = defaultPrefs.getInt("default.telnet.port", 11311);

	public String DEFAULT_USERNAME = defaultPrefs.get("default.username", StringPool.EMPTY);

}