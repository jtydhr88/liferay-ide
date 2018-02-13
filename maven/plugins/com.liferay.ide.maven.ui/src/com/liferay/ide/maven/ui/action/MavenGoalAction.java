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

import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.maven.core.ILiferayMavenConstants;
import com.liferay.ide.maven.core.MavenUtil;
import com.liferay.ide.maven.ui.LiferayMavenUI;
import com.liferay.ide.maven.ui.MavenUIProjectBuilder;
import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.ui.action.AbstractObjectAction;

import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 * @author Simon Jiang
 */

public abstract class MavenGoalAction extends AbstractObjectAction {

	public MavenGoalAction() {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (fSelection instanceof IStructuredSelection) {
			Object[] elems = ((IStructuredSelection)fSelection).toArray();

			Object elem = elems[0];

			if (elem instanceof IFile) {
				pomXmlFile = (IFile)elem;

				project = pomXmlFile.getProject();
			}
			else if (elem instanceof IProject) {
				project = (IProject)elem;

				pomXmlFile = project.getFile("pom.xml");
			}

			if (FileUtil.notExists(pomXmlFile)) {
				setEnableTaskAction(action);
				return;
			}

			setEnableTaskAction(action);
		}
	}

	protected void setEnableTaskAction(IAction action) {
		action.setEnabled(true);
	}

	@Override
	public void run(IAction action) {

		if (fSelection instanceof IStructuredSelection) {
			IProject p = project;

			try {
				String pluginKey =
					ILiferayMavenConstants.LIFERAY_MAVEN_PLUGINS_GROUP_ID + ":" +
						ILiferayMavenConstants.LIFERAY_MAVEN_PLUGIN_ARTIFACT_ID;

				plugin = MavenUtil.getPlugin(MavenUtil.getProjectFacade(p), pluginKey, new NullProgressMonitor());

				if (plugin == null) {
					plugin = MavenUtil.getPlugin(
						MavenUtil.getProjectFacade(p), getGroupId() + ":" + getPluginKey(),
						new NullProgressMonitor());
				}
			}
			catch (CoreException ce) {
			}

			Job job = new Job(p.getName() + " - " + getMavenGoals()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (plugin == null) {
							return ProjectUI.createErrorStatus("Can't find any plugins for " + getMavenGoals());
						}

						monitor.beginTask(getMavenGoals(), 100);

						_runMavenGoal(pomXmlFile, getMavenGoals(), monitor);

						monitor.worked(80);

						p.refreshLocal(IResource.DEPTH_INFINITE, monitor);

						monitor.worked(10);

						updateProject(monitor);

						monitor.worked(10);
					}
					catch (Exception e) {
						return ProjectUI.createErrorStatus("Error running Maven goal " + getMavenGoals(), e);
					}

					return Status.OK_STATUS;
				}

			};

			job.schedule();
		}
	}

	public Plugin plugin = null;
	protected IProject project = null;
	protected IFile pomXmlFile = null;

	protected String getGroupId() {
		return ILiferayMavenConstants.NEW_LIFERAY_MAVEN_PLUGINS_GROUP_ID;
	}

	protected abstract String getMavenGoals();

	protected String getPluginKey() {
		return "";
	}

	protected void updateProject(IProgressMonitor monitor) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (CoreException ce) {
			LiferayMavenUI.logError("Error refreshing project after " + getMavenGoals(), ce);
		}
	}

	private void _runMavenGoal(IFile pomFile, String goal, IProgressMonitor monitor) throws CoreException {
		IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();

		IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());

		MavenUIProjectBuilder builder = new MavenUIProjectBuilder(pomFile.getProject());

		builder.runMavenGoal(projectFacade, goal, "run", monitor);
	}

}
