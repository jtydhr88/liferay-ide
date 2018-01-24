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

import com.liferay.ide.core.LiferayNature;
import com.liferay.ide.project.core.NewLiferayProjectProvider;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.model.ProjectName;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.NewLiferayModuleProjectOp;
import com.liferay.ide.project.core.modules.PropertyKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IProjectCreationListener;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.platform.PathBridge;

/**
 * @author Joye Luo
 * @author Simon Jiang
 */
public class LiferayMavenModuleProjectProvider
	extends LiferayMavenProjectProvider implements NewLiferayProjectProvider<NewLiferayModuleProjectOp> {

	@Override
	public IStatus createNewProject(NewLiferayModuleProjectOp op, IProgressMonitor monitor) throws CoreException {
		IStatus retval = Status.OK_STATUS;

		String projectName = op.getProjectName().content();

		IPath location = PathBridge.create(op.getLocation().content());

		String className = op.getComponentName().content();

		String serviceName = op.getServiceName().content();

		String packageName = op.getPackageName().content();

		ElementList<PropertyKey> propertyKeys = op.getPropertyKeys();

		List<String> properties = new ArrayList<>();

		for (PropertyKey propertyKey : propertyKeys) {
			properties.add(propertyKey.getName().content(true) + "=" + propertyKey.getValue().content(true));
		}

		File targetDir = location.toFile();

		targetDir.mkdirs();

		String projectTemplateName = op.getProjectTemplateName().content();

		StringBuilder sb = new StringBuilder();

		sb.append("create ");
		sb.append("-d \"");
		sb.append(targetDir.getAbsolutePath());
		sb.append("\" ");
		sb.append("-b ");
		sb.append("maven ");
		sb.append("-t ");
		sb.append(projectTemplateName);
		sb.append(" ");

		if (className != null) {
			sb.append("-c ");
			sb.append(className);
			sb.append(" ");
		}

		if (serviceName != null) {
			sb.append("-s ");
			sb.append(serviceName);
			sb.append(" ");
		}

		if (packageName != null) {
			sb.append("-p ");
			sb.append(packageName);
			sb.append(" ");
		}

		sb.append("\"");
		sb.append(projectName);
		sb.append("\" ");

		try {
			BladeCLI.execute(sb.toString());

			ElementList<ProjectName> projectNames = op.getProjectNames();

			projectNames.insert().setName(projectName);

			if (projectTemplateName.equals("service-builder")) {
				projectNames.insert().setName(projectName + "-api");
				projectNames.insert().setName(projectName + "-service");
			}

			IPath projectLocation = location;

			String lastSegment = location.lastSegment();

			if ((location != null) && (location.segmentCount() > 0)) {
				if (!lastSegment.equals(projectName)) {
					projectLocation = location.append(projectName);
				}
			}

			MavenUtil.importProject(projectLocation.toPortableString(), new LiferaMavenModuleImportListener(), monitor);
		}
		catch (Exception e) {
			retval = ProjectCore.createErrorStatus("can't create module project.", e);
		}

		return retval;
	}

	private class LiferaMavenModuleImportListener implements IProjectCreationListener{

		@Override
		public void projectCreated(IProject project) {

			if (_isMavenBundlePlugin(project)) {
				try {
					LiferayNature.addLiferayNature(project, new NullProgressMonitor());
				} catch (CoreException e) {
				}
			}

		}

		private boolean _isMavenBundlePlugin(IProject project) {
			NullProgressMonitor monitor = new NullProgressMonitor();

			IMavenProjectFacade facade = MavenUtil.getProjectFacade(project, monitor);

			if (facade != null) {
				try {
					MavenProject mavenProject = facade.getMavenProject(new NullProgressMonitor());

					if ((mavenProject != null) && "bundle".equals(mavenProject.getPackaging())) {
						Plugin mavenBundlePlugin = MavenUtil.getPlugin(
							facade, ILiferayMavenConstants.MAVEN_BUNDLE_PLUGIN_KEY, monitor);

						if (mavenBundlePlugin != null) {
							return true;
						}
					}
					else if ((mavenProject != null) && "jar".equals(mavenProject.getPackaging())) {
						Plugin bndMavenPlugin = MavenUtil.getPlugin(
							facade, ILiferayMavenConstants.BND_MAVEN_PLUGIN_KEY, monitor);

						if (bndMavenPlugin != null) {
							return true;
						}
					}
				}
				catch (CoreException ce) {
				}
			}

			return false;
		}
	}

	@Override
	public IStatus validateProjectLocation(String projectName, IPath path) {
		return Status.OK_STATUS;
	}

}