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

package com.liferay.ide.project.core;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;

/**
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public abstract class AbstractProjectBuilder implements IProjectBuilder {

	public AbstractProjectBuilder(IProject project) {
		_project = project;
	}

	public IProject getProject() {
		return _project;
	}

	public IStatus initBundle(IProject project, String bundleUrl, IProgressMonitor monitor) {
		if (CoreUtil.isNotNullOrEmpty(bundleUrl)) {
			IPath bundlesLocation = LiferayWorkspaceUtil.getHomeLocation(_project);

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
				ProjectCore.logError(ce);
			}
		}

		return Status.OK_STATUS;
	}

	protected IFile getDocrootFile(String path) {
		IFolder docroot = CoreUtil.getDefaultDocrootFolder(_project);

		if (FileUtil.notExists(docroot)) {
			return null;
		}

		IFile file = docroot.getFile(new Path(path));

		if (FileUtil.exists(file)) {
			return file;
		}

		return null;
	}

	private IProject _project;

}