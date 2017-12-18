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
 * @author Ying Xu
 */
public class NewFragmentWizardMavenTests extends SwtbotBase {

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
		String projectName = "test-fragment-jsp-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/blogs_admin/configuration.jsp");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithJspf() {
		String projectName = "test-fragment-jspf-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		String[] files = {
			"META-INF/resources/blogs_admin/entry_search_columns.jspf",
			"META-INF/resources/blogs_admin/entry_search_results.jspf"
		};

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithoutFiles() {
		String projectName = "test-fragment-without-files-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.application.list.api");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		Assert.assertFalse(dialogAction.getConfirmBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithPortletProperites() {
		String projectName = "test-fragment-portlet-properties-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		String[] files = {"META-INF/resources/blogs_admin/configuration.jsp", "portlet.properties"};

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithResourceAction() {
		String projectName = "test-fragment-resource-action-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("resource-actions/default.xml");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createFragmentWithWholeFiles() {
		String projectName = "test-fragment-whole-files-maven";

		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepareMaven(projectName);

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.asset.categories.navigation.web");

		dialogAction.confirm();

		String[] files = {
			"META-INF/resources/configuration.jsp", "META-INF/resources/init-ext.jsp", "META-INF/resources/init.jsp",
			"META-INF/resources/view.jsp", "portlet.properties", "resource-actions/default.xml"
		};

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems(files);

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		Assert.assertFalse(dialogAction.getConfirmBtn().isEnabled());

		dialogAction.cancel();

		wizardAction.newFragmentInfo.selectFile("portlet.properties");

		wizardAction.newFragmentInfo.deleteFile();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("portlet.properties");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

	private static final String _serverName = "Liferay 7-fragment-maven";

}