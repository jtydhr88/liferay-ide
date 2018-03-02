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

package com.liferay.ide.core.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

/**
 * @author Andy Wu
 */
public class ValidationUtil {

	public static boolean isExistingProjectName(String projectName) {
		IProject[] projects = CoreUtil.getAllProjects();

		for (IProject project : projects) {
			if (projectName.equalsIgnoreCase(project.getName())) {
				return true;
			}
		}

		return false;
	}

	public static boolean isProjectTargetDirFile(File file) {
		IProject project = CoreUtil.getProject(file);

		IFolder targetFolder = project.getFolder("target");

		boolean inTargetDir = false;

		File targetDir = null;

		if (targetFolder.exists()) {
			targetDir = targetFolder.getLocation().toFile();

			try {
				inTargetDir = file.getCanonicalPath().startsWith(targetDir.getCanonicalPath());
			}
			catch (IOException ioe) {
			}
		}

		return inTargetDir;
	}

}