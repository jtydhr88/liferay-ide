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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vicky Wang
 * @author Sunny Shi
 * @author Rui Wang
 */
public class NewFragmentFilesWizardGradleTests extends SwtbotBase {

	@BeforeClass
	public static void init() throws IOException {
		
		envAction.unzipServer();
		
		String serverName = "Liferay 7-fragment-gradle";

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
	
	public void addFragmentProject(String projectName ,String bundleName) {

		wizardAction.openNewFragmentWizard();

		wizardAction.prepareFragmentGradle(projectName);

		wizardAction.next();

		wizardAction.openBrowseOsgiBundleDialog();

		dialogAction.prepareText(bundleName);

		dialogAction.confirm();

		wizardAction.finish();
		
	}

	@Test
	public void addFragmentJsp() {
		
		String projectName = "test-fragment-jsp";

		String bundleName = "com.liferay.asset.browser.web-1.0.17.jar";

		addFragmentProject(projectName, bundleName);

		wizardAction.openLiferayModuleFragmentFilesWizard();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/init-ext.jsp");

		dialogAction.confirm();

		wizardAction.finish();
		
		viewAction.closeProject(projectName);

		viewAction.deleteProject(projectName);
		
		}
	
	@Test
	public void addFragmentJspf() {
		String projectName = "test-fragment-jspf";
		
		String bundleName = "com.liferay.asset.categories.admin.web-1.0.19.jar";

		addFragmentProject(projectName ,bundleName);

		wizardAction.openLiferayModuleFragmentFilesWizard();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/edit_vocabulary_settings.jspf");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void addFragmentPortletProperties() {
		String projectName = "test-fragment-portlet";
		
		String bundleName = "com.liferay.announcements.web-1.1.9.jar";

		addFragmentProject(projectName ,bundleName);

		wizardAction.openLiferayModuleFragmentFilesWizard();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("portlet.properties");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.deleteProject(projectName);
	}

	@Test
	public void addFragmentResourceAction() {
		String projectName = "test-fragment-resource";
		
		String bundleName = "com.liferay.asset.publisher.web-1.5.3.jar";

		addFragmentProject(projectName ,bundleName);

		wizardAction.openLiferayModuleFragmentFilesWizard();

		wizardAction.openAddOverrideFilesDialog();

		dialogAction.selectItems("resource-actions/default.xml");

		dialogAction.confirm();

		wizardAction.finish();

		viewAction.deleteProject(projectName);
	}

	}
