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

package com.liferay.ide.gradle.ui;

import com.liferay.ide.gradle.core.GradleUtil;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import java.io.File;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author Terry Jia
 */
public class WatchableProjectPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IProject project = null;

		if (receiver instanceof IProject) {
			project = (IProject)receiver;

			IFile buildFile = project.getFile("build.gradle");

			if (LiferayWorkspaceUtil.inLiferayWorkspace(project) || LiferayWorkspaceUtil.isValidWorkspace(project)) {

				IPath projectLocation = project.getLocation();

				File projectFile = projectLocation.toFile();

				File parentFile = projectFile.getParentFile();

				String parentName = parentFile.getName();

				if (!parentName.equals("modules")) {
					return false;
				}

				IProject workspaceProject = LiferayWorkspaceUtil.getWorkspaceProject();

				buildFile = workspaceProject.getFile("settings.gradle");
			}

			return GradleUtil.isWatchableProject(buildFile);
		}

		return false;
	}

}