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

import com.liferay.ide.core.IBundleProject;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.maven.core.util.DefaultMaven2OsgiConverter;
import com.liferay.ide.project.core.IProjectBuilder;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.server.remote.IRemoteServerPublisher;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings("restriction")
public class MavenBundlePluginProject extends LiferayMavenProject implements IBundleProject {

	public MavenBundlePluginProject(IProject project) {
		super(project);
	}

	@Override
	public <T> T adapt(Class<T> adapterType) {
		T adapter = super.adapt(adapterType);

		if (adapter != null) {
			return adapter;
		}

		IMavenProjectFacade facade = MavenUtil.getProjectFacade(getProject(), new NullProgressMonitor());

		if (facade != null) {
			if (IProjectBuilder.class.equals(adapterType)) {
				IProjectBuilder projectBuilder = new MavenProjectBuilder(getProject());

				return adapterType.cast(projectBuilder);
			}
			else if (IRemoteServerPublisher.class.equals(adapterType)) {
				IRemoteServerPublisher remoteServerPublisher = new MavenProjectRemoteServerPublisher(getProject());

				return adapterType.cast(remoteServerPublisher);
			}
			else if (IBundleProject.class.equals(adapterType)) {
				return adapterType.cast(this);
			}
		}

		return null;
	}

	@Override
	public boolean filterResource(IPath resourcePath) {
		if (filterResource(resourcePath, _ignorePaths)) {
			return true;
		}

		return false;
	}

	@Override
	public String getBundleShape() {
		return "jar";
	}

	@Override
	public IPath getOutputBundle(boolean cleanBuild, IProgressMonitor monitor) throws CoreException {
		IPath outputJar = null;

		IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(getProject(), monitor);
		MavenProjectBuilder mavenProjectBuilder = new MavenProjectBuilder(getProject());

		// IDE-3009 delete the MANIFEST.MF to ensure that it will be regenerated by
		// bnd-process

		IFile manifest = getProject().getFile("target/classes/META-INF/MANIFEST.MF");

		if (FileUtil.exists(manifest)) {
			manifest.delete(true, monitor);
		}

		if (cleanBuild || !_isAutoBuild()) {
			getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
			getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		}
		else {
			getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		}

		IMarker[] buildProblems = JavaBuilder.getProblemsFor(getProject());

		Stream<IMarker> buildProblemsStream = Stream.of(buildProblems);

		List<IMarker> errorMarkerList = buildProblemsStream.filter(marker -> {
			try {
				int severirty = (int)marker.getAttribute(IMarker.SEVERITY);

				return (IMarker.SEVERITY_ERROR == severirty);
			}
			catch (CoreException e) {
				return false;
			}
		}).collect(Collectors.toList());

		if (ListUtil.isNotEmpty(errorMarkerList)) {
			return outputJar;
		}

		mavenProjectBuilder.execJarMojo(projectFacade, monitor);

		MavenProject mavenProject = projectFacade.getMavenProject(monitor);

		String targetName = mavenProject.getBuild().getFinalName() + "." + getBundleShape();

		String buildDirectory = mavenProject.getBuild().getDirectory();
		File baseDirectory = mavenProject.getBasedir();

		IPath buildDirPath = new Path(buildDirectory);
		IPath baseDirPath = new Path(baseDirectory.toString());

		IPath relativePath = buildDirPath.makeRelativeTo(baseDirPath);

		IFolder targetFolder = _getTargetFolder(getProject(), relativePath);

		if (FileUtil.exists(targetFolder)) {

			// targetFolder.refreshLocal( IResource.DEPTH_ONE, monitor );

			IPath targetFile = targetFolder.getRawLocation().append(targetName);

			if (FileUtil.exists(targetFile)) {
				outputJar = targetFile;
			}
		}

		return outputJar;
	}

	@Override
	public IPath getOutputBundlePath() {
		IPath outputJar = null;

		try {
			IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(getProject(), null);

			MavenProject mavenProject = projectFacade.getMavenProject(null);

			String targetName = mavenProject.getBuild().getFinalName() + "." + getBundleShape();

			IFolder targetFolder = getProject().getFolder("target");

			if (FileUtil.exists(targetFolder)) {
				IPath targetFile = targetFolder.getRawLocation().append(targetName);

				if (FileUtil.exists(targetFile)) {
					outputJar = targetFile;
				}
			}
		}
		catch (Exception e) {
			LiferayMavenCore.logError(e);
		}

		return outputJar;
	}

	@Override
	public String getSymbolicName() throws CoreException {
		String bsn = ProjectUtil.getBundleSymbolicNameFromBND(getProject());

		if (!CoreUtil.empty(bsn)) {
			return bsn;
		}

		String retval = null;

		IProgressMonitor monitor = new NullProgressMonitor();

		IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade(getProject(), monitor);

		MavenProject mavenProject = projectFacade.getMavenProject(monitor);

		Artifact artifact = mavenProject.getArtifact();

		File file = artifact.getFile();

		if (FileUtil.exists(file) && !file.getName().equals("classes")) {
			retval = new DefaultMaven2OsgiConverter().getBundleSymbolicName(artifact);
		}
		else {
			IProject project = getProject();

			// fallback to project name

			retval = project.getLocation().lastSegment();
		}

		return retval;
	}

	@Override
	public boolean isFragmentBundle() {
		IFile bndFile = getProject().getFile("bnd.bnd");

		if (FileUtil.exists(bndFile)) {
			try(InputStream inputStream = bndFile.getContents()) {
				String content = FileUtil.readContents(inputStream);

				if (content.contains("Fragment-Host")) {
					return true;
				}
			}
			catch (Exception e) {
			}
		}

		return false;
	}

	private IFolder _getTargetFolder(IProject project, IPath relativePath) {
		IFolder targetFolder = project.getFolder(relativePath);

		if (FileUtil.notExists(targetFolder)) {
			targetFolder = project.getFolder("target");
		}

		return targetFolder;
	}

	private boolean _isAutoBuild() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		return workspace.getDescription().isAutoBuilding();
	}

	private String[] _ignorePaths = {"target"};

}