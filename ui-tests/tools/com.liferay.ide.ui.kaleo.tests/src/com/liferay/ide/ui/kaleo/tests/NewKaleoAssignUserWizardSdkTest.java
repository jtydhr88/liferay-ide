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

package com.liferay.ide.ui.kaleo.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.base.SdkSupport;

import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Haoyi Sun
 */
public class NewKaleoAssignUserWizardSdkTest extends SwtbotBase {

	@ClassRule
	public static SdkSupport sdk = new SdkSupport(bot);

	@Test
	public void createKaleoWorkflowOnProject() {
		viewAction.switchLiferayPerspective();

		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-kaleo-workflow-portlet";

		wizardAction.newPlugin.preparePortletSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		viewAction.switchKaleoDesignerPerspective();

		wizardAction.openNewLiferayKaleoWorkflowWizard();

		wizardAction.next();

		wizardAction.newKaleoWorkflow.selectAssignUser();

		wizardAction.next();

		wizardAction.newKaleoWorkflow.prepareEmailAddress("test@liferay.com");

		wizardAction.finish();

		viewAction.project.closeAndDelete(projectName);
	}

}