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

package com.liferay.ide.maven.ui.action;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.maven.core.ILiferayMavenConstants;

import org.osgi.framework.Version;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public class DistGoalAction extends MavenGoalAction {

	@Override
	protected String getMavenGoals() {
		if (plugin == null) {
			return "dist";
		}

		if (CoreUtil.compareVersions(new Version(plugin.getVersion()), new Version("2.0.2")) >= 0) {
			return "bundle-support:dist";
		}
		else {
			return "liferay:dist-bundle";
		}
	}

	@Override
	protected String getPluginKey() {
		return ILiferayMavenConstants.LIFERAY_MAVEN_PLUGINS_BUNDLE_SUPPORT_KEY;
	}
}