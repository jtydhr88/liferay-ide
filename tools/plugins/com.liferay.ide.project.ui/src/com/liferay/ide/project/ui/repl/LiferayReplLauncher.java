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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.ILineNumberAttribute;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;
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
		return _filesToDebugTargets.get(file);
	}

	@Override
	public IBreakpoint getMagicBreakpoint(IDebugTarget debugTarget) throws DebugException {
		return _debugTargetsToBreakpoints.get(debugTarget);
	}

	@Override
	public IVMInstall getVMInstall(IFile file) throws CoreException {
		return null;
	}

	@Override
	public String getWorkingDirectoryAttribute(IFile file) throws CoreException {
		return null;
	}

	@Override
	public ILaunch launch(IFile file) {
		_cleanupLaunchConifgurations();

		if (!file.getFileExtension().equals("repl")) {
			_showNoReplDialog();

			return null;
		}

		IDebugTarget debugTarget = getDebugTarget(file);

		if (debugTarget != null) {
			return debugTarget.getLaunch();
		}

		IJavaProject javaProject = JavaCore.create(file.getProject());

		try {
			return _doLaunch(javaProject, file, null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final QualifiedName _REPL_LAUNCH_CONFIG_HANDLE_MEMENTO = new QualifiedName(ProjectUI.PLUGIN_ID, "repl_launch_config");


	private ILaunch _doLaunch(IJavaProject javaProject, IFile file, IRuntimeClasspathEntry[] runtimeClasspathEntries) throws CoreException, IOException {
		if (_debugTargetsToFiles.isEmpty()) {
			DebugPlugin debugPlugin = DebugPlugin.getDefault();

			debugPlugin.addDebugEventListener(this);
		}

		ILaunchConfiguration launchConfiguration = null;
		ILaunchConfigurationWorkingCopy launchConfigurationWorkingCopy = null;

		try {
			launchConfiguration = _getLaunchConfiguration(file);

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

		launchConfigurationWorkingCopy.setAttribute(_REPL_LAUNCH, _REPL_LAUNCH);

		launchConfiguration = launchConfigurationWorkingCopy.doSave();

		ILaunch launch = launchConfiguration.launch(ILaunchManager.DEBUG_MODE, null);

		if (launch != null) {
			IDebugTarget debugTarget = launch.getDebugTarget();
			IBreakpoint breakpoint = _createMagicBreakpoint();
			_debugTargetsToFiles.put(debugTarget, file);
			_filesToDebugTargets.put(file, debugTarget);
			_debugTargetsToBreakpoints.put(debugTarget, breakpoint);
			debugTarget.breakpointAdded(breakpoint);
			launch.setAttribute(_REPL_LAUNCH, _REPL_LAUNCH);
		}

		return launch;
	}

	private static final String _REPL_LAUNCH = ProjectUI.PLUGIN_ID + ".repl_launch";

	private IBreakpoint _createMagicBreakpoint() throws CoreException, IOException {
		String typeName = "com.liferay.repl.session.ReplSession";

		String entryName = "com/liferay/repl/session/ReplSession.class";

		Bundle bundle = ProjectUI.getPluginBundle();

		URL url = FileLocator.toFileURL(bundle.getEntry("/resources/repl-session/build/libs/com.liferay.repl.session-1.0.0.jar"));

		IClassFileReader classFileReader =
			ToolFactory.createDefaultClassFileReader(
				url.getFile(), entryName, IClassFileReader.METHOD_INFOS | IClassFileReader.METHOD_BODIES);

		String noopMethodName = "noop";

		Optional<IBreakpoint> magicBreakpoint = Stream.of(
			classFileReader.getMethodInfos()
		).filter(
			methodInfo -> noopMethodName.equals(new String(methodInfo.getName()))
		).findFirst(
		).map(
			methodInfo -> {
				ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
				ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
				int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
				int lineNumber = lineNumberTable[0][1];

				try {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();

					IBreakpoint breakpoint = JDIDebugModel.createLineBreakpoint(workspace.getRoot(), typeName, lineNumber, -1, -1, 0, false, null);
					breakpoint.setPersisted(false);

					return breakpoint;
				}
				catch (CoreException e) {
					ProjectUI.logError("Unable to create breakpoint", e);
				}

				return null;
			}
		);

		if (!magicBreakpoint.isPresent()) {
			throw new CoreException(ProjectUI.createErrorStatus("Unable to find method to create breakpoint."));
		}

		return magicBreakpoint.get();
	}

	private ILaunchConfiguration _createLaunchConfigurationTemplate(IFile file) throws CoreException {
		IProject project = file.getProject();

		ILaunchManager launchManager = _getLaunchManager();

		ILaunchConfigurationType remoteJavaLaunchConfigurationType =
			launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);

		ILaunchConfigurationWorkingCopy launchConfigurationWorkingCopy =
			remoteJavaLaunchConfigurationType.newInstance(null, "Repl launcher");

		launchConfigurationWorkingCopy.setContainer(file.getProject());
		launchConfigurationWorkingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
		launchConfigurationWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
		launchConfigurationWorkingCopy.setAttribute(
			IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
			IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);

		Map<String, String> connectMap = new HashMap<String, String>();
	    connectMap.put("port", "8000");
	    connectMap.put("hostname", "localhost");

	    launchConfigurationWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, connectMap);
		launchConfigurationWorkingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
		launchConfigurationWorkingCopy.setAttribute(
			"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS",
			new ArrayList<String>(Arrays.asList(new String[] {"/" + project.getName()})));
		launchConfigurationWorkingCopy.setAttribute(
				"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES",
				new ArrayList<String>(Arrays.asList(new String[] {"4"})));

		ILaunchConfiguration launchConfiguration = launchConfigurationWorkingCopy.doSave();

		_setLaunchConfigMemento(file, launchConfiguration.getMemento());

		return launchConfiguration;
	}

	private void _setLaunchConfigMemento(IFile file, String memento) {
		try {
			file.setPersistentProperty(_REPL_LAUNCH_CONFIG_HANDLE_MEMENTO, memento);
		}
		catch (CoreException e) {
			ProjectUI.logError(e);
		}
	}

	private ILaunchConfiguration _getLaunchConfiguration(IFile file) throws CoreException {
		String memento = _getLaunchConfigurationMemento(file);

		if (memento != null) {
			ILaunchManager launchManager = _getLaunchManager();

			return launchManager.getLaunchConfiguration(memento);
		}

		return null;
	}

	private ILaunchManager _getLaunchManager() {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();

		return debugPlugin.getLaunchManager();
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

	private Map<IFile, IDebugTarget> _filesToDebugTargets = new HashMap<>(10);
	private Map<IDebugTarget, IFile> _debugTargetsToFiles = new HashMap<>(10);
	private Map<IDebugTarget, IBreakpoint> _debugTargetsToBreakpoints = new HashMap<>(10);

	private void _showNoReplDialog() {
		// TODO Auto-generated method stub

	}

	private void _cleanupLaunchConifgurations() {
		// TODO Auto-generated method stub

	}

	private void _cleanup(IDebugTarget debugTarget) {
		IFile file = _debugTargetsToFiles.get(debugTarget);

		if (file != null) {
			_debugTargetsToFiles.remove(debugTarget);
			_filesToDebugTargets.remove(file);
			_debugTargetsToBreakpoints.remove(debugTarget);

			ILaunch launch = debugTarget.getLaunch();

			ILaunchManager launchManager = _getLaunchManager();

			launchManager.removeLaunch(launch);

			if (_debugTargetsToFiles.isEmpty()) {
				DebugPlugin debugPlugin = DebugPlugin.getDefault();

				debugPlugin.removeDebugEventListener(this);
			}
		}
	}

	@Override
	public void handleDebugEvents(DebugEvent[] debugEvents) {
		Stream.of(
			debugEvents
		).filter(
			event -> (event.getSource() instanceof IDebugTarget) && (event.getKind() == DebugEvent.TERMINATE)
		).map(
			event -> (IDebugTarget)event.getSource()
		).forEach(
			this::_cleanup
		);
	}

}