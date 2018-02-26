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

package com.liferay.ide.gradle.action;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.gradle.core.GradleUtil;
import com.liferay.ide.project.core.util.SearchFilesVisitor;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * @author Lovett Li
 * @author Terry Jia
 * @author Andy Wu
 * @author Simon Jiang
 */
public class BuildServiceTaskAction extends GradleTaskAction {

	private IProject getParentProject(IProject checkProject) {

		ProjectConnection connection = null;

		GradleConnector connector =
			GradleConnector.newConnector().forProjectDirectory(
				checkProject.getLocation().toFile());

		connection = connector.connect();

		ModelBuilder<EclipseProject> modelBuilder =
			connection.model(EclipseProject.class);
		EclipseProject eclipseProject = modelBuilder.get();
		EclipseProject serviceParentEclipseProject = eclipseProject.getParent();

		if (serviceParentEclipseProject != null) {
			return CoreUtil.getProject(
				serviceParentEclipseProject.getProjectDirectory());
		}

		return null;
	}

	public boolean getServiceParentProject(IProject checkProject) {

		if (checkProject != null) {
			List<IFile> serviceXmlFiles = new SearchFilesVisitor().searchFiles(
				checkProject, "service.xml");

			if (serviceXmlFiles.size() == 1) {
				IPath servicePath = serviceXmlFiles.get(0).getFullPath();
				IPath serviceProjectLocation = servicePath.removeLastSegments(1);
				IProject serviceProject = CoreUtil.getProject(servicePath.segment(servicePath.segmentCount()-2));

				if ((servicePath.segmentCount() == 2) && checkProject.equals(serviceProject)) {
					project = serviceProject;
					return true;
				}
				else {
					if (servicePath.segmentCount() == 3) {
						String paretnProjectName = serviceProjectLocation.segment(serviceProjectLocation.segmentCount()-2);
						IProject sbProject = CoreUtil.getProject(paretnProjectName);
						if ( checkProject.equals(sbProject)) {
							project = sbProject;
							return true;
						}
					}
					else {
						return false;
					}
				}
			}
			else if (serviceXmlFiles.size() > 1) {
				return false;
			}
			else if (serviceXmlFiles.size() == 0) {
				return getServiceParentProject(getParentProject(checkProject));
			}
		}
		else {
			return false;
		}

		return false;
	}

	@Override
	protected void setEnableTaskAction(IAction action) {

		boolean enabled = getServiceParentProject(project);
		action.setEnabled(enabled);
	}

	protected void afterTask() {

		GradleUtil.refreshGradleProject(project);
	}

	@Override
	protected String getGradleTask() {

		return "buildService";
	}

}
