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

package com.liferay.ide.project.ui.repl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardSourcePathProvider;

/**
 * @author Terry Jia
 */
public class ScrapbookSourcepathProvider extends StandardSourcePathProvider {

	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH,
				true);
		IRuntimeClasspathEntry[] entries = null;
		if (useDefault) {
			// the default source lookup path is the classpath of the associated project
			IJavaProject project = JavaRuntime.getJavaProject(configuration);
			entries = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		} else {
			// recover persisted source path
			entries = recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH);
		}
		return entries;
	}
}
