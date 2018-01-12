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

package com.liferay.ide.ui.portlet.tests;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.base.LiferayWorkspaceGradleSupport;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Rui Wang
 */
public class NewPortletModuleLiferayWorkspaceGradleTests extends SwtbotBase {

	@ClassRule
	public static LiferayWorkspaceGradleSupport liferayWorkspace = new LiferayWorkspaceGradleSupport(bot);

	@Test
	public void createFreemarkerPortlet() {
		String projectName = "test-freemarker-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, FREEMARKER_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createMvcPortlet() {
		String projectName = "test-mvc-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, MVC_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		jobAction.waitForValidate(projectName);

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmAngularPortlet() {
		String projectName = "test-npm-angular-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_ANGULAR_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmBillboardjsPortlet() {
		String projectName = "test-npm-billboardjs-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_BILLBOARDJS_PORLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmIsomorphicPortlet() {
		String projectName = "test-npm-isomorphic-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_ISOMORPHIC_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmJqueryPortlet() {
		String projectName = "test-npm-jquery-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_JQUERY_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmMetaljsPortletGradle() {
		String projectName = "test-npm-metaljs-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_METALJS_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmPortlet() {
		String projectName = "test-npm-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmReactPortlet() {
		String projectName = "test-npm-react-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_REACT_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createNpmVuejsPortlet() {
		String projectName = "test-npm-vuejs-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, NPM_VUEJS_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Test
	public void createPortlet() {
		String projectName = "test-npm-angular-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

	@Ignore("ignore to wait IDE-3579 as it will take too long unexpected")
	@Test
	public void createSoyPortlet() {
		String projectName = "test-soy-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SOY_PORTLET);

		wizardAction.finish();

		Assert.assertTrue(viewAction.project.visibleFileTry(projectName));

		viewAction.project.closeAndDelete(projectName);
	}

	@Test
	public void createSpringMvcPortlet() {
		String projectName = "test-spring-mvc-portlet-gradle-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SPRING_MVC_PORTLET);

		wizardAction.finish();

		String[] projectNames = {liferayWorkspace.getLiferayWorkspaceName(), "wars", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDelete(projectNames);
	}

}