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

package com.liferay.ide.service.core;

import com.liferay.ide.core.Artifact;
import com.liferay.ide.core.IBundleProject;
import com.liferay.ide.core.ILiferayPortal;
import com.liferay.ide.core.ILiferayProject;
import com.liferay.ide.core.IProjectBuilder;
import com.liferay.ide.core.IWebProject;
import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.ServiceBuilderHelper;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.project.core.util.WizardUtil;
import com.liferay.ide.service.core.operation.INewServiceBuilderDataModelProperties;

import java.io.ByteArrayInputStream;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Gregory Amerson
 * @author Cindy Li
 * @author Terry Jia
 */
public class AddServiceBuilderOperation
	extends AbstractDataModelOperation implements INewServiceBuilderDataModelProperties {

	public AddServiceBuilderOperation(IDataModel model) {
		super(model);

		Bundle bundle = FrameworkUtil.getBundle(AddServiceBuilderOperation.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceTracker = new ServiceTracker<>(bundleContext, ServiceBuilderHelper.class, null);

		_serviceTracker.open();
	}

	public void createDefaultServiceBuilderFile(IFile serviceBuilderFile, IProgressMonitor monitor)
		throws CoreException {

		String descriptorVersion = "7.0.0";

		IProject project = serviceBuilderFile.getProject();

		try {
			ILiferayProject liferayProject = LiferayCore.create(ILiferayProject.class, project);

			ILiferayPortal portal = liferayProject.adapt(ILiferayPortal.class);

			Version portalVersion = null;

			if (portal != null) {
				portalVersion = new Version(portal.getVersion());
			}
			else {
				IWorkspaceProject workspaceProject = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

				if (workspaceProject != null) {
					String targetPlatformVersion = workspaceProject.getTargetPlatformVersion();

					if (targetPlatformVersion != null) {
						portalVersion = new Version(workspaceProject.getTargetPlatformVersion());
					}
				}
			}

			if (portalVersion != null) {
				descriptorVersion = portalVersion.getMajor() + "." + portalVersion.getMinor() + ".0";
			}
		}
		catch (Exception e) {
			ProjectCore.logError("Could not determine liferay runtime version", e);
			descriptorVersion = "7.0.0";
		}

		WizardUtil.createDefaultServiceBuilderFile(
			serviceBuilderFile, descriptorVersion, getDataModel().getBooleanProperty(USE_SAMPLE_TEMPLATE),
			getDataModel().getStringProperty(PACKAGE_PATH), getDataModel().getStringProperty(NAMESPACE),
			getDataModel().getStringProperty(AUTHOR), monitor);

		IBundleProject bundleProject = LiferayCore.create(IBundleProject.class, project);

		if (bundleProject != null) {
			_updateBndFile(project);

			IProjectBuilder projectBuilder = bundleProject.adapt(IProjectBuilder.class);

			IWorkspaceProject workspaceProject = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

			boolean hasTargetPlatform = false;

			if (workspaceProject != null) {
				String targetPlatformVersion = workspaceProject.getTargetPlatformVersion();

				if (targetPlatformVersion != null) {
					hasTargetPlatform = true;
				}
			}

			ServiceBuilderHelper serviceBuilderHelper = _serviceTracker.getService();

			List<Artifact> dependencies = serviceBuilderHelper.getServiceBuilderDependencies();

			if (hasTargetPlatform) {
				dependencies.stream(
				).forEach(
					dependency -> {
						dependency.setVersion(null);
					}
				);
			}

			projectBuilder.updateDependencies(project, dependencies);

			IFile buildGradle = project.getFile("build.gradle");

			if (FileUtil.exists(buildGradle)) {
				String contents = FileUtil.readContents(buildGradle, true);

				if (!contents.contains("buildService")) {
					contents = contents + "buildService {apiDir = \"./src/main/java\"}";

					try (ByteArrayInputStream bis = new ByteArrayInputStream(contents.getBytes("UTF-8"))) {
						buildGradle.setContents(bis, IFile.FORCE, null);
					}
					catch (Exception e) {
						ProjectCore.logError(e);
					}
				}
			}
		}

		getDataModel().setProperty(CREATED_SERVICE_FILE, serviceBuilderFile);
	}

	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus retval = null;

		IStatus status = _createServiceBuilderFile(getTargetProject(), monitor);

		if (!status.isOK()) {
			return status;
		}

		return retval;
	}

	@SuppressWarnings("restriction")
	protected IProject getTargetProject() {
		String projectName = model.getStringProperty(PROJECT_NAME);

		return ProjectUtil.getProject(projectName);
	}

	private IStatus _createServiceBuilderFile(IProject project, IProgressMonitor monitor) {

		// IDE-110 IDE-648

		IFile serviceBuilderFile = null;

		IBundleProject bundleProject = LiferayCore.create(IBundleProject.class, project);

		if (bundleProject != null) {
			serviceBuilderFile = project.getFile(getDataModel().getStringProperty(SERVICE_FILE));
		}
		else {
			IWebProject webproject = LiferayCore.create(IWebProject.class, project);

			if ((webproject == null) || (webproject.getDefaultDocrootFolder() == null)) {
				return ServiceCore.createErrorStatus("Could not find webapp root folder.");
			}

			IFolder defaultDocroot = webproject.getDefaultDocrootFolder();
			Path path = new Path("WEB-INF/" + getDataModel().getStringProperty(SERVICE_FILE));

			serviceBuilderFile = defaultDocroot.getFile(path);
		}

		if (FileUtil.notExists(serviceBuilderFile)) {
			try {
				createDefaultServiceBuilderFile(serviceBuilderFile, monitor);
			}
			catch (Exception ex) {
				return ServiceCore.createErrorStatus(ex);
			}
		}

		return Status.OK_STATUS;
	}

	private void _updateBndFile(IProject project) throws CoreException {
		IFile bndFile = project.getFile("bnd.bnd");

		if (FileUtil.exists(bndFile)) {
			String contents = FileUtil.readContents(bndFile, true);

			if (!contents.contains("Liferay-Service")) {
				contents = contents + "Liferay-Service: true";

				try (ByteArrayInputStream bis = new ByteArrayInputStream(contents.getBytes("UTF-8"))) {
					bndFile.setContents(bis, IFile.FORCE, null);
				}
				catch (Exception e) {
					ProjectCore.logError(e);
				}
			}
		}
	}

	private ServiceTracker<ServiceBuilderHelper, ServiceBuilderHelper> _serviceTracker;

}