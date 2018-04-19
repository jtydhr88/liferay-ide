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

package com.liferay.ide.server.core.portal;

import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.server.core.LiferayServerCore;

import java.io.File;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author Gregory Amerson
 */
public class PortalServerLaunchConfigDelegate extends AbstractJavaLaunchConfigurationDelegate {

	public static final String ID = "com.liferay.ide.server.portal.launch";

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
		throws CoreException {

		IServer server = ServerUtil.getServer(config);

		if (server != null) {

			/*
			 * if( server.shouldPublish() && ServerCore.isAutoPublishing() )
			 * {
			 * server.publish( IServer.PUBLISH_INCREMENTAL, monitor );
			 * }
			 */
			_launchServer(server, config, mode, launch, monitor);
		}
	}

	private void _launchServer(
			IServer server, ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
		throws CoreException {

		IVMInstall vm = verifyVMInstall(config);

		IVMRunner runner;

		if (vm.getVMRunner(mode) != null) {
			runner = vm.getVMRunner(mode);
		}
		else {
			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
		}

		File workingDir = verifyWorkingDirectory(config);

		String workingDirPath = workingDir != null ? workingDir.getAbsolutePath() : null;

		String progArgs = getProgramArguments(config);
		String vmArgs = getVMArguments(config);
		String[] envp = getEnvironment(config);

		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, progArgs);

		Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(config);

		PortalServerBehavior portalServer = (PortalServerBehavior)server.loadAdapter(
			PortalServerBehavior.class, monitor);

		String classToLaunch = portalServer.getClassToLaunch();

		String[] classpath = getClasspath(config);

		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(classToLaunch, classpath);

		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirPath);
		runConfig.setEnvironment(envp);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		String[] bootpath = getBootpath(config);

		if (ListUtil.isNotEmpty(bootpath)) {
			runConfig.setBootClassPath(bootpath);
		}

		portalServer.launchServer(launch, mode, monitor);

		server.addServerListener(
			new IServerListener() {

				@Override
				public void serverChanged(ServerEvent event) {
					if ((event.getKind() & ServerEvent.MODULE_CHANGE) > 0) {
						AbstractSourceLookupDirector sourceLocator =
							(AbstractSourceLookupDirector)launch.getSourceLocator();

						try {
							String memento = config.getAttribute(
								ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);

							if (memento != null) {
								sourceLocator.initializeFromMemento(memento);
							}
							else {
								sourceLocator.initializeDefaults(config);
							}
						}
						catch (CoreException ce) {
							LiferayServerCore.logError("Could not reinitialize source lookup director", ce);
						}
					}
					else if (((event.getKind() & ServerEvent.SERVER_CHANGE) > 0) &&
							 (event.getState() == IServer.STATE_STOPPED)) {

						server.removeServerListener(this);
					}
				}

			});

		try {
			runner.run(runConfig, launch, monitor);
			portalServer.addProcessListener(launch.getProcesses()[0]);
		}
		catch (Exception e) {
			portalServer.cleanup();
		}
	}

}