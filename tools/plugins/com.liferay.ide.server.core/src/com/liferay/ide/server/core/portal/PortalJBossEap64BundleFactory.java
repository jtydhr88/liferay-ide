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

import com.liferay.ide.core.util.FileUtil;

import java.util.Map;

import org.eclipse.core.runtime.IPath;

/**
 * @author Simon Jiang
 */
public class PortalJBossEap64BundleFactory extends PortalJBossEapBundleFactory {

	@Override
	public PortalBundle create(IPath location)
	{

		return new PortalJBossEap64Bundle(location);
	}

	@Override
	public PortalBundle create(Map<String, String> appServerProperties)
	{

		return new PortalJBossEap64Bundle(appServerProperties);
	}

	@Override
	protected boolean detectBundleDir(IPath path)
	{

		if (FileUtil.notExists(path))
		{
			return false;
		}

		IPath bundlesPath = path.append("bundles");
		IPath modulesPath = path.append("modules");
		IPath standalonePath = path.append("standalone");
		IPath binPath = path.append("bin");

		if (FileUtil.exists(bundlesPath) && FileUtil.exists(modulesPath) &&
			FileUtil.exists(standalonePath) && FileUtil.exists(binPath))
		{
			String eapVersion = getEAPVersion(path.toFile(), _EAP_DIR_META_INF, new String[] {"6."}, "eap", "EAP");

			if (eapVersion != null) {
				return true;
			}

			return false;
		}

		return false;
	}

	private static final String _EAP_DIR_META_INF = "modules/system/layers/base/org/jboss/as/product/eap/dir/META-INF";

}