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

package com.liferay.ide.sdk.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Greg Amerson
 */
public abstract class SDKJob extends Job {

	public SDKJob(String name) {
		super(name);
	}

	protected IProject getProject() {
		return project;
	}

	protected SDK getSDK() {
		if (project == null) {
			return null;
		}

		SDK retval = null;

		// try to determine SDK based on project location

		IPath sdkLocation = this.project.getRawLocation().removeLastSegments(2);

		retval = SDKManager.getInstance().getSDK(sdkLocation);

		if (retval == null) {
			retval = SDKUtil.createSDKFromLocation(sdkLocation);

			if ((retval != null) && retval.isValid()) {
				SDKManager.getInstance().addSDK(retval);
			}
		}

		return retval;
	}

	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	protected void setProject(IProject project) {
		this.project = project;
	}

	protected IProject project;

}