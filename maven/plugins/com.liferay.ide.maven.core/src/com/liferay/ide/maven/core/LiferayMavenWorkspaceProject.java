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

package com.liferay.ide.maven.core;

import com.liferay.ide.project.core.IProjectBuilder;
import com.liferay.ide.project.core.IWorkspaceProjectBuilder;
import com.liferay.ide.project.core.LiferayWorkspaceProject;

import org.eclipse.core.resources.IProject;

/**
 * @author Simon Jiang
 */
public class LiferayMavenWorkspaceProject extends LiferayWorkspaceProject {

	public LiferayMavenWorkspaceProject(IProject project) {
		super(project);
	}

	@Override
	public <T> T adapt(Class<T> adapterType) {
		if (IProjectBuilder.class.equals(adapterType) || IWorkspaceProjectBuilder.class.equals(adapterType)) {
			IProjectBuilder projectBuilder = new MavenProjectBuilder(getProject());

			return adapterType.cast(projectBuilder);
		}

		return super.adapt(adapterType);
	}

}