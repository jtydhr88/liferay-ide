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

import com.liferay.ide.ui.liferay.base.LiferayWorkspaceTomcatGradleBase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Ying Xu
 * @author Ashley Yuan
 * @author Sunny Shi
 */
public class NewModuleProjectWizardLiferayWorkspaceGradleTests extends LiferayWorkspaceTomcatGradleBase {

	@Test
	public void createActivator() {
		String projectName = "test-activator-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, ACTIVATOR);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createApi() {
		String projectName = "test-api-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, API);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingReport() {
		String projectName = "test-content-targeting-report-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, CONTENT_TARGETING_REPORT);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingRule() {
		String projectName = "test-content-targeting-rule-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, CONTENT_TARGETING_RULE);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingTrackingAction() {
		String projectName = "test-content-targeting-tracking-action-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, CONTENT_TARGETING_TRACKING_ACTION);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createControlMenuEntry() {
		String projectName = "test-control-menu-entry-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, CONTROL_MENU_ENTRY);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createFormField() {
		String projectName = "test-form-field-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, FORM_FIELD);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createMvcPortlet() {
		String projectName = "test-mvc-portlet-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, MVC_PORTLET);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPanelApp() {
		String projectName = "test-panel-app-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PANEL_APP);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortlet() {
		String projectName = "test-portlet-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PORTLET);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletConfigurationIcon() {
		String projectName = "test-portlet-configuration-icon-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PORTLET_CONFIGURATION_ICON);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletProvider() {
		String projectName = "test-portlet-provider-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PORTLET_PROVIDER);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletToolbarContributor() {
		String projectName = "test-portlet-toolbar-contributor-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, PORTLET_TOOLBAR_CONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createRest() {
		String projectName = "test-rest-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, REST);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createService() {
		String projectName = "test-service-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SERVICE);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createServiceBuilder() {
		String projectName = "test-service-builder-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SERVICE_BUILDER);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createServiceWrapper() {
		String projectName = "test-service-wrapper-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SERVICE_WRAPPER);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createSimulationPanelEntry() {
		String projectName = "test-simulation-panel-entry-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, SIMULATION_PANEL_ENTRY);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createTemplateContextContributor() {
		String projectName = "test-template-context-contributor-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, TEMPLATE_CONTEXT_CONCONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createTheme() {
		String projectName = "test-theme-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, THEME);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "wars", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createThemeContributor() {
		String projectName = "test-theme-contributor-in-lrws";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(projectName, THEME_CONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), "modules", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

}