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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vicky Wang
 * @author Sunny Shi
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

	public void createFragmentProject(){

		String projectName = "test-fragment-gradle";
		
		wizardAction.openNewFragmentWizard();
		
		wizardAction.prepareFragmentGradle(projectName);
		
		wizardAction.next();
		
		wizardAction.openBrowseOsgiBundleDialog();
		
		dialogAction.prepareText("com.liferay.announcements.web-1.1.9.");
		
		dialogAction.confirm();
		
		wizardAction.finishToWait();
	}
	
	@Test
	public void addFragmentJsp() {
		
		createFragmentProject();
		
		wizardAction.openNewModuleFragmentFilesWizard();
		
		wizardAction.openAddOverrideFilesDialog();
		
		dialogAction.selectItems("META-INF/resources/configuration.jsp");
		
		dialogAction.confirm();

		wizardAction.finishToWait();

		viewAction.deleteProject();
	
	}

	@Test
	public void addFragmentPortletProperties() {
	}

	@Test
	public void addFragmentResourceAction() {
	}

}