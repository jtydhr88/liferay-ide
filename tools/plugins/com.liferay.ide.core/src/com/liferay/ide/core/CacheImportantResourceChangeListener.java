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

package com.liferay.ide.core;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.FileUtil;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Terry Jia
 */
public class CacheImportantResourceChangeListener implements IResourceChangeListener {

	public CacheImportantResourceChangeListener(IProject project, String[] importantResources) {
		_project = project;
		_importantResources = importantResources;

		CoreUtil.addResourceChangeListener(this);
	}

	public boolean getStale() {
		return _stale;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta resourceDelta = event.getDelta();

		if (resourceDelta != null) {
			try {
				_visitResourceDelta(resourceDelta);
			}
			catch (CoreException ce) {
				_stale = true;

				CoreUtil.removeResourceChangeListener(this);
			}
		}
	}

	private Set<IPath> _collectAffectedResourcePaths(IResourceDelta[] resourceDeltas) {
		Set<IPath> result = new HashSet<>();

		_collectAffectedResourcePaths(result, resourceDeltas);

		return result;
	}

	private void _collectAffectedResourcePaths(Set<IPath> paths, IResourceDelta[] resourceDeltas) {
		for (IResourceDelta resourceDelta : resourceDeltas) {
			IResource resource = resourceDelta.getResource();

			paths.add(resource.getProjectRelativePath());

			_collectAffectedResourcePaths(paths, resourceDelta.getAffectedChildren());
		}
	}

	private boolean _doVisitDelta(IResourceDelta resourceDelta) {
		IResource resource = resourceDelta.getResource();

		if (resource instanceof IProject) {
			IProject project = (IProject)resource;

			if (project.equals(_project)) {
				if (_importantResourcesAffected(resourceDelta)) {
					_stale = true;

					CoreUtil.removeResourceChangeListener(this);
				}
			}

			return false;
		}
		else {
			return resource instanceof IWorkspaceRoot;
		}
	}

	private boolean _importantResourcesAffected(IResourceDelta resourceDelta) {
		Set<IPath> affectedResourcePaths = _collectAffectedResourcePaths(resourceDelta.getAffectedChildren());

		for (String fileName : _importantResources) {
			IPath path = FileUtil.getProjectFileRelativePath(_project, fileName);

			if (affectedResourcePaths.contains(path)) {
				return true;
			}
		}

		return false;
	}

	private void _visitResourceDelta(IResourceDelta resourceDelta) throws CoreException {
		resourceDelta.accept(
			new IResourceDeltaVisitor() {

				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					try {
						return _doVisitDelta(delta);
					}
					catch (Exception e) {
						_stale = true;

						CoreUtil.removeResourceChangeListener(CacheImportantResourceChangeListener.this);

						throw new CoreException(new Status(IStatus.WARNING, LiferayCore.PLUGIN_ID, e.getMessage(), e));
					}
				}

			});
	}

	private String[] _importantResources;
	private IProject _project;
	private volatile boolean _stale = false;

}