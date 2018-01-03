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

package com.liferay.ide.ui.layouttpl.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.base.Sdk62Support;

import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Terry Jia
 */
public class NewLayouttplProjectSdk62Tests extends SwtbotBase {

	@ClassRule
	public static Sdk62Support sdk62 = new Sdk62Support(bot);

	@Test
	public void createLayoutTemplate() {
		if (!envAction.internal()) {
			return;
		}

		viewAction.switchLiferayPerspective();

		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-template-layouttpl";

		wizardAction.newPlugin.prepareLayoutTemplateSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		wizardAction.openNewLiferayLayoutTemplate();

		wizardAction.finish();

		String layoutTpl = "test_template.tpl";

		viewAction.project.openFile(projectName, "docroot", layoutTpl);

		editorAction.close();

		String layoutWapTpl = "test_template.wap.tpl";

		viewAction.project.openFile(projectName, "docroot", layoutWapTpl);

		editorAction.close();

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createLayoutTemplateProject() {
		if (!envAction.internal()) {
			return;
		}

		viewAction.switchLiferayPerspective();

		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = "test-layouttpl";

		wizardAction.newPlugin.prepareLayoutTemplateSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		viewAction.project.closeAndDelete(projectName);
	}

}