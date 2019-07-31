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

package com.liferay.ide.project.core.samples.internal;

import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.samples.NewSampleOp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author Terry Jia
 */
public class TargetLiferayVersionValidationService extends ValidationService implements SapphireContentAccessor {

	@Override
	protected Status compute() {
		String liferayVersion = get(_op().getLiferayVersion());

		Job job = _versionCachedMap.get(liferayVersion);

		if (job == null) {
			try {
				job = new Job("Downloading liferay blade samples archive for " + liferayVersion) {

					@Override
					protected IStatus run(IProgressMonitor progressMonitor) {
						try {
							BladeCLI.execute("samples -c -v " + liferayVersion);
						}
						catch (Exception e) {
							return ProjectCore.createErrorStatus(e);
						}

						refresh();

						return org.eclipse.core.runtime.Status.OK_STATUS;
					}

				};

				_versionCachedMap.put(liferayVersion, job);

				job.schedule();
			}
			catch (Exception e) {
				return Status.createErrorStatus(e);
			}
		}

		return Status.createOkStatus();
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private Map<String, Job> _versionCachedMap = new HashMap<>();

}