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

package com.liferay.ide.project.core.util;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.ZipUtil;
import com.liferay.ide.project.core.BinaryProjectRecord;
import com.liferay.ide.project.core.IPortletFramework;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.ProjectRecord;
import com.liferay.ide.project.core.facet.IPluginFacetConstants;
import com.liferay.ide.project.core.model.NewLiferayPluginProjectOp;
import com.liferay.ide.sdk.core.ISDKConstants;
import com.liferay.ide.sdk.core.SDK;
import com.liferay.ide.sdk.core.SDKManager;
import com.liferay.ide.sdk.core.SDKUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.internal.BridgedRuntime;

/**
 * @author <a href="mailto:kamesh.sampath@hotmail.com">Kamesh Sampath</a>
 * @author Terry Jia
 * @author Simon Jiang
 */
@SuppressWarnings("restriction")
public class ProjectImportUtil {

	/**
	 * This method was added as part of the IDE-381 fix, this method will collect
	 * all the binaries based on the binaries list
	 *
	 * @return true if the directory has some binaries
	 */
	public static boolean collectBinariesFromDirectory(
		Collection<File> binaryProjectFiles, File directory, boolean recurse, IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return false;
		}

		monitor.subTask(NLS.bind(Msgs.checking, directory.getPath()));

		List<String> wildCards = Arrays.asList(ISDKConstants.BINARY_PLUGIN_PROJECT_WILDCARDS);

		WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(wildCards);

		Collection<File> contents = FileUtils.listFiles(directory, wildcardFileFilter, DirectoryFileFilter.INSTANCE);

		if (contents == null) {
			return false;
		}
		else {
			for (File file : contents) {
				if (!binaryProjectFiles.contains(file) && isValidLiferayPlugin(file)) {
					binaryProjectFiles.add(file);
				}
			}
		}

		return true;
	}

	/**
	 * @param dataModel
	 * @param pluginBinaryRecord
	 * @param liferaySDK
	 * @return
	 * @throws IOException
	 */
	public static ProjectRecord createSDKPluginProject(
			BridgedRuntime bridgedRuntime, BinaryProjectRecord pluginBinaryRecord, SDK liferaySDK)
		throws IOException {

		ProjectRecord projectRecord = null;

		if (!pluginBinaryRecord.isConflicts()) {
			String displayName = pluginBinaryRecord.getDisplayName();
			File binaryFile = pluginBinaryRecord.getBinaryFile();
			IPath projectPath = null;
			IPath sdkPluginProjectFolder = liferaySDK.getLocation();

			// IDE-110 IDE-648

			String webappRootFolder = null;
			IProgressMonitor npm = new NullProgressMonitor();

			ArrayList<String> arguments = new ArrayList<>();

			arguments.add(displayName);
			arguments.add(displayName);

			// Create Project

			if (pluginBinaryRecord.isHook()) {
				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.HOOK_PLUGIN_PROJECT_FOLDER);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "hook", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.HOOK_PLUGIN_SDK_CONFIG_FOLDER;
			}
			else if (pluginBinaryRecord.isPortlet()) {
				IPortletFramework[] portletFrameworks = ProjectCore.getPortletFrameworks();
				String portletFrameworkName = null;

				for (int i = 0; i < portletFrameworks.length; i++) {
					IPortletFramework portletFramework = portletFrameworks[i];

					if (portletFramework.isDefault() && !portletFramework.isAdvanced()) {
						portletFrameworkName = portletFramework.getShortName();
						break;
					}
				}

				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.PORTLET_PLUGIN_PROJECT_FOLDER);

				arguments.add(portletFrameworkName);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "portlet", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.PORTLET_PLUGIN_SDK_CONFIG_FOLDER;
			}
			else if (pluginBinaryRecord.isTheme()) {
				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.THEME_PLUGIN_PROJECT_FOLDER);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "theme", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.THEME_PLUGIN_SDK_CONFIG_FOLDER;
			}
			else if (pluginBinaryRecord.isLayoutTpl()) {
				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.LAYOUTTPL_PLUGIN_PROJECT_FOLDER);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "layouttpl", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.LAYOUTTPL_PLUGIN_SDK_CONFIG_FOLDER;
			}
			else if (pluginBinaryRecord.isExt()) {
				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.EXT_PLUGIN_PROJECT_FOLDER);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "ext", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.EXT_PLUGIN_SDK_CONFIG_FOLDER;
			}
			else if (pluginBinaryRecord.isWeb()) {
				sdkPluginProjectFolder = sdkPluginProjectFolder.append(ISDKConstants.WEB_PLUGIN_PROJECT_FOLDER);

				try {
					projectPath = liferaySDK.createNewProject(
						displayName, arguments, "web", sdkPluginProjectFolder.toOSString(), npm);
				}
				catch (CoreException ce) {
					ProjectCore.logError(ce);
				}

				webappRootFolder = IPluginFacetConstants.WEB_PLUGIN_SDK_CONFIG_FOLDER;
			}

			// Extract the contents

			File webappRoot = new File(projectPath.toFile(), webappRootFolder);

			ZipUtil.unzip(binaryFile, webappRoot);

			// IDE-569 check to see if the project already has .project

			File projectFile = new File(projectPath.toFile(), ".project");

			if (FileUtil.exists(projectFile)) {
				projectRecord = new ProjectRecord(projectFile);
			}
			else {
				projectRecord = new ProjectRecord(projectPath.toFile());
			}
		}

		return projectRecord;
	}

	/**
	 * This will create the Eclipse Workspace projects
	 *
	 * @param monitor
	 * @throws CoreException
	 */
	public static void createWorkspaceProjects(
			Object[] projects, IRuntime runtime, String sdkLocation, IProgressMonitor monitor)
		throws CoreException {

		List<IProject> createdProjects = new ArrayList<>();

		monitor.beginTask(Msgs.creatingSDKWorkspaceProjects, projects.length);

		if ((projects != null) && (projects.length > 0)) {
			SDK sdk = SDKManager.getInstance().getSDK(new Path(sdkLocation));

			// need to add the SDK to workspace if not already available.

			if (sdk == null) {
				sdk = SDKUtil.createSDKFromLocation(new Path(sdkLocation));
			}

			if ((sdk != null) && sdk.isValid() && !(SDKManager.getInstance().containsSDK(sdk))) {
				SDKManager.getInstance().addSDK(sdk);
			}
		}

		for (int i = 0; i < projects.length; i++) {
			if (projects[i] instanceof ProjectRecord) {
				IProject project = importProject((ProjectRecord)projects[i], runtime, sdkLocation, monitor);

				if (project != null) {
					createdProjects.add(project);
					monitor.worked(createdProjects.size());
				}
			}
		}
	}

	public static String getConfigFileLocation(String configFile) {
		StringBuilder sb = new StringBuilder("WEB-INF/");

		sb.append(configFile);

		return sb.toString();
	}

	public static IProject importProject(IPath projectdir, IProgressMonitor monitor, NewLiferayPluginProjectOp op)
		throws CoreException {

		IProject project = null;

		ProjectRecord projectRecord = ProjectUtil.getProjectRecordForDir(projectdir.toPortableString());

		File projectDir = projectRecord.getProjectLocation().toFile();

		SDK sdk = SDKUtil.getSDKFromProjectDir(projectDir);

		if (sdk == null) {
			return null;
		}

		if (projectRecord.projectSystemFile != null) {
			try {
				project = ProjectUtil.createExistingProject(projectRecord, sdk.getLocation(), monitor);
			}
			catch (CoreException ce) {
				throw new CoreException(ProjectCore.createErrorStatus(ce));
			}
		}
		else if (projectRecord.liferayProjectDir != null) {
			try {
				project = ProjectUtil.createNewSDKProject(projectRecord, sdk.getLocation(), monitor, op);
			}
			catch (CoreException ce) {
				throw new CoreException(ProjectCore.createErrorStatus(ce));
			}
		}

		File parent = projectdir.removeLastSegments(1).toFile();

		String parentName = parent.getName();

		IProject sdkProject = CoreUtil.getProject(sdk.getName());

		IFolder folder = sdkProject.getFolder(parentName);

		ResourceFilterUtil.addResourceFilter(folder, project.getName(), monitor);

		return project;
	}

	public static IProject importProject(
			ProjectRecord projectRecord, IRuntime runtime, String sdkLocation, IProgressMonitor monitor)
		throws CoreException {

		return importProject(projectRecord, runtime, sdkLocation, null, monitor);
	}

	public static IProject importProject(
			ProjectRecord projectRecord, IRuntime runtime, String sdkLocation, NewLiferayPluginProjectOp op,
			IProgressMonitor monitor)
		throws CoreException {

		IProject project = null;

		if (projectRecord.projectSystemFile != null) {
			try {
				project = ProjectUtil.createExistingProject(projectRecord, runtime, sdkLocation, monitor);
			}
			catch (CoreException ce) {
				throw new CoreException(ProjectCore.createErrorStatus(ce));
			}
		}
		else if (projectRecord.liferayProjectDir != null) {
			try {
				project = ProjectUtil.createNewSDKProject(projectRecord, runtime, sdkLocation, op, monitor);
			}
			catch (CoreException ce) {
				throw new CoreException(ProjectCore.createErrorStatus(ce));
			}
		}

		return project;
	}

	/**
	 * This method is used to validate whether the given plugin binary is a valid
	 * Liferay Plugin Archieve
	 *
	 * @param binaryFile
	 *            - the binary file to be validated
	 * @return
	 */
	public static boolean isValidLiferayPlugin(File binaryFile) {
		boolean valid = false;

		JarFile pluginBinary = null;

		try {
			pluginBinary = new JarFile(binaryFile);

			BinaryProjectRecord tempRecord = new BinaryProjectRecord(binaryFile);

			// Check for liferay-plugin-package.properties or liferay-plugin-package.xml

			String packagePrpoertiesLocation = getConfigFileLocation(
				ILiferayConstants.LIFERAY_PLUGIN_PACKAGE_PROPERTIES_FILE);

			JarEntry lfrPluginPkgPropsEntry = pluginBinary.getJarEntry(packagePrpoertiesLocation);

			String packagePrpoertiesXMLLocation = getConfigFileLocation(
				ILiferayConstants.LIFERAY_PLUGIN_PACKAGE_PROPERTIES_XML_FILE);

			JarEntry lfrPluginPkgXmlEntry = pluginBinary.getJarEntry(packagePrpoertiesXMLLocation);

			if ((lfrPluginPkgPropsEntry != null) || (lfrPluginPkgXmlEntry != null)) {
				valid = true;
			}

			if (tempRecord.isHook()) {
				JarEntry jar = pluginBinary.getJarEntry(getConfigFileLocation(ILiferayConstants.LIFERAY_HOOK_XML_FILE));

				valid = valid && (jar != null);
			}
			else if (tempRecord.isLayoutTpl()) {
				JarEntry jar = pluginBinary.getJarEntry(
					getConfigFileLocation(ILiferayConstants.LIFERAY_LAYOUTTPL_XML_FILE));

				valid = valid || (jar != null);
			}
			else if (tempRecord.isPortlet()) {
				JarEntry jar = pluginBinary.getJarEntry(
					getConfigFileLocation(ILiferayConstants.LIFERAY_PORTLET_XML_FILE));

				valid = valid && (jar != null);
			}
			else if (tempRecord.isTheme()) {
				JarEntry jar = pluginBinary.getJarEntry(
					getConfigFileLocation(ILiferayConstants.LIFERAY_LOOK_AND_FEEL_XML_FILE));

				valid = valid || (jar != null);
			}

			if (!valid) {
				return valid;
			}
			else {

				// check if its a valid web Archieve

				JarEntry jar = pluginBinary.getJarEntry(getConfigFileLocation(ILiferayConstants.WEB_XML_FILE));

				valid = valid || jar != null;
			}
		}
		catch (IOException ioe) {
			valid = false;
		}
		finally {
			if (pluginBinary != null) {
				try {
					pluginBinary.close();
				}
				catch (IOException ioe) {
				}
			}
		}

		return valid;
	}

	public static IStatus validatePath(String currentPath) {
		if (!Path.EMPTY.isValidPath(currentPath)) {
			return ProjectCore.createErrorStatus("\"" + currentPath + "\" is not a valid path.");
		}

		IPath osPath = Path.fromOSString(currentPath);

		if (!osPath.toFile().isAbsolute()) {
			return ProjectCore.createErrorStatus("\"" + currentPath + "\" is not an absolute path.");
		}

		if (FileUtil.notExists(osPath)) {
			return ProjectCore.createErrorStatus("Directory doesn't exist.");
		}

		return Status.OK_STATUS;
	}

	public static IStatus validateSDKPath(String currentPath) {
		IStatus retVal = validatePath(currentPath);

		if (!retVal.isOK()) {
			return retVal;
		}

		IPath osPath = Path.fromOSString(currentPath);

		SDK sdk = SDKUtil.createSDKFromLocation(osPath);

		if (sdk == null) {
			return ProjectCore.createErrorStatus("SDK does not exist.");
		}

		try {
			IProject workspaceSdkProject = SDKUtil.getWorkspaceSDKProject();

			if ((workspaceSdkProject != null) && !workspaceSdkProject.getLocation().equals(sdk.getLocation())) {
				return ProjectCore.createErrorStatus("This project has different sdk than current workspace sdk");
			}
		}
		catch (CoreException ce) {
			return ProjectCore.createErrorStatus("Can't find sdk in workspace");
		}

		return sdk.validate(true);
	}

	public static IStatus validateSDKProjectPath(String currentPath) {
		IStatus retVal = validatePath(currentPath);

		if (!retVal.isOK()) {
			return retVal;
		}

		ProjectRecord record = ProjectUtil.getProjectRecordForDir(currentPath);

		if (record == null) {
			return ProjectCore.createErrorStatus("Invalid project location");
		}

		String projectName = record.getProjectName();

		IProject existingProject = CoreUtil.getWorkspaceRoot().getProject(projectName);

		if (FileUtil.exists(existingProject)) {
			return ProjectCore.createErrorStatus("Project name already exists.");
		}

		File projectDir = record.getProjectLocation().toFile();

		SDK sdk = SDKUtil.getSDKFromProjectDir(projectDir);

		if (sdk == null) {
			return ProjectCore.createErrorStatus("Could not determine SDK from project location " + currentPath);
		}

		try {
			IProject workspaceSdkProject = SDKUtil.getWorkspaceSDKProject();

			if ((workspaceSdkProject != null) && !workspaceSdkProject.getLocation().equals(sdk.getLocation())) {
				return ProjectCore.createErrorStatus("This project has different sdk than current workspace sdk");
			}
		}
		catch (CoreException ce) {
			return ProjectCore.createErrorStatus("Can't find sdk in workspace");
		}

		return sdk.validate(true);
	}

	private static class Msgs extends NLS {

		public static String checking;
		public static String creatingSDKWorkspaceProjects;

		static {
			initializeMessages(ProjectImportUtil.class.getName(), Msgs.class);
		}

	}

}