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

package com.liferay.ide.ui.liferay.action;

import com.liferay.ide.ui.liferay.UIAction;
import com.liferay.ide.ui.swtbot.condition.BrowserLoadedCondition;
import com.liferay.ide.ui.swtbot.condition.CancelIvyJobCondition;
import com.liferay.ide.ui.swtbot.condition.CancelValidateJobCondition;
import com.liferay.ide.ui.swtbot.condition.CloseProjectJobCondition;
import com.liferay.ide.ui.swtbot.condition.ConsoleContentCondition;
import com.liferay.ide.ui.swtbot.condition.IvyJobsCondition;
import com.liferay.ide.ui.swtbot.condition.NoRunningJobsCondition;
import com.liferay.ide.ui.swtbot.condition.RefreshForSubnodeCondition;
import com.liferay.ide.ui.swtbot.condition.ServerStartJobCondition;
import com.liferay.ide.ui.swtbot.condition.ServerStopJobCondition;
import com.liferay.ide.ui.swtbot.condition.ShellAppearedCondition;
import com.liferay.ide.ui.swtbot.condition.ValidateJobCondition;
import com.liferay.ide.ui.swtbot.condition.WizardClosedCondition;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * @author Terry Jia
 */
public class JobAction extends UIAction {

	public JobAction(SWTWorkbenchBot bot) {
		super(bot);
	}

	public void waitForBrowserLoaded() {
		ide.waitUntil(new BrowserLoadedCondition(bot));
	}

	public void waitForCancelIvy() {
		ide.waitUntil(new CancelIvyJobCondition());
	}

	public void waitForCancelValidate(String projectName) {
		ide.waitUntil(new CancelValidateJobCondition(projectName), 120 * 1000);
	}

	public void waitForCloseProject() {
		ide.waitUntil(new CloseProjectJobCondition());
	}

	public void waitForConsoleContent(String consoleName, String content, long timeout) {
		ide.sleep(2000);

		ide.waitUntil(new ConsoleContentCondition(consoleName, content), timeout);
	}

	public void waitForIvy() {
		ide.waitUntil(new IvyJobsCondition(), 300 * 1000);
	}

	public void waitForNoRunningJobs() {
		ide.waitUntil(new NoRunningJobsCondition());
	}

	public void waitForServerStarted(String serverName) {
		ide.waitUntil(new ServerStartJobCondition(serverName), 300 * 1000);
	}

	public void waitForServerStopped(String serverName) {
		ide.waitUntil(new ServerStopJobCondition(serverName));
	}

	public void waitForShellAppeared(String title) {
		ide.waitUntil(new ShellAppearedCondition(bot, title));
	}

	public void waitForShellClosed(String title) {
		ide.waitUntil(new WizardClosedCondition(bot, title), 300 * 1000);
	}

	public void waitForSubnode(SWTBotTreeItem parent, String subnode, String refreshText) {
		ide.waitUntil(new RefreshForSubnodeCondition(parent, subnode, refreshText), 30 * 1000);
	}

	public void waitForValidate(String projectName) {
		ide.waitUntil(new ValidateJobCondition(projectName), 300 * 1000);
	}

}