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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vicky Wang
 * @author Sunny Shi
 * @author Lily Li
 * @author Ying Xu
 */
public class NewFragmentWizardGradleTests extends SwtbotBase {

	@AfterClass
	public static void deleteRuntime() {
		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(_serverName);

		dialogAction.preferences.confirm();
	}

	@BeforeClass
	public static void init() throws IOException {
		envAction.unzipServer();

		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		IPath serverDir = envAction.getServerDir();

		IPath fullServerDir = serverDir.append(envAction.getServerName());

		wizardAction.newRuntime7.prepare(_serverName, fullServerDir.toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(_serverName);

		wizardAction.finish();
	}

	@Test
	public void createFragmentWithJsp() {
		String projectName = "test-fragment-jsp-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.announcements.");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/configuration.jsp");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithJspf() {
		String projectName = "test-fragment-jspf-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.document.library.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/document_library/cast_result.jspf");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithoutFiles() {
		String projectName = "test-fragment-without-files-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.hello.world.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		Assert.assertFalse(dialogAction.getConfirmBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithPortletProperites() {
		String projectName = "test-fragment-portlet-properties-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		String[] files = {
			"META-INF/resources/blogs_admin/configuration.jsp", "META-INF/resources/blogs_aggregator/init.jsp",
			"META-INF/resources/blogs/asset/abstract.jsp", "META-INF/resources/blogs/edit_entry.jsp",
			"portlet.properties"
		};

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithResourceAction() {
		String projectName = "test-fragment-resource-action-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.bookmarks.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("resource-actions/default.xml");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithWholeFiles() {
		String projectName = "test-fragment-whole-files-gradle";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareGradle(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.asset.tags.navigation.web");

		dialogAction.confirm();

		String[] files = {
			"META-INF/resources/configuration.jsp", "META-INF/resources/init-ext.jsp", "META-INF/resources/init.jsp",
			"META-INF/resources/view.jsp", "portlet.properties", "resource-actions/default.xml"
		};

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		Assert.assertFalse(dialogAction.getConfirmBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.newFragmentInfo.selectFile("META-INF/resources/configuration.jsp");

		wizardAction.newFragmentInfo.deleteFile();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/configuration.jsp");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	private static final String _serverName = "Liferay 7-fragment-gradle";

}