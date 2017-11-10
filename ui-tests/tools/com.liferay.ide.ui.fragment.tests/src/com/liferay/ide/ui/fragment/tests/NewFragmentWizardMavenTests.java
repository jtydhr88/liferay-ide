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

package com.liferay.ide.ui.fragment.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vicky Wang
 * @author Sunny Shi
 * @author Ying Xu
 */
public class NewFragmentWizardMavenTests extends SwtbotBase {

	@BeforeClass
	public static void init() throws IOException {
		envAction.unzipServer();

		String serverName = "Liferay 7-fragment-maven";

		dialogAction.openPreferencesDialog();

		dialogAction.openServerRuntimeEnvironmentsDialogTry();

		dialogAction.openNewRuntimeWizard();

		wizardAction.prepareLiferay7RuntimeType();

		wizardAction.next();

		IPath serverDir = envAction.getLiferayServerDir();

		IPath fullServerDir = serverDir.append(envAction.getLiferayPluginServerName());

		wizardAction.prepareLiferay7RuntimeInfo(serverName, fullServerDir.toOSString());

		wizardAction.finish();

		dialogAction.confirmPreferences();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.prepareNewServer(serverName);

		wizardAction.finish();
	}

	@Test
	public void createFragmentWithJsp() {
		String projectName = "test-fragment-jsp-files-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentMaven(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/blogs_admin/configuration.jsp");

		dialogAction.confirm();

		wizardAction.finishToWait();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void createFragmentWithoutOverrideFiles() {
		String projectName = "test-fragment-without-files-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentMaven(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.application.list.api");

		dialogAction.confirm();

		wizardAction.openAddOverrideFilesDialog();

		Assert.assertFalse(dialogAction.getOkBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.finishToWait();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void createFragmentWithPortletProperites() {
		String projectName = "test-fragment-portlet-properties-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentMaven(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		String[] files = {"META-INF/resources/blogs_admin/configuration.jsp", "portlet.properties"};

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.finishToWait();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void createFragmentWithResourceAction() {
		String projectName = "test-fragment-resource-action-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentMaven(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("resource-actions/default.xml");

		dialogAction.confirm();

		wizardAction.finishToWait();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void createFragmentWithWholeOverrideFiles() {
		String projectName = "test-fragment-whole-files-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentMaven(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.asset.categories.navigation.web");

		dialogAction.confirm();

		String[] files = {
			"META-INF/resources/configuration.jsp", "META-INF/resources/init-ext.jsp", "META-INF/resources/init.jsp",
			"META-INF/resources/view.jsp", "portlet.properties", "resource-actions/default.xml"
		};

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.openBrowseOsgiBundleDialog();

		Assert.assertFalse(dialogAction.getOkBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.selectOverridenFiles("portlet.properties");

		wizardAction.overriddenFilesDelete();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("portlet.properties");

		dialogAction.confirm();

		wizardAction.finishToWait();

		viewAction.deleteProject(projectName);
	}

}