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

package com.liferay.ide.installer.tests.model;

import com.liferay.ide.installer.tests.util.InstallerUtil;

/**
 * @author Terry Jia
 */
public class LiferayProjectSdk extends Installer {

	public LiferayProjectSdk(String type) {
		super(type);
	}

	@Override
	public String command() {
		String command = "";

		if (isWindow()) {
			command = InstallerUtil.getProjectSdkFullNameWin();
		}
		else if (isLinux()) {
			command = InstallerUtil.getProjectSdkFullNameLinux();
		}
		else if (isMacos()) {
			// need more research to how to run installer on command line of Macos
		}

		command = command + " --mode unattended";

		return command;
	}
}
