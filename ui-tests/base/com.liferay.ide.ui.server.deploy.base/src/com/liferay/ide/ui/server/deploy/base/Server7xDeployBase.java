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

package com.liferay.ide.ui.server.deploy.base;

import com.liferay.ide.ui.liferay.ServerTestBase;
import com.liferay.ide.ui.liferay.support.project.ProjectSupport;
import com.liferay.ide.ui.liferay.support.server.PureTomcat70Support;
import com.liferay.ide.ui.liferay.support.server.PureTomcat71Support;
import com.liferay.ide.ui.liferay.support.server.PureTomcatDxpSupport;

import org.junit.Assert;
import org.junit.Rule;

/**
 * @author Terry Jia
 * @author Rui Wang
 */
public abstract class Server7xDeployBase extends ServerTestBase {

	public void deployFragment() {
		wizardAction.openNewFragmentWizard();

		wizardAction.newFragment.prepare(project.getName());

		wizardAction.next();

		wizardAction.newFragmentInfo.openBrowseOsgiBundleDialog();

		dialogAction.prepareText("com.liferay.blogs.web");

		dialogAction.confirm();

		wizardAction.newFragmentInfo.openAddOverrideFilesDialog();

		dialogAction.selectItems("META-INF/resources/init.jsp");

		dialogAction.confirm();

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(server.getServerName(), "STARTED com.liferay.blogs.web", M1);
		}

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(server.getServerName(), "STOPPED com.liferay.blogs.web", M1);
		}

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployModule() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(project.getName(), MVC_PORTLET, getVersion());

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		jobAction.waitForNoRunningJobs();

		Assert.assertTrue(viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName()));

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(server.getServerName(), "STARTED " + project.getName() + "_", M1);
		}

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(server.getServerName(), "STOPPED " + project.getName() + "_", M1);
		}

		viewAction.project.closeAndDelete(project.getName());
	}

	public void deployPluginPortlet() {
		viewAction.switchLiferayPerspective();

		wizardAction.openNewLiferayPluginProjectWizard();

		String projectName = project.getName("-portlet");

		wizardAction.newPlugin.prepareSdk(projectName);

		wizardAction.finish();

		jobAction.waitForIvy();

		jobAction.waitForValidate(projectName);

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(projectName);

		dialogAction.confirm(FINISH);

		viewAction.project.closeAndDelete(projectName);
	}

	public void deployWar() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(project.getName(), WAR_MVC_PORTLET, getVersion());

		wizardAction.finish();

		jobAction.waitForNoRunningProjectBuildingJobs();

		viewAction.servers.openAddAndRemoveDialog(server.getStartedLabel());

		dialogAction.addAndRemove.addModule(project.getName());

		dialogAction.confirm(FINISH);

		Assert.assertTrue(viewAction.servers.visibleModuleTry(server.getStartedLabel(), project.getName()));

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(
				server.getServerName(), "1 portlet for " + project.getName() + " is available for use", M5);
		}

		viewAction.servers.removeModule(server.getServerName(), project.getName());

		dialogAction.confirm();

		if (server instanceof PureTomcat70Support || server instanceof PureTomcat71Support ||
			server instanceof PureTomcatDxpSupport) {

			jobAction.waitForConsoleContent(server.getServerName(), "STOPPED " + project.getName() + "_", M1);
		}

		viewAction.project.closeAndDelete(project.getName());
	}

	@Rule
	public ProjectSupport project = new ProjectSupport(bot);

	protected abstract String getVersion();

}