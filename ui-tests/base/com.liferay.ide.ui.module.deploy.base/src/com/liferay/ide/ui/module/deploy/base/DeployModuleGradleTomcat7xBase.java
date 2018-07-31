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

package com.liferay.ide.ui.module.deploy.base;

import com.liferay.ide.ui.liferay.ServerTestBase;
import com.liferay.ide.ui.liferay.support.project.ProjectSupport;

import org.junit.Rule;

/**
 * @author Terry Jia
 * @author Lily Li
 */
public class DeployModuleGradleTomcat7xBase extends ServerTestBase {

	public void deployActivator() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), ACTIVATOR, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployApi() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), API, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployControlMenuEntry() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), CONTROL_MENU_ENTRY, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployFormField() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), FORM_FIELD, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployPanelApp() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), PANEL_APP, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployPortletConfigurationIcon() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), PORTLET_CONFIGURATION_ICON, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployPortletProvider() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), PORTLET_PROVIDER, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployPortletToolbarContributor() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), PORTLET_TOOLBAR_CONTRIBUTOR, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployRest() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), REST, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployService() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), SERVICE, "7.1");

		wizardAction.next();

		wizardAction.newModuleInfo.openSelectServiceDialog();

		dialogAction.prepareText("*lifecycleAction");

		dialogAction.confirm();

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployServiceWrapper() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), SERVICE_WRAPPER, "7.1");

		wizardAction.next();

		wizardAction.newModuleInfo.openSelectServiceDialog();

		dialogAction.prepareText("*BookmarksEntryLocalServiceWrapper");

		dialogAction.confirm();

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deploySimulationPanelEntry() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), SIMULATION_PANEL_ENTRY, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployTemplateContextContributor() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), TEMPLATE_CONTEXT_CONCONTRIBUTOR, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployThemeContributor() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), THEME_CONTRIBUTOR, envAction.getTempDir().getPath(), "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployWarHook() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), WAR_HOOK, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M5);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployWarMvcPortlet() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle71(project.getName(), WAR_MVC_PORTLET, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName());

		jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M5);

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		viewAction.project.closeAndDelete(project.getName());
	}

	@Rule
	public ProjectSupport project = new ProjectSupport(bot);

}