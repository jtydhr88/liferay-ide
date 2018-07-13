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

package com.liferay.ide.project.core.modules;

import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.sapphire.java.JavaPackageName;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author Simon Jiang
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class JavaPackageNameValidationService extends ValidationService {

	@Override
	protected Status compute() {
		Status retval = Status.createOkStatus();

		NewLiferayComponentOp op = _op();

		String projectName = op.getProjectName().text(true);

		if (projectName != null) {
			IProject project = CoreUtil.getProject(projectName);

			if (project == null) {
				return Status.createErrorStatus("Unable to find project " + projectName);
			}

			ILiferayProject liferayProject = LiferayCore.create(project);

			IFolder sourceFolder = liferayProject.getSourceFolder("java");

			if (FileUtil.notExists(sourceFolder)) {
				return Status.createErrorStatus("Unable to find any 'java' source folders.");
			}
		}

		JavaPackageName packageName = op.getPackageName().content(true);

		if (packageName != null) {
			IStatus status = JavaConventions.validatePackageName(
				packageName.toString(), CompilerOptions.VERSION_1_7, CompilerOptions.VERSION_1_7);

			int packageNameStatus = status.getSeverity();

			if (packageNameStatus == IStatus.ERROR) {
				retval = Status.createErrorStatus("Invalid package name");
			}
		}

		return retval;
	}

	private NewLiferayComponentOp _op() {
		return context(NewLiferayComponentOp.class);
	}

}