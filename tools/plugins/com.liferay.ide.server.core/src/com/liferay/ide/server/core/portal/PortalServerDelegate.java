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

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.core.util.StringPool;
import com.liferay.ide.server.core.LiferayServerCore;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */
@SuppressWarnings("restriction")
public class PortalServerDelegate extends ServerDelegate implements PortalServerWorkingCopy {

	public PortalServerDelegate() {
	}

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		IStatus retval = Status.OK_STATUS;

		if (ListUtil.isNotEmpty(add)) {
			for (IModule module : add) {
				IModuleType moduleType = module.getModuleType();

				if (!_supportTypeList.contains(moduleType.getId())) {
					retval = LiferayServerCore.error("Unable to add module with type " + moduleType.getName());

					break;
				}
			}
		}

		return retval;
	}

	public int getAutoPublishTime() {
		return getAttribute(Server.PROP_AUTO_PUBLISH_TIME, 0);
	}

	@Override
	public IModule[] getChildModules(IModule[] module) {
		IModule[] retval = null;

		if (module != null) {
			IModuleType moduleType = module[0].getModuleType();

			if ((module.length == 1) && (moduleType != null) && _supportTypeList.contains(moduleType.getId())) {
				retval = new IModule[0];
			}
		}

		return retval;
	}

	@Override
	public boolean getDeveloperMode() {
		return getAttribute(PROPERTY_DEVELOPER_MODE, PortalServerConstants.DEFAULT_DEVELOPER_MODE);
	}

	public String getExternalProperties() {
		return getAttribute(PROPERTY_EXTERNAL_PROPERTIES, StringPool.EMPTY);
	}

	public String getHost() {
		return getServer().getHost();
	}

	@Override
	public String getHttpPort() {
		return getAttribute(ATTR_HTTP_PORT, DEFAULT_HTTP_PORT);
	}

	public String getId() {
		return getServer().getId();
	}

	@Override
	public boolean getLaunchSettings() {
		return getAttribute(PROPERTY_LAUNCH_SETTINGS, PortalServerConstants.DEFAULT_LAUNCH_SETTING);
	}

	public String[] getMemoryArgs() {
		String[] retval = new String[0];

		String args = getAttribute(PROPERTY_MEMORY_ARGS, PortalServerConstants.DEFAULT_MEMORY_ARGS);

		if (!CoreUtil.isNullOrEmpty(args)) {
			retval = args.split(" ");
		}

		return retval;
	}

	public String getPassword() {
		return getAttribute(ATTR_PASSWORD, DEFAULT_PASSWORD);
	}

	@Override
	public URL getPluginContextURL(String context) {
		try {
			return new URL(getPortalHomeUrl(), StringPool.FORWARD_SLASH + context);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public URL getPortalHomeUrl() {
		try {
			return new URL("http://localhost:" + getHttpPort());
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		IStatus status = canModifyModules(new IModule[] {module}, null);

		if ((status == null) || !status.isOK()) {
			throw new CoreException(status);
		}

		return new IModule[] {module};
	}

	public String getUsername() {
		return getAttribute(ATTR_USERNAME, DEFAULT_USERNAME);
	}

	@Override
	public URL getWebServicesListURL() {
		try {
			return new URL(getPortalHomeUrl(), "/tunnel-web/axis");
		}
		catch (MalformedURLException murle) {
			LiferayServerCore.logError("Unable to get web services list URL", murle);
		}

		return null;
	}

	@Override
	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		setAttribute(Server.PROP_AUTO_PUBLISH_TIME, getAutoPublishTime());
		ServerUtil.setServerDefaultName(getServerWorkingCopy());
	}

	@Override
	public void setDeveloperMode(boolean developerMode) {
		setAttribute(PROPERTY_DEVELOPER_MODE, developerMode);
	}

	public void setExternalProperties(String externalProperties) {
		setAttribute(PROPERTY_EXTERNAL_PROPERTIES, externalProperties);
	}

	public void setHttpPort(String httpPort) {
		setAttribute(ATTR_HTTP_PORT, httpPort);

		IRuntime rt = getServer().getRuntime();

		PortalRuntime runtime = (PortalRuntime)rt.loadAdapter(PortalRuntime.class, new NullProgressMonitor());

		PortalBundle portalRuntime = runtime.getPortalBundle();

		portalRuntime.setHttpPort(httpPort);
	}

	@Override
	public void setLaunchSettings(boolean launchSettings) {
		setAttribute(PROPERTY_LAUNCH_SETTINGS, launchSettings);
	}

	public void setMemoryArgs(String memoryArgs) {
		setAttribute(PROPERTY_MEMORY_ARGS, memoryArgs);
	}

	public void setPassword(String password) {
		setAttribute(ATTR_PASSWORD, password);
	}

	public void setUsername(String username) {
		setAttribute(ATTR_USERNAME, username);
	}

	private static final List<String> _supportTypeList = Arrays.asList("liferay.bundle", "jst.web", "jst.utility");

}