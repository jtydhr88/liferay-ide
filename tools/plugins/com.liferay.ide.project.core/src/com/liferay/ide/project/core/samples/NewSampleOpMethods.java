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

package com.liferay.ide.project.core.samples;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BladeCLI;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;

/**
 * @author Terry Jia
 */
public class NewSampleOpMethods {

	public static final Status execute(NewSampleOp newSampleOp, ProgressMonitor progressMonitor) {
		Status retval = Status.createOkStatus();

		Throwable errorStack = null;

		String projectName = _getter.get(newSampleOp.getProjectName());

		Path location = _getter.get(newSampleOp.getLocation());

		String liferayVersion = _getter.get(newSampleOp.getLiferayVersion());

		String buildType = _getter.get(newSampleOp.getBuildType());

		String sampleName = _getter.get(newSampleOp.getSampleName());

		StringBuffer sb = new StringBuffer();

		sb.append("samples");
		sb.append(" ");
		sb.append("-b ");
		sb.append(buildType);
		sb.append(" ");
		sb.append("-v ");
		sb.append(liferayVersion);
		sb.append(" ");
		sb.append("-d ");
		sb.append(location.toOSString());
		sb.append(" ");
		sb.append(sampleName);

		try {
			BladeCLI.execute(sb.toString());

			Path oldPath = location.append(sampleName);

			File oldFile = oldPath.toFile();

			Path newPath = location.append(projectName);

			File newFile = newPath.toFile();

			oldFile.renameTo(newFile);

			Job job = new Job("Openning project " + projectName) {

				@Override
				protected IStatus run(IProgressMonitor progressMonitor) {
					org.eclipse.core.runtime.Path projectLocation = new org.eclipse.core.runtime.Path(
						newFile.getPath());

					try {
						CoreUtil.openProject(projectName, projectLocation, progressMonitor);
					}
					catch (CoreException ce) {
						return ProjectCore.createErrorStatus(ce);
					}

					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

			};

			job.schedule();
		}
		catch (Exception e) {
			errorStack = e;
		}

		if (errorStack != null) {
			String readableStack = CoreUtil.getStackTrace(errorStack);

			ProjectCore.logError(readableStack);

			return Status.createErrorStatus(readableStack + "\t Please see Eclipse error log for more details.");
		}

		return retval;
	}

	private static final SapphireContentAccessor _getter = new SapphireContentAccessor() {
	};

}