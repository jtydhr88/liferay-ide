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

package com.liferay.ide.gradle.action;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.gradle.core.GradleCore;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;

/**
 * @author Terry Jia
 * @author Charles Wu
 * @author Simon Jiang
 */
public class InitBundleTaskAction extends GradleTaskAction {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		action.setEnabled(LiferayWorkspaceUtil.isValidWorkspace(project));
	}

	protected void afterTask() {
		LiferayWorkspaceUtil.addPortalRuntime();
	}

	@Override
	protected void beforeTask() {
		IWorkspaceProject liferayWorkpsaceProject = LiferayCore.create(IWorkspaceProject.class, project);

		if (liferayWorkpsaceProject != null) {
			IPath bundlesLocation = LiferayWorkspaceUtil.getHomeLocation(project);

			if (FileUtil.notExists(bundlesLocation)) {
				return;
			}

			Stream.of(
				ServerCore.getServers()
			).filter(
				server -> server != null
			).filter(
				server -> {
					IRuntime runtime = server.getRuntime();

					return bundlesLocation.equals(runtime.getLocation());
				}
			).forEach(
				server -> {
					try {
						IRuntime runtime = server.getRuntime();

						server.delete();

						if (runtime != null) {
							runtime.delete();
						}
					}
					catch (Exception e) {
						ProjectCore.logError("Failed to delete server and runtime", e);
					}
				}
			);

			try {
				FileUtil.deleteDir(bundlesLocation.toFile(), true);

				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
			catch (CoreException ce) {
				GradleCore.logError(ce);
			}
		}
	}

	@Override
	protected String getGradleTask() {
		return "initBundle";
	}

}