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

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.support.project.ProjectSupport;
import com.liferay.ide.ui.liferay.support.workspace.LiferayWorkspaceGradleSupport;
import com.liferay.ide.ui.liferay.util.RuleUtil;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Rui Wang
 */
public class WatchModuleLiferayWorkspaceGradleTomcatTests extends SwtbotBase {

	public static LiferayWorkspaceGradleSupport workspace = new LiferayWorkspaceGradleSupport(bot);

	@ClassRule
	public static RuleChain chain = RuleUtil.getTomcat7xRunningLiferayWokrspaceRuleChain(bot, workspace);

	public static String getServerName() {
		return "Liferay Community Edition Portal 7.1.0 CE GA1";
	}

	public static String getStartedLabel() {
		return "Liferay Community Edition Portal 7.1.0 CE GA1  [Started]";
	}

	public static String getStoppedLabel() {
		return "Liferay Community Edition Portal 7.1.0 CE GA1  [Stopped]";
	}

	@Test
	public void watchPortlet() {
		wizardAction.openNewLiferayModuleWizard();

		wizardAction.newModule.prepareGradle(project.getName(), PORTLET, "7.1");

		wizardAction.finish();

		jobAction.waitForNoRunningJobs();

		viewAction.servers.visibleWorkspaceModuleTry(getStartedLabel(), workspace.getName(), project.getName());

		viewAction.servers.startWatchingProject(getStartedLabel(), workspace.getName(), project.getName());

		jobAction.waitForConsoleContent(getServerName() + " [Liferay 7.x]", "STARTED " + project.getName() + "_", M1);

		viewAction.servers.stopWatchingProject(
			getStartedLabel(), workspace.getName(), project.getName() + " [watching]");

		jobAction.waitForNoRunningJobs();

		viewAction.project.closeAndDelete(workspace.getModuleFiles(project.getName()));
	}

	@Rule
	public ProjectSupport project = new ProjectSupport(bot);

}