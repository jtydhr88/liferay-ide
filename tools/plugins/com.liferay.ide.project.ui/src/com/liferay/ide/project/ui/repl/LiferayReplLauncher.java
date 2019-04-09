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

package com.liferay.ide.project.ui.repl;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import com.liferay.ide.project.ui.ProjectUI;

/**
 * @author Gregory Amerson
 */
@Component(scope = ServiceScope.SINGLETON, service = ReplLauncher.class)
public class LiferayReplLauncher implements ReplLauncher, IDebugEventSetListener {

	@Override
	public void cleanup(IDebugTarget debugTarget) {
	}

	@Override
	public IDebugTarget getDebugTarget(IFile file) {

		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public IBreakpoint getMagicBreakpoint(IDebugTarget debugTarget) throws DebugException {
		return null;
	}

	@Override
	public IVMInstall getVMInstall(IFile file) throws CoreException {

		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public String getWorkingDirectoryAttribute(IFile file) throws CoreException {

		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public ILaunch launch(IFile file) {
		_cleanupLaunchConifgurations();

		if (file.getFileExtension().equals("repl")) {
			_showNoReplDialog();

			return null;
		}

		IDebugTarget debugTarget = getDebugTarget(file);

		if (debugTarget != null) {
			return debugTarget.getLaunch();
		}

		IJavaProject javaProject = JavaCore.create(file.getProject());

		try {
			IRuntimeClasspathEntry[] runtimeClasspathEntries = Stream.of(
				JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject)
			).filter(
				entry -> entry.getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES
			).collect(
				Collectors.toList()
			).toArray(
				new IRuntimeClasspathEntry[0]
			);

			return _doLaunch(javaProject, file, runtimeClasspathEntries);
		}
		catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}

	private static final QualifiedName _REPL_LAUNCH_CONFIG_HANDLE_MEMENTO = new QualifiedName(ProjectUI.PLUGIN_ID, "repl_launch_config");


	private ILaunch _doLaunch(IJavaProject javaProject, IFile file, IRuntimeClasspathEntry[] runtimeClasspathEntries) throws CoreException {
		if (_debugTargetsToFiles.isEmpty()) {
			DebugPlugin debugPlugin = DebugPlugin.getDefault();

			debugPlugin.addDebugEventListener(this);
		}

		ILaunchConfiguration launchConfiguration = null;
		ILaunchConfigurationWorkingCopy launchConfigurationWorkingCopy = null;

		try {
			launchConfiguration = _getLaunchConfigurationTemplate(file);

			if (launchConfiguration != null) {
				launchConfigurationWorkingCopy = launchConfiguration.getWorkingCopy();
			}
		}
		catch (CoreException e) {
			launchConfiguration = null;

			ProjectUI.errorDialog("Liferay Repl", "Unable to retrieve repl settings", e);
		}

		if (launchConfiguration == null) {
			launchConfiguration = _createLaunchConfigurationTemplate(file);
			launchConfigurationWorkingCopy = launchConfiguration.getWorkingCopy();
		}

		IPath outputLocation = javaProject.getProject().getWorkingLocation(ProjectUI.PLUGIN_ID);

		return null;
	}

	private ILaunchConfiguration _createLaunchConfigurationTemplate(IFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	private ILaunchConfiguration _getLaunchConfigurationTemplate(IFile file) throws CoreException {
		String memento = _getLaunchConfigurationMemento(file);

		if (memento != null) {
			DebugPlugin debugPlugin = DebugPlugin.getDefault();

			ILaunchManager launchManager = debugPlugin.getLaunchManager();

			return launchManager.getLaunchConfiguration(memento);
		}

		return null;
	}

	private String _getLaunchConfigurationMemento(IFile file) {
		try {
			return file.getPersistentProperty(_REPL_LAUNCH_CONFIG_HANDLE_MEMENTO);
		}
		catch (CoreException e) {
			ProjectUI.logError(e);
		}

		return null;
	}

	private HashMap<IDebugTarget, IFile> _debugTargetsToFiles = new HashMap<>(10);

	private void _showNoReplDialog() {
		// TODO Auto-generated method stub

	}

	private void _cleanupLaunchConifgurations() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		// TODO Auto-generated method stub

	}

}