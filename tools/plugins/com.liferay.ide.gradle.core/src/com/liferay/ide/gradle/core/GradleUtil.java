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

package com.liferay.ide.gradle.core;

import com.google.common.base.Optional;

import com.liferay.ide.core.util.FileUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.ConfigurationManager;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.launch.GradleLaunchConfigurationManager;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.binding.Validator;
import org.eclipse.buildship.core.util.binding.Validators;
import org.eclipse.buildship.core.util.gradle.GradleDistribution;
import org.eclipse.buildship.core.util.gradle.GradleDistributionInfo;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.buildship.core.workspace.SynchronizationJob;
import org.eclipse.buildship.core.workspace.WorkspaceOperations;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

/**
 * @author Andy Wu
 * @author Lovett Li
 * @author Charles Wu
 */
@SuppressWarnings("restriction")
public class GradleUtil {

	public static IStatus importGradleProject(IPath dir, IProgressMonitor monitor) {
		if (FileUtil.notExists(dir)) {
			return GradleCore.createErrorStatus("Unbale to find gralde project under " + dir);
		}

		Validator<File> projectDirValidator = Validators.and(
			Validators.requiredDirectoryValidator("Project root directory"),
			Validators.nonWorkspaceFolderValidator("Project root directory"));

		Validator<GradleDistributionInfo> gradleDistributionValidator = GradleDistributionInfo.validator();

		Validator<Boolean> applyWorkingSetsValidator = Validators.nullValidator();
		Validator<List<String>> workingSetsValidator = Validators.nullValidator();
		Validator<File> gradleUserHomeValidator = Validators.optionalDirectoryValidator("Gradle user home");

		ProjectImportConfiguration configuration = new ProjectImportConfiguration(
			projectDirValidator, gradleDistributionValidator, gradleUserHomeValidator, applyWorkingSetsValidator,
			workingSetsValidator);

		// read configuration from gradle preference

		ConfigurationManager configurationManager = CorePlugin.configurationManager();

		WorkspaceConfiguration gradleConfig = configurationManager.loadWorkspaceConfiguration();

		GradleDistribution gradleDistribution = gradleConfig.getGradleDistribution();

		configuration.setProjectDir(dir.toFile());
		configuration.setOverwriteWorkspaceSettings(false);
		configuration.setDistributionInfo(gradleDistribution.getDistributionInfo());
		configuration.setGradleUserHome(gradleConfig.getGradleUserHome());
		configuration.setApplyWorkingSets(false);
		configuration.setBuildScansEnabled(gradleConfig.isBuildScansEnabled());
		configuration.setOfflineMode(gradleConfig.isOffline());
		configuration.setAutoSync(true);

		BuildConfiguration buildConfig = configuration.toBuildConfig();

		GradleWorkspaceManager gradleWorkspaceManager = CorePlugin.gradleWorkspaceManager();

		GradleBuild build = gradleWorkspaceManager.getGradleBuild(buildConfig);

		SynchronizationJob synchronizeJob = new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, build);

		synchronizeJob.schedule();

		return Status.OK_STATUS;
	}

	public static boolean isBuildFile(IFile buildFile) {
		if (FileUtil.exists(buildFile) && "build.gradle".equals(buildFile.getName()) &&
			buildFile.getParent() instanceof IProject) {

			return true;
		}

		return false;
	}

	public static boolean isGradleProject(Object resource) throws CoreException {
		IProject project = null;

		if (resource instanceof IFile) {
			project = ((IFile)resource).getProject();
		}
		else if (resource instanceof IProject) {
			project = (IProject)resource;
		}

		return GradleProjectNature.isPresentOn(project);
	}

	public static void refreshGradleProject(IProject project) {
		GradleWorkspaceManager gradleWorkspaceManager = CorePlugin.gradleWorkspaceManager();

		Optional<GradleBuild> optional = gradleWorkspaceManager.getGradleBuild(project);

		GradleBuild build = optional.get();

		new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, build).schedule();
	}

	public static void runGradleTask(IProject project, String task, IProgressMonitor monitor) throws CoreException {
		runGradleTask(project, new String[] {task}, monitor);
	}

	public static void runGradleTask(IProject project, String[] tasks, IProgressMonitor monitor) throws CoreException {
		GradleLaunchConfigurationManager launchConfigManager = CorePlugin.gradleLaunchConfigurationManager();

		ILaunchConfiguration launchConfiguration = launchConfigManager.getOrCreateRunConfiguration(
			_getRunConfigurationAttributes(project, tasks));

		final ILaunchConfigurationWorkingCopy launchConfigurationWC = launchConfiguration.getWorkingCopy();

		launchConfigurationWC.setAttribute("org.eclipse.debug.ui.ATTR_CAPTURE_IN_CONSOLE", Boolean.TRUE);
		launchConfigurationWC.setAttribute("org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND", Boolean.TRUE);
		launchConfigurationWC.setAttribute("org.eclipse.debug.ui.ATTR_PRIVATE", Boolean.TRUE);

		launchConfigurationWC.doSave();

		launchConfigurationWC.launch(ILaunchManager.RUN_MODE, monitor);
	}

	private static GradleRunConfigurationAttributes _getRunConfigurationAttributes(IProject project, String[] tasks) {
		IPath projecLocation = project.getLocation();

		File rootDir = projecLocation.toFile();

		ConfigurationManager configurationManager = CorePlugin.configurationManager();

		BuildConfiguration buildConfig = configurationManager.loadBuildConfiguration(rootDir);

		String projectDirectoryExpression = null;

		WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations();

		Optional<IProject> gradleProject = workspaceOperations.findProjectByLocation(rootDir);

		if (gradleProject.isPresent()) {
			projectDirectoryExpression = ExpressionUtils.encodeWorkspaceLocation(gradleProject.get());
		}
		else {
			projectDirectoryExpression = rootDir.getAbsolutePath();
		}

		File gradleHome = buildConfig.getGradleUserHome();

		String gradleUserHome = gradleHome == null ? "" : gradleHome.getAbsolutePath();

		List<String> taskList = new ArrayList<>();

		for (String task : tasks) {
			taskList.add(task);
		}

		GradleDistribution gradleDistribution = buildConfig.getGradleDistribution();

		String serializeString = gradleDistribution.serializeToString();

		return new GradleRunConfigurationAttributes(
			taskList, projectDirectoryExpression, serializeString, gradleUserHome, null,
			Collections.<String>emptyList(), Collections.<String>emptyList(), true, true,
			buildConfig.isOverrideWorkspaceSettings(), buildConfig.isOfflineMode(), buildConfig.isBuildScansEnabled());
	}

}