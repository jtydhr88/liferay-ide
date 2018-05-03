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

package com.liferay.ide.ui.server.wizard.base;

import com.liferay.ide.ui.liferay.ServerTestBase;
import com.liferay.ide.ui.liferay.page.editor.ServerEditor;
import com.liferay.ide.ui.liferay.page.editor.ServerTable;
import com.liferay.ide.ui.swtbot.page.SWTBotHyperlink;

import org.junit.Assert;

/**
 * @author Terry Jia
 * @author Vicky Wang
 * @author Ashley Yuan
 * @author Ying Xu
 */
public class ServerTomcat7xBase extends ServerTestBase {

	public void addLiferay7RuntimeFromPreferences() {
		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(testServer.getServerName(), testServer.getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		dialogAction.deleteRuntimFromPreferences(testServer.getServerName());

		resetTestServer();
	}

	public void addLiferay7ServerFromMenu() {
		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(testServer.getServerName(), testServer.getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(testServer.getServerName());

		wizardAction.finish();

		dialogAction.deleteRuntimFromPreferences(testServer.getServerName());

		resetTestServer();
	}

	public void serverEditorCustomLaunchSettingsChange() {
		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(testServer.getServerName(), testServer.getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(testServer.getServerName());

		wizardAction.finish();

		viewAction.servers.openEditor(testServer.getStoppedLabel());

		editorAction.server.selectCustomLaunchSettings();

		editorAction.server.selectUseDeveloperMode();

		editorAction.save();

		editorAction.close();

		viewAction.servers.openEditor(testServer.getStoppedLabel());

		editorAction.server.selectDefaultLaunchSettings();

		editorAction.save();

		editorAction.close();

		dialogAction.deleteRuntimFromPreferences(testServer.getServerName());

		resetTestServer();
	}

	public void serverEditorCustomLaunchSettingsChangeAndStart() {
		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(testServer.getServerName(), testServer.getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(testServer.getServerName());

		wizardAction.finish();

		viewAction.servers.openEditor(testServer.getStoppedLabel());

		editorAction.server.selectCustomLaunchSettings();

		editorAction.server.selectUseDeveloperMode();

		editorAction.save();

		editorAction.close();

		// viewAction.servers.start(serverStoppedLabel);

		// jobAction.waitForServerStarted(serverName);

		// String serverStartedLabel = serverName + "  [Started]";

		// viewAction.servers.stop(serverStartedLabel);

		// jobAction.waitForServerStopped(serverName);

		viewAction.servers.openEditor(testServer.getStoppedLabel());

		editorAction.server.selectDefaultLaunchSettings();

		editorAction.save();

		editorAction.close();

		dialogAction.deleteRuntimFromPreferences(testServer.getServerName());

		resetTestServer();
	}

	public void serverEditorPortsChangeAndStart() {
		String serverName = "Liferay 7-server-port-change";

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(getTestServer().getServerName(), getTestServer().getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(serverName);

		wizardAction.finish();

		String serverStoppedLabel = serverName + "  [Stopped]";

		viewAction.servers.openEditor(serverStoppedLabel);

		ServerEditor serverEditor = new ServerEditor(bot);

		ServerTable serverPortTable = serverEditor.getServerPortTable();

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
		ServerTable serverPortChangedTable = serverEditor.getServerPortTable();

		String[] serverPortChangedValues = serverPortChangedTable.getServerPortsInfo();

		Assert.assertArrayEquals(serverPortChangedValues, newPortValues);

		SWTBotHyperlink resetHyperlink = serverEditor.getHyperlink();

		resetHyperlink.click();

		editorAction.save();
		editorAction.close();

		viewAction.servers.openEditor(serverStoppedLabel);
		ServerTable serverPortResetTable = serverEditor.getServerPortTable();

		String[] serverPortResetValues = serverPortResetTable.getServerPortsInfo();

		Assert.assertArrayEquals(serverPortResetValues, serverPortValues);

		dialogAction.openPreferencesDialog();

		dialogAction.serverRuntimeEnvironments.deleteRuntimeTryConfirm(serverName);

		dialogAction.preferences.confirm();

		resetTestServer();
	}

	public void testLiferay7ServerDebug() {
		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openServerRuntimeEnvironmentsTry();

		dialogAction.serverRuntimeEnvironments.openNewRuntimeWizard();

		wizardAction.newRuntime.prepare7();

		wizardAction.next();

		wizardAction.newRuntime7.prepare(testServer.getServerName(), testServer.getFullServerDir());

		wizardAction.finish();

		dialogAction.preferences.confirm();

		wizardAction.openNewLiferayServerWizard();

		wizardAction.newServer.prepare(testServer.getServerName());

		wizardAction.finish();

		// String serverStoppedLabel = serverName + "  [Stopped]";

		// viewAction.servers.debug(serverStoppedLabel);

		// jobAction.waitForServerStarted(serverName);

		// String serverDebuggingLabel = serverName + "  [Debugging]";

		// viewAction.servers.stop(serverDebuggingLabel);

		// jobAction.waitForServerStopped(serverName);

		dialogAction.deleteRuntimFromPreferences(testServer.getServerName());

		resetTestServer();
	}

}