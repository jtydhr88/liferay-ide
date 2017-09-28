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

package com.liferay.ide.ui.workspace.tests;

import com.liferay.ide.ui.liferay.Actions;
import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.swtbot.util.StringPool;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.swt.finder.SWTBotAssert;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sunny Shi
 * @author Terry Jia
 * @author Ashley Yuan
 */
public class ImportLiferayWorkspaceWizardGradleTests extends SwtbotBase {

	@After
	public void after() {
		viewAction.deleteProjectsExcludeNames("init-project");
	}

	@Test
	public void importLiferayWorkspace() throws IOException {
		String liferayWorkspaceName = "test-liferay-workspace-gradle";
		String gradlePropertyFileName = "gradle.properties";
		String settingGradleFileName = "settings.gradle";

		IPath testProject = envAction.getProjectsFolder().append(liferayWorkspaceName);

		File workspaceProject = envAction.prepareTempProject(testProject.toFile());

		wizardAction.openImportLiferayWorkspaceWizard();

		wizardAction.prepareImportLiferayWorkspace(workspaceProject.getPath());

		wizardAction.finishToWait();

		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "configs").isVisible());
		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "gradle").isVisible());

		viewAction.openProjectFile(liferayWorkspaceName, gradlePropertyFileName);

		SWTBotAssert.assertContains("liferay.workspace.modules.dir", editorAction.getContent());
		SWTBotAssert.assertContains("liferay.workspace.home.dir", editorAction.getContent());

		editorAction.close();

		viewAction.openProjectFile(liferayWorkspaceName, settingGradleFileName);

		SWTBotAssert.assertContains("buildscript", editorAction.getContent());
		SWTBotAssert.assertContains("repositories", editorAction.getContent());

		editorAction.close();

		viewAction.doActionOnProjectFile(Actions.getLiferayInitializeServerBundle(), liferayWorkspaceName);

		sleep(20000);
		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "bundles").isVisible());
	}

	@Test
	public void importLiferayWorkspaceWithDownloadLiferayBundle() throws IOException {
		String liferayWorkspaceName = "test-liferay-workspace-gradle";

		wizardAction.openImportLiferayWorkspaceWizard();

		IPath testProject = envAction.getProjectsFolder().append(liferayWorkspaceName);

		File workspaceProject = envAction.prepareTempProject(testProject.toFile());

		wizardAction.prepareImportLiferayWorkspace(workspaceProject.getPath(), true, StringPool.EMPTY);

		wizardAction.finishToWait();

		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "bundles").isVisible());

		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "configs").isVisible());
		Assert.assertTrue(viewAction.fetchProjectFile(liferayWorkspaceName, "gradle").isVisible());
	}

	@Test
	public void importLiferayWorkspaceWithPluginsSdk() {
	}

}