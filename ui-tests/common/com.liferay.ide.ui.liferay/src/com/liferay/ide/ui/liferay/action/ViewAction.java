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
import com.liferay.ide.ui.liferay.page.view.CodeUpgradeView;
import com.liferay.ide.ui.swtbot.eclipse.page.DeleteResourcesDialog;
import com.liferay.ide.ui.swtbot.eclipse.page.ErrorLogView;
import com.liferay.ide.ui.swtbot.eclipse.page.PackageExplorerView;
import com.liferay.ide.ui.swtbot.eclipse.page.ProjectExplorerView;
import com.liferay.ide.ui.swtbot.eclipse.page.ServersView;
import com.liferay.ide.ui.swtbot.page.Perspective;
import com.liferay.ide.ui.swtbot.page.Tree;

import java.util.Arrays;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * @author Terry Jia
 * @author Ying Xu
 * @author Rui Wang
 */
public class ViewAction extends UIAction {

	public static ViewAction getInstance(SWTWorkbenchBot bot) {
		if (_viewAction == null) {
			_viewAction = new ViewAction(bot);
		}

		return _viewAction;
	}

	public void showCodeUpgradeView() {
		ide.showCodeUpgradeView();
	}

	public void showServersView() {
		ide.showServersView();
	}

	public void switchKaleoDesignerPerspective() {
		Perspective kaleoDesignerPerspective = ide.getKaleoDesignerPerspective();

		kaleoDesignerPerspective.activate();
	}

	public void switchLiferayPerspective() {
		Perspective liferayPerspective = ide.getLiferayPerspective();

		liferayPerspective.activate();
	}

	public CodeUpgradeViewAction codeUpgrade = new CodeUpgradeViewAction();
	public ProjectViewAction project = new ProjectViewAction();
	public ServersViewAction servers = new ServersViewAction();

	public class CodeUpgradeViewAction {

		// Just for right now

		public void prepareMigrateLayout(String migrateLayout) {
			_codeUpgradeView.selectMigrateLayouts(migrateLayout);
		}

		public void restartUpgrade() {
			_codeUpgradeView.clickRestartUpgradeBtn();
		}

		public void showAllPages() {
			_codeUpgradeView.clickShowAllPagesBtn();
		}

		public void switchGear(int index) {
			_codeUpgradeView.selectGear(index);
		}

		private final CodeUpgradeView _codeUpgradeView = new CodeUpgradeView(bot);

	}

	public class ErrorLogViewAction {

		public void checkErrorLog() {
			Tree errorLog = _errorLogView.getLogs();

			System.out.println(errorLog.size());
		}

		private final ErrorLogView _errorLogView = new ErrorLogView(bot);

	}

	public class ProjectViewAction {

		public void closeAndDelete(String... items) {
			closeProjectTry(items);

			delete(items);
		}

		public void closeAndDeleteFromDisk(String... items) {
			closeProjectTry(items);

			deleteProjectFromDisk(items);
		}

		public void closeAndDeleteWithNoRunningJobs(String... items) {
			closeProjectTry(items);

			_jobAction.waitForNoRunningJobs();

			delete(items);
		}

		public void closeProjectTry(String... items) {
			ide.sleep();

			try {
				_getProjects().contextMenu(true, "Close Project", items);

				_jobAction.waitForCloseProject();
			}
			catch (Exception e) {
			}
		}

		/**
		 * The resource won't be deleted from disk if we call this method on Project.
		 * But the resource will be deleted from disk if we call this method on Package or File.
		 */
		public void delete(String... items) {
			_getProjects().contextMenu(true, DELETE, items);

			ide.sleep();

			String label = _deleteResourcesDialog.getLabel();

			_deleteResourcesDialog.confirm();

			_jobAction.waitForShellClosed(label);
		}

		public void deleteProjectFromDisk(String... items) {
			_getProjects().contextMenu(true, DELETE, items);

			ide.sleep();

			String label = _deleteResourcesDialog.getLabel();

			_deleteResourcesDialog.selectDeleteFromDisk();

			_deleteResourcesDialog.confirm();

			_jobAction.waitForShellClosed(label);
		}

		public String[] getProjectNames() {
			return _getProjects().getItemLabels();
		}

		public void openComponentClassWizard(String... projectNames) {
			_getProjects().contextMenu(LIFERAY_COMPONENT_CLASS, projectNames);
		}

		public void openFile(String... files) {
			ide.sleep();

			_getProjects().setFocus();

			try {
				_getProjects().doubleClick(files);
			}
			catch (Exception e) {
			}

			try {
				_getProjects().contextMenu("Open", files);
			}
			catch (Exception e) {
			}
		}

		public void openFragmentFilesWizard() {
			_getProjects().contextMenu(LIFERAY_MODULE_FRAGMENT_FILES);
		}

		public void openUpdateMavenProjectDialog(String projectName) {
			try {
				_getProjects().contextMenu("Update Project...", projectName);
			}
			catch (Exception e) {
			}
		}

		public void refreshGradleProject(String projectName) {
			ide.sleep(1000);

			try {
				_getProjects().contextMenu("Refresh Gradle Project", projectName);

				// should use job way instead

				ide.sleep(2000);
			}
			catch (Exception e) {
			}
		}

		// module project

		public void runBuildService(String... projectNames) {
			try {
				_getProjects().contextMenu(BUILD_SERVICE, projectNames);
			}
			catch (Exception e) {
				ide.sleep(2000);

				_getProjects().contextMenu(BUILD_SERVICE, projectNames);
			}
		}

		public void runBuildServices(String... projectNames) {
			try {
				_getProjects().contextMenu(BUILD_SERVICES, projectNames);
			}
			catch (Exception e) {
				ide.sleep(2000);

				_getProjects().contextMenu(BUILD_SERVICES, projectNames);
			}
		}

		public void runBuildWSDD(String... projectNames) {
			try {
				_getProjects().contextMenu(BUILD_WSDD, projectNames);
			}
			catch (Exception e) {
				ide.sleep(2000);

				_getProjects().contextMenu(BUILD_WSDD, projectNames);
			}
		}

		public void runGradleInitBundle(String... projectNames) {
			try {
				_getProjects().contextMenu(INITIALIZE_SERVER_BUNDLE, projectNames);
			}
			catch (Exception e) {
				ide.sleep(2000);

				_getProjects().contextMenu(INITIALIZE_SERVER_BUNDLE, projectNames);
			}
		}

		public void runMavenInitBundle(String... projectNames) {
			try {
				_getProjects().contextMenu(INIT_BUNDLE, projectNames);
			}
			catch (Exception e) {
				ide.sleep(2000);

				_getProjects().contextMenu(INIT_BUNDLE, projectNames);
			}
		}

		public boolean visibleFileTry(String... files) {
			try {
				return _getProjects().isVisible(files);
			}
			catch (Exception e) {
				_getProjects().setFocus();

				try {
					String[] parents = Arrays.copyOfRange(files, 0, files.length - 1);

					_getProjects().expand(parents);

					_getProjects().contextMenu(REFRESH, parents);

					ide.sleep(2000);
				}
				catch (Exception e1) {
				}

				for (int i = files.length - 1; i > 0; i--) {
					String[] parents = Arrays.copyOfRange(files, 0, files.length - i);

					SWTBotTreeItem parent = _getProjects().getTreeItem(parents);

					_getProjects().expand(parents);

					String subnode = files[files.length - i];

					_jobAction.waitForSubnode(parent, subnode, REFRESH);
				}

				return _getProjects().isVisible(files);
			}
		}

		private Tree _getProjects() {
			String perspectiveLabel = ide.getPerspectiveLabel();

			if (perspectiveLabel.equals(LIFERAY_WORKSPACE)) {
				return _projectExplorerView.getProjects();
			}
			else if (perspectiveLabel.equals(LIFERAY_PLUGINS)) {
				return _packageExplorerView.getProjects();
			}
			else if (perspectiveLabel.equals(KALEO_DESIGNER)) {
				return _packageExplorerView.getProjects();
			}

			return null;
		}

		private final DeleteResourcesDialog _deleteResourcesDialog = new DeleteResourcesDialog(bot);
		private final PackageExplorerView _packageExplorerView = new PackageExplorerView(bot);
		private final ProjectExplorerView _projectExplorerView = new ProjectExplorerView(bot);

	}

	public class ServersViewAction {

		public void clickKaleoNameDialog(String serverLabel) {
			ide.sleep(5000);

			_getServers().setFocus();

			_getServers().selectTreeItem(serverLabel, KALEO_WORKFLOWS, "New Workflow  [Version: 1, Draft Version: 1]");

			ide.sleep();
		}

		public void debug(String serverLabel) {
			ide.sleep(5000);

			_getServers().select(serverLabel);

			_serversView.clickDebugBtn();
		}

		public void openAddAndRemoveDialog(String serverLabel) {
			_getServers().contextMenu(true, ADD_AND_REMOVE, serverLabel);
		}

		public void openEditor(String serverLabel) {
			_getServers().doubleClick(serverLabel);
		}

		public void openLiferayPortalHome(String serverLabel) {
			_getServers().contextMenu(true, OPEN_LIFERAY_PORTAL_HOME, serverLabel);

			_jobAction.waitForBrowserLoaded();
		}

		public void openUpLoadNewWorkflowDialog(String serverLabel) {
			ide.sleep(2000);

			_getServers().setFocus();

			_getServers().selectTreeItem(serverLabel, KALEO_WORKFLOWS);

			_getServers().contextMenu(true, "Upload new workflow...", serverLabel, KALEO_WORKFLOWS);
		}

		public void removeModule(String serverLabel, String projectName) {
			ide.sleep(2000);

			_getServers().setFocus();

			_getServers().contextMenu(true, "Remove", serverLabel, projectName);
		}

		public void start(String serverLabel) {
			ide.sleep(5000);

			_getServers().select(serverLabel);

			_serversView.clickStartBtn();
		}

		public void stop(String serverLabel) {
			ide.sleep(2000);

			_getServers().contextMenu(true, STOP, serverLabel);
		}

		public boolean visibleKaleofolderTry(String serverLabel) {
			try {
				return _getServers().isVisibleStartsBy(KALEO_WORKFLOWS);
			}
			catch (Exception e) {
				_getServers().setFocus();

				_getServers().select(serverLabel);

				_getServers().expand(serverLabel);

				_getServers().expand(KALEO_WORKFLOWS);

				_getServers().selectTreeItem(
					serverLabel, KALEO_WORKFLOWS, "New Workflow  [Version: 1, Draft Version: 1]");

				return _getServers().isVisibleStartsBy(KALEO_WORKFLOWS);
			}
		}

		public boolean visibleKaleoNameTry(String serverLabel, String kaleoName) {
			try {
				return _getServers().isVisibleStartsBy(serverLabel, KALEO_WORKFLOWS, kaleoName);
			}
			catch (Exception e) {
				_getServers().setFocus();

				_getServers().select(serverLabel);

				_getServers().expand(serverLabel);

				_getServers().expand(KALEO_WORKFLOWS);

				_getServers().select(KALEO_WORKFLOWS);

				_getServers().expand("New Workflow  [Version: 1, Draft Version: 1]");

				_getServers().getTreeItem(serverLabel, KALEO_WORKFLOWS, "New Workflow  [Version: 1, Draft Version: 1]");

				return _getServers().isVisibleStartsBy(
					serverLabel, KALEO_WORKFLOWS, "New Workflow  [Version: 1, Draft Version: 1]");
			}
		}

		public boolean visibleModuleTry(String serverLabel, String projectName) {
			try {
				return _getServers().isVisibleStartsBy(serverLabel, projectName);
			}
			catch (Exception e) {
				_getServers().setFocus();

				_getServers().select(serverLabel);

				_getServers().expand(serverLabel);

				return _getServers().isVisibleStartsBy(serverLabel, projectName);
			}
		}

		public boolean visibleServer(String serverName) {
			return _getServers().isVisibleStartsBy(serverName);
		}

		private Tree _getServers() {
			return _serversView.getServers();
		}

		private final ServersView _serversView = new ServersView(bot);

	}

	private ViewAction(SWTWorkbenchBot bot) {
		super(bot);
	}

	private static ViewAction _viewAction;

	private final JobAction _jobAction = JobAction.getInstance(bot);

}