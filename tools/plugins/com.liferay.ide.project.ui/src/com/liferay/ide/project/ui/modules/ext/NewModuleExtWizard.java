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

package com.liferay.ide.project.ui.modules.ext;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.jobs.JobNameId;
import com.liferay.ide.project.core.jobs.JobUtil;
import com.liferay.ide.project.core.jobs.LiferayJob;
import com.liferay.ide.project.core.modules.ext.NewModuleExtOp;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.project.ui.modules.BaseProjectWizard;
import com.liferay.ide.ui.util.UIUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

/**
 * @author Charles Wu
 */
public class NewModuleExtWizard extends BaseProjectWizard<NewModuleExtOp> {

	public NewModuleExtWizard() {
		super(_createDefaultOp(), DefinitionLoader.sdef(NewModuleExtWizard.class).wizard());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		NewModuleExtOp op = element().nearest(NewModuleExtOp.class);

		String version = SapphireUtil.getContent(op.getTargetPlatformVersion());

		if (CoreUtil.isNullOrEmpty(version)) {
			return;
		}

		Job job = JobUtil.getJobByName(JobNameId.CALCULATE_ARTIFACTS_JOB_NAME);

		if (job != null) {
			return;
		}

		job = new LiferayJob(JobNameId.CALCULATE_ARTIFACTS_JOB_NAME) {

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				if (CoreUtil.isNotNullOrEmpty(version)) {
					IWorkspaceProject gradleWorkspaceProject = LiferayWorkspaceUtil.getGradleWorkspaceProject();

					try {
						gradleWorkspaceProject.getTargetPlatformArtifacts();
					}
					catch (Exception e) {

						// Do not want to show the exception to user

					}
				}

				return Status.OK_STATUS;
			}

		};

		job.schedule();
	}

	@Override
	protected void performPostFinish() {
		super.performPostFinish();

		final NewModuleExtOp op = element().nearest(NewModuleExtOp.class);

		final IProject project = CoreUtil.getProject(SapphireUtil.getContent(op.getProjectName()));

		try {
			addToWorkingSets(project);
		}
		catch (Exception ex) {
			ProjectUI.logError("Unable to add project to working set", ex);
		}

		openLiferayPerspective(project);
	}

	private static NewModuleExtOp _createDefaultOp() {
		return NewModuleExtOp.TYPE.instantiate();
	}

}