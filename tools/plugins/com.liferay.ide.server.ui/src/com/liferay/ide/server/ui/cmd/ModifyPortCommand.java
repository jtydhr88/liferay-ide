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

package com.liferay.ide.server.ui.cmd;

import com.liferay.ide.server.core.LiferayServerCommand;
import com.liferay.ide.server.core.portal.IPortalBundleConfiguration;
import com.liferay.ide.server.core.portal.LiferayServerPort;
import com.liferay.ide.server.core.portal.PortalServerDelegate;

import java.util.Iterator;
import java.util.List;

/**
 * @author Simon Jiang
 */
public class ModifyPortCommand extends LiferayServerCommand {

	public ModifyPortCommand(
		IPortalBundleConfiguration bundleConfiguration, PortalServerDelegate server, String id, int port)
			{

		super(server, "");

		_serverDelgate = server;
		_bundleConfiguration = bundleConfiguration;
		this.id = id;
		this.port = port;
	}

	/**
	 * Execute the command.
	 */
	public void execute()
	{

		// find old port number

		List<LiferayServerPort> liferayServerPorts = _serverDelgate.getLiferayServerPorts();

		Iterator<LiferayServerPort> iterator = liferayServerPorts.iterator();

		while (iterator.hasNext())
		{
			LiferayServerPort temp = (LiferayServerPort)iterator.next();

			if (id.equals(temp.getId())) {
				oldPort = temp.getPort();
			}
		}

		// make the change

		_bundleConfiguration.modifyServerPort(id, port);
	}

	/**
	 * Undo the command.
	 */
	public void undo()
	{

		_bundleConfiguration.modifyServerPort(id, oldPort);
	}

	protected String id;
	protected int oldPort;
	protected int port;

	private IPortalBundleConfiguration _bundleConfiguration;
	private PortalServerDelegate _serverDelgate;

}