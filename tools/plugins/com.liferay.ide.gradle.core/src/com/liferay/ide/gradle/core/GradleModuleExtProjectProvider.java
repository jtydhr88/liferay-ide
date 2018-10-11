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

package com.liferay.ide.gradle.core;

import com.liferay.ide.core.AbstractLiferayProjectProvider;
import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.NewLiferayProjectProvider;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.ext.NewModuleExtOp;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.platform.PathBridge;

import org.osgi.framework.Version;

/**
 * @author Charles Wu
 */
public class GradleModuleExtProjectProvider
	extends AbstractLiferayProjectProvider implements NewLiferayProjectProvider<NewModuleExtOp> {

	public GradleModuleExtProjectProvider() {
		super(null);
	}

	@Override
	public IStatus createNewProject(NewModuleExtOp op, IProgressMonitor monitor) throws CoreException {
		IStatus retval = Status.OK_STATUS;

		String projectName = SapphireUtil.getContent(op.getProjectName());
		String originalModuleName = SapphireUtil.getContent(op.getOriginalModuleName());
		Version originalModuleVersion = SapphireUtil.getContent(op.getOriginalModuleVersion());
		String targetPlatform = SapphireUtil.getContent(op.getTargetPlatformVersion());

		IPath location = PathBridge.create(SapphireUtil.getContent(op.getLocation()));

		StringBuilder sb = new StringBuilder();

		File locationFile = location.toFile();

		sb.append("create -d \"");
		sb.append(locationFile.getAbsolutePath());
		sb.append("\" -t ");
		sb.append("modules-ext ");
		sb.append("-m ");
		sb.append(originalModuleName);

		if (CoreUtil.isNullOrEmpty(targetPlatform)) {
			sb.append(" -M ");
			sb.append(originalModuleVersion);
		}

		sb.append(" \"");
		sb.append(projectName);
		sb.append("\"");

		try {
			BladeCLI.execute(sb.toString());
		}
		catch (Exception e) {
			return GradleCore.createErrorStatus("Could not create module ext project.", e);
		}

		IPath projecLocation = location.append(projectName);

		CoreUtil.openProject(projectName, projecLocation, monitor);

		boolean useDefaultLocation = SapphireUtil.getContent(op.getUseDefaultLocation());
		boolean inWorkspacePath = false;

		IWorkspaceProject gradleWorkspaceProject = LiferayWorkspaceUtil.getGradleWorkspaceProject();

		IProject project = gradleWorkspaceProject.getProject();

		if ((gradleWorkspaceProject != null) && !useDefaultLocation) {
			IPath workspaceLocation = project.getLocation();

			if (workspaceLocation != null) {
				String extDir = LiferayWorkspaceUtil.getModuleExtDir(project);

				if (extDir != null) {
					IPath extPath = workspaceLocation.append(extDir);

					if (extPath.isPrefixOf(projecLocation)) {
						inWorkspacePath = true;
					}
				}
			}
		}

		if (((gradleWorkspaceProject != null) && useDefaultLocation) || inWorkspacePath) {
			GradleUtil.refreshProject(project);
		}
		else {
			GradleUtil.sychronizeProject(projecLocation, monitor);
		}

		return retval;
	}

	@Override
	public synchronized ILiferayProject provide(Object adaptable) {
		return null;
	}

	@Override
	public IStatus validateProjectLocation(String projectName, IPath path) {
		IStatus retval = Status.OK_STATUS;

		if (LiferayWorkspaceUtil.isValidGradleWorkspaceLocation(path)) {
			retval = GradleCore.createErrorStatus("Can not set WorkspaceProject root folder as project directory.");
		}

		return retval;
	}

}