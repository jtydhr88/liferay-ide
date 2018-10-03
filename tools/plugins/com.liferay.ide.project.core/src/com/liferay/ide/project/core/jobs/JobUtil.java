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

package com.liferay.ide.project.core.jobs;

import com.liferay.ide.core.ILiferayProjectProvider;
import com.liferay.ide.project.core.ProjectCore;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Charles Wu
 */
public class JobUtil {

	public static Job getJobByName(String name) {
		assert name != null;

		Job[] jobs = _jobManager.find(null);

		for (Job job : jobs) {
			if (name.equals(job.getName())) {
				return job;
			}
		}

		return null;
	}

	public static void waitForLiferayProjectJob() {
		Job[] jobs = _jobManager.find(null);

		for (Job job : jobs) {
			if (job.getProperty(ILiferayProjectProvider.LIFERAY_PROJECT_JOB) != null) {
				try {
					job.join();
				}
				catch (InterruptedException ie) {
					ProjectCore.logError(ie);
				}
			}
		}
	}

	private static IJobManager _jobManager = Job.getJobManager();

}