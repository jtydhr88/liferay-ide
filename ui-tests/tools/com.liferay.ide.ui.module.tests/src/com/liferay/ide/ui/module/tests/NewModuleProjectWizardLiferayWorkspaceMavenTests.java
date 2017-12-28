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

import com.liferay.ide.ui.liferay.base.LiferayWorkspaceTomcatMavenBase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Terry Jia
 * @author Ying Xu
 */
public class NewModuleProjectWizardLiferayWorkspaceMavenTests extends LiferayWorkspaceTomcatMavenBase {

	@Test
	public void createActivator() {
		String projectName = "test-activator-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, ACTIVATOR);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createApi() {
		String projectName = "test-api-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, API);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingReport() {
		String projectName = "test-content-targeting-report-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, CONTENT_TARGETING_REPORT);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingRule() {
		String projectName = "test-content-targeting-rule-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, CONTENT_TARGETING_RULE);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createContentTargetingTrackingAction() {
		String projectName = "test-content-targeting-tracking-action-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, CONTENT_TARGETING_TRACKING_ACTION);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createControlMenuEntry() {
		String projectName = "test-control-menu-entry-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, CONTROL_MENU_ENTRY);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createFormField() {
		String projectName = "test-form-field-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, FORM_FIELD);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPanelApp() {
		String projectName = "test-panel-app-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, PANEL_APP);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletConfigurationIcon() {
		String projectName = "test-portlet-configuration-icon-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, PORTLET_CONFIGURATION_ICON);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletProvider() {
		String projectName = "test-portlet-provider-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, PORTLET_PROVIDER);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createPortletToolbarContributor() {
		String projectName = "test-portlet-toolbar-contributor-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, PORTLET_TOOLBAR_CONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createRest() {
		String projectName = "test-rest-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, REST);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createService() {
		String projectName = "test-service-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, SERVICE);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createServiceWrapper() {
		String projectName = "test-service-wrapper-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, SERVICE_WRAPPER);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createSimulationPanelEntry() {
		String projectName = "test-simulation-panel-entry-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, SIMULATION_PANEL_ENTRY);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createTemplateContextContributor() {
		String projectName = "test-template-context-contributor-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, TEMPLATE_CONTEXT_CONCONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createThemeContributor() {
		String projectName = "test-theme-contributor-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, THEME_CONTRIBUTOR);

		wizardAction.finish();

		String[] projectNames =
			{getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-modules (in modules)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createWarHook() {
		String projectName = "test-war-hook-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, WAR_HOOK);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-wars (in wars)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

	@Test
	public void createWarMvcPortlet() {
		String projectName = "test-war-mvc-portlet-in-lws-maven";

		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareMaven(projectName, WAR_MVC_PORTLET);

		wizardAction.finish();

		String[] projectNames = {getLiferayWorkspaceName(), getLiferayWorkspaceName() + "-wars (in wars)", projectName};

		Assert.assertTrue(viewAction.project.visibleFileTry(projectNames));

		viewAction.project.closeAndDeleteFromDisk(projectNames);
	}

}