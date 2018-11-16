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

package com.liferay.ide.ui.liferay.support.server;

import com.liferay.ide.ui.liferay.support.SupportBase;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import org.junit.Assert;

/**
 * @author Rui Wang
 */
public class ServerRunningLiferayWorkspaceSupport extends SupportBase {

	public ServerRunningLiferayWorkspaceSupport(SWTWorkbenchBot bot) {
		super(bot);
	}

	@Override
	public void after() {
		viewAction.servers.stop(getWorkspaceStartedName());

		jobAction.waitForServerStopped(getName());

		Assert.assertFalse("http://localhost:8080 still running", envAction.localConnected());

		dialogAction.deleteRuntimFromPreferences(getName());

		String[] modulesfile = {getName(), "modules"};

		viewAction.project.closeAndDelete(modulesfile);

		viewAction.project.closeAndDelete(getName());

		super.after();
	}

	@Override
	public void before() {
		super.before();

		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openGradleTry();

		dialogAction.gradle.checkAutomaticSync();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayWorkspaceWizard();

		wizardAction.newLiferayWorkspace.prepareGradle(getName(), "7.0");

		wizardAction.newLiferayWorkspace.selectDownloadLiferayBundle();

		wizardAction.finish();

		jobAction.waitForNoRunningJobs();

		Assert.assertTrue(viewAction.project.visibleFileTry(getName()));

		viewAction.servers.start(getWorkspaceStoppedName());
	}

	public String getName() {
		return name + timestamp;
	}

	public String getWorkspaceStartedName() {
		return name + timestamp + "  [Started]";
	}

	public String getWorkspaceStoppedName() {
		return name + timestamp + "  [Stopped]";
	}

	protected String name = "test";

}