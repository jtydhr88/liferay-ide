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

package com.liferay.ide.ui.liferay.support.project;

import com.liferay.ide.ui.swtbot.page.Perspective;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.junit.BeforeClass;

/**
 * @author Lily Li
 */
public class UpgradeProjectSupport extends ProjectSupport {

	public UpgradeProjectSupport(SWTWorkbenchBot bot) {
		super(bot);
	}

	@BeforeClass
	public void before() {
		Perspective upgradePlannerPerspective = ide.getUpgradePlannerPerspective();

		upgradePlannerPerspective.activate();

		String tt = String.valueOf(System.currentTimeMillis());

		try {
			timestamp = Long.parseLong(tt.substring(6));
		}
		catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
	}

	public String getName() {
		return name + timestamp;
	}

	public String getPath() {
		return _project.getPath();
	}

	protected String name = "test";
	protected long timestamp = 0;

	private File _project;

}