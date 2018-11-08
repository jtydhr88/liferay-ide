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

package com.liferay.ide.ui.module.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.support.project.ProjectSupport;
import com.liferay.ide.ui.liferay.support.workspace.LiferayWorkspaceGradleDownloadBundleSupport;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rui Wang
 */
public class WatchModuleLiferayWorkspaceGradleTomcatTests extends SwtbotBase {

	@ClassRule
	public static LiferayWorkspaceGradleDownloadBundleSupport liferayWorkspace =
		new LiferayWorkspaceGradleDownloadBundleSupport(bot);

	@Test
	public void watchPortlet() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(project.getName(), PORTLET);

		wizardAction.finish();

		viewAction.servers.start(liferayWorkspace.getWorkspaceName());

		jobAction.waitForServerStarted(liferayWorkspace.getName());

		viewAction.servers.visibleWorkspaceModuleTry(
			liferayWorkspace.getWorkspaceStartedName(), liferayWorkspace.getName(), project.getName());

		viewAction.servers.startWatchingProject(
			liferayWorkspace.getWorkspaceStartedName(), liferayWorkspace.getName(), project.getName());

		jobAction.waitForConsoleContent(
			liferayWorkspace.getName() + " [Liferay 7.x]", "STARTED " + project.getName() + "_", M1);

		viewAction.servers.stopWatchingProject(
			liferayWorkspace.getWorkspaceStartedName(), liferayWorkspace.getName(), project.getName() + " [watching]");

		viewAction.project.closeAndDelete(liferayWorkspace.getModuleFiles(project.getName()));
	}

	@Rule
	public ProjectSupport project = new ProjectSupport(bot);

}