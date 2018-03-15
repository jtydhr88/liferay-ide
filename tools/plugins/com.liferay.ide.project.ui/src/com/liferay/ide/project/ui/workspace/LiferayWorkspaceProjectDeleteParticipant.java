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

package com.liferay.ide.project.ui.workspace;

import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.project.ui.ProjectUI;

import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

/**
 * @author Simon Jiang
 */

public class LiferayWorkspaceProjectDeleteParticipant extends DeleteParticipant {

	public LiferayWorkspaceProjectDeleteParticipant() {
	}

	@Override
	protected boolean initialize(Object element) {

		if(!(element instanceof IProject)) {
			return false;
		}

		_workspaceProject = (IProject) element;
		return true;
	}

	@Override
	public String getName() {
		return _MODS_FROM_WORKSPACE_PROJECT;
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		CompositeChange change = new CompositeChange(getName());

		if ((_workspaceProject == null) || FileUtil.notExists(_workspaceProject)) {
			return change;
		}

		IPath bundlesLocation = LiferayWorkspaceUtil.getHomeLocation(_workspaceProject);

		if ((bundlesLocation == null) || FileUtil.notExists(bundlesLocation)) {
			return change;
		}

		IServer[] servers = ServerCore.getServers();

		Stream<IServer> serverStream = Stream.of(servers);

		serverStream.filter(server -> server != null)
		.filter(server -> server.getRuntime().getLocation().equals(bundlesLocation))
		.forEach(server -> change.add(new RemoveLiferayWorkspaceBundleServerRuntimeChange(server)));

		return change;
	}	

	public class RemoveLiferayWorkspaceBundleServerRuntimeChange extends Change {
		private IServer _server;
		public RemoveLiferayWorkspaceBundleServerRuntimeChange(IServer server) {
			_server = server;
		}

		@Override
		public String getName() {
			return _MODS_FROM_WORKSPACE_PROJECT;
		}

		@Override
		public void initializeValidationData(IProgressMonitor pm) {
		}

		@Override
		public RefactoringStatus isValid(IProgressMonitor pm)
				throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}

		@Override
		public Change perform(IProgressMonitor pm) throws CoreException {
			try {
				IRuntime runtime = _server.getRuntime();
				_server.delete();

				if (runtime != null) {
					runtime.delete();
				}
			}
			catch(Exception e) {
				ProjectUI.logError("Failed to delete server " + _server.getName(), e);
			}
			return null;
		}

		@Override
		public Object getModifiedElement() {
			return _server;
		}
	}

	private IProject _workspaceProject;
	private static final String _MODS_FROM_WORKSPACE_PROJECT = "Liferay Workpsace Project Bundle's Runtime Cleanup";
}
