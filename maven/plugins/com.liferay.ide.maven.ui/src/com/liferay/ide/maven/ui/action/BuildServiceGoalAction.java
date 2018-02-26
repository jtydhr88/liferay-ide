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

package com.liferay.ide.maven.ui.action;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.maven.core.ILiferayMavenConstants;
import com.liferay.ide.maven.core.MavenGoalUtil;
import com.liferay.ide.maven.core.MavenProjectBuilder;
import com.liferay.ide.maven.core.MavenUtil;
import com.liferay.ide.maven.ui.LiferayMavenUI;
import com.liferay.ide.project.core.util.SearchFilesVisitor;

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */
public class BuildServiceGoalAction extends MavenGoalAction {

	@Override
	protected String getMavenGoals() {
		return MavenGoalUtil.getMavenBuildServiceGoal(plugin);
	}

	@Override
	protected String getPluginKey() {
		return ILiferayMavenConstants.LIFERAY_MAVEN_PLUGINS_SERVICE_BUILDER_KEY;
	}

	private IProject getParentProject(IProject checkProject) {

		IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(checkProject, new NullProgressMonitor());
		MavenProject parent = projectFacade.getMavenProject().getParent();
		return parent!=null?CoreUtil.getProject(parent.getName()):null;
	}

	public boolean getServiceParentProject(IProject checkProject) {

		if (checkProject != null) {
			List<IFile> serviceXmlFiles = new MavenSearchFilesVisitor().searchFiles(
				checkProject, "service.xml");

			if (serviceXmlFiles.size() == 1 ) {
				IPath servicePath = serviceXmlFiles.get(0).getFullPath();
				IPath serviceProjectLocation = servicePath.removeLastSegments(1);
				IProject serviceProject = CoreUtil.getProject(servicePath.segment(servicePath.segmentCount()-2));

				if ((servicePath.segmentCount() == 2) && checkProject.equals(serviceProject)) {
					project = serviceProject;
					pomXmlFile = project.getFile("pom.xml");
					return true;
				}
				else {
					if (servicePath.segmentCount() == 3) {
						String paretnProjectName = serviceProjectLocation.segment(serviceProjectLocation.segmentCount()-2);
						IProject sbProject = CoreUtil.getProject(paretnProjectName);
						if ( checkProject.equals(sbProject)) {
							project = serviceProject;
							pomXmlFile = project.getFile("pom.xml");
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
	protected void updateProject(IProgressMonitor monitor) {
		MavenProjectBuilder builder = new MavenProjectBuilder(project);

		try {
			IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(project, monitor);

			builder.refreshSiblingProject(projectFacade, monitor);
		}
		catch (CoreException ce) {
			LiferayMavenUI.logError("Unable to refresh sibling project", ce);
		}
	}

	@Override
	protected void setEnableTaskAction(IAction action) {
		boolean enabled = getServiceParentProject(project);
		action.setEnabled(enabled);
	}

	private class MavenSearchFilesVisitor extends SearchFilesVisitor{

		public boolean visit(IResourceProxy resourceProxy) {
			if ((resourceProxy.getType() == IResource.FILE) && resourceProxy.getName().equals(searchFileName)) {
				IResource resource = resourceProxy.requestResource();

				if (resource.exists() && !resource.getFullPath().toOSString().contains("/target/")) {
					resources.add((IFile)resource);
				}
			}

			return true;
		}
	}
}