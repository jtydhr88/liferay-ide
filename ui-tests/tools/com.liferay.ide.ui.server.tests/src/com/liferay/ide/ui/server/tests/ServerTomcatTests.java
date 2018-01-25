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

package com.liferay.ide.ui.server.tests;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.liferay.ide.ui.liferay.SwtbotBase;
import com.liferay.ide.ui.liferay.page.editor.ServerEditor;
import com.liferay.ide.ui.swtbot.page.SWTBotHyperlink;
import com.liferay.ide.ui.swtbot.page.Table;

/**
 * @author Terry Jia
 * @author Vicky Wang
 * @author Ashley Yuan
 * @author Ying Xu
 */
public class ServerTomcatTests extends SwtbotBase {

	@BeforeClass
	public static void prepareServer() throws IOException {
		envAction.unzipServer();

		envAction.prepareGeoFile();

		envAction.preparePortalExtFile();

		envAction.preparePortalSetupWizardFile();

		String serverName = "Liferay 7-initialization";

		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		IPath serverDir = envAction.getServerDir();

		IPath fullServerDir = serverDir.append(envAction.getServerName());

		wizardAction.newRuntime7.prepare(serverName, fullServerDir.toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		// String serverStoppedLabel = serverName + "  [Stopped]";

		// viewAction.servers.start(serverStoppedLabel);

		// jobAction.waitForServerStarted(serverName);

		// String serverStartedLabel = serverName + "  [Started]";

		// viewAction.openLiferayPortalHome(serverStartedLabel);

		// viewAction.servers.stop(serverStartedLabel);

		// jobAction.waitForServerStopped(serverName);

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();
	}

	@Test
	public void addLiferay7RuntimeFromPreferences() {
		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		String runtimeName = "Liferay 7-add-runtime";

		wizardAction.newRuntime7.prepare(runtimeName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(runtimeName);

		dialogAction.preferences.confirm();
	}

	@Test
	public void addLiferay7ServerFromMenu() {
		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		String runtimeName = "Liferay 7-add-server";

		wizardAction.newRuntime7.prepare(runtimeName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare("Liferay 7-add-server");

		wizardAction.finish();

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(runtimeName);

		dialogAction.preferences.confirm();
	}

	@Test
	public void serverEditorCustomLaunchSettingsChange() {
		String serverName = "Liferay 7-custom-launch-settings";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.selectCustomLaunchSettings();

		editorAction.server.selectUseDeveloperMode();

		editorAction.save();

		editorAction.close();

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.selectDefaultLaunchSettings();

		editorAction.save();

		editorAction.close();

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();
	}

	@Test
	public void serverEditorCustomLaunchSettingsChangeAndStart() {
		String serverName = "Liferay 7-custom-launch-settings-start";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.selectCustomLaunchSettings();

		editorAction.server.selectUseDeveloperMode();

		editorAction.save();

		editorAction.close();

		// viewAction.servers.start(serverStoppedLabel);

		// jobAction.waitForServerStarted(serverName);

		// String serverStartedLabel = serverName + "  [Started]";

		// viewAction.servers.stop(serverStartedLabel);

		// jobAction.waitForServerStopped(serverName);

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.selectDefaultLaunchSettings();

		editorAction.save();

		editorAction.close();

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();
	}

	@Ignore("To wait for IDE-3343")
	@Test
	public void serverEditorPortsChange() {
		String serverName = "Liferay 7-http-port-change";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.setHttpPort("8081");

		editorAction.save();

		editorAction.close();

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.setHttpPort("8080");

		editorAction.save();

		editorAction.close();
	}

	@Ignore("To wait for IDE-3343")
	@Test
	public void serverEditorPortsChangeAndStart() {
		String serverName = "Liferay 7-http-port-change-and-start";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.setHttpPort("8082");

		editorAction.save();

		editorAction.close();

		viewAction.servers.start(serverStoppedLabel);

		jobAction.waitForServerStarted(serverName);

		String serverStartedLabel = serverName + "  [Started]";

		viewAction.servers.stop(serverStartedLabel);

		jobAction.waitForServerStopped(serverName);

		viewAction.servers.openEditor(serverStoppedLabel);

		editorAction.server.setHttpPort("8080");

		editorAction.save();

		editorAction.close();
	}

	@Test
	public void testLiferay7ServerDebug() {
		String serverName = "Liferay 7-debug";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		// String serverStoppedLabel = serverName + "  [Stopped]";

		// viewAction.servers.debug(serverStoppedLabel);

		// jobAction.waitForServerStarted(serverName);

		// String serverDebuggingLabel = serverName + "  [Debugging]";

		// viewAction.servers.stop(serverDebuggingLabel);

		// jobAction.waitForServerStopped(serverName);

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();
	}

	@Test
	public void serverEditorServerPortChange() {
		String serverName = "Liferay 7-server-port-change";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(serverName, envAction.getServerFullDir().toOSString());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		ServerEditor serverEditor = new ServerEditor(bot);

		Table serverPortTable = serverEditor.getServerPortTable();

		String[] serverPortValues = serverPortTable.getServerPortsInfo();

		String[] newPortValues = new String[serverPortValues.length];

		for (int i = 0; i < serverPortValues.length; i++) {
			serverPortTable.click(i, 1);
			Integer newPortValue = Integer.valueOf(serverPortValues[i]) + 1;

			serverPortTable.setText(serverPortValues[i], newPortValue.toString());
			newPortValues[i] = newPortValue.toString();
		}

		editorAction.save();
		editorAction.close();

		viewAction.servers.openEditor(serverStoppedLabel);
		Table serverPortChangedTable = serverEditor.getServerPortTable();

		String[] serverPortChangedValues = serverPortChangedTable.getServerPortsInfo();

		Assert.assertArrayEquals(serverPortChangedValues, newPortValues);

		SWTBotHyperlink resetHyperlink = serverEditor.getHyperlink();

		resetHyperlink.click();

		editorAction.save();
		editorAction.close();

		viewAction.servers.openEditor(serverStoppedLabel);
		Table serverPortResetTable = serverEditor.getServerPortTable();

		String[] serverPortResetValues = serverPortResetTable.getServerPortsInfo();

		Assert.assertArrayEquals(serverPortResetValues, serverPortValues);

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();
	}
}