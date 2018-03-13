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

package com.liferay.ide.project.core.workspace;

import com.liferay.ide.project.core.NewLiferayProjectProvider;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.util.ServerUtil;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.ProgressMonitorBridge;
import org.eclipse.sapphire.platform.StatusBridge;

/**
 * @author Andy Wu
 */
public class NewLiferayWorkspaceOpMethods {

	public static Status execute(NewLiferayWorkspaceOp op, ProgressMonitor pm) {
		IProgressMonitor monitor = ProgressMonitorBridge.create(pm);

		monitor.beginTask("Creating Liferay Workspace project...", 100);

		Status retval = null;

		try {
			String wsName = op.getWorkspaceName().content();

			NewLiferayProjectProvider<NewLiferayWorkspaceOp> provider = op.getProjectProvider().content(true);

			IStatus status = provider.createNewProject(op, monitor);

			retval = StatusBridge.create(status);

			if (!retval.ok()) {
				return retval;
			}

			org.eclipse.sapphire.modeling.Path parent = op.getLocation().content();

			String location = parent.append(wsName).toPortableString();

			boolean initBundle = op.getProvisionLiferayBundle().content();

			if (initBundle) {
				String serverRuntimeName = op.getServerName().content();
				IPath bundlesLocation = null;

				String projectProvider = op.getProjectProvider().text();

				if (projectProvider.equals("gradle-liferay-workspace")) {
					bundlesLocation = LiferayWorkspaceUtil.getHomeLocation(location);
				}
				else {
					bundlesLocation = new Path(location).append("bundles");
				}
                
				if (bundlesLocation != null &&
					bundlesLocation.toFile().exists() &&
					LiferayServerCore.isPortalBundlePath(bundlesLocation)) {
			        ServerUtil.addPortalRuntimeAndServer(serverRuntimeName, bundlesLocation, monitor);
			    }
			    else {
			        ProjectCore.logWarning( "bundles download failed" );
			    }
			}
		}
		catch (Exception e) {
			String msg = "Error creating Liferay Workspace project.";

			ProjectCore.logError(msg, e);

			return Status.createErrorStatus(msg, e);
		}

		if (retval.ok()) {
			_updateBuildPrefs(op);
		}

		return retval;
	}

	private static void _updateBuildPrefs(NewLiferayWorkspaceOp op) {
		try {
			IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(ProjectCore.PLUGIN_ID);

			prefs.put(ProjectCore.PREF_DEFAULT_WORKSPACE_PROJECT_BUILD_TYPE_OPTION, op.getProjectProvider().text());

			prefs.flush();
		}
		catch (Exception e) {
			String msg = "Error updating default workspace build type.";

			ProjectCore.logError(msg, e);
		}
	}

}