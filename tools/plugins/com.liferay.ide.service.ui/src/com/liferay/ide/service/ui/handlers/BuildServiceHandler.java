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

package com.liferay.ide.service.ui.handlers;

import com.liferay.ide.core.ILiferayConstants;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.service.core.ServiceCore;
import com.liferay.ide.service.core.job.BuildServiceJob;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Simon Jiang
 * @author Kuo Zhang
 */
@SuppressWarnings("restriction")
public class BuildServiceHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStatus retval = null;
		IProject project = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;

			Object selected = structuredSelection.getFirstElement();

			if (selected instanceof IResource) {
				project = ((IResource)selected).getProject();
			}
			else if (selected instanceof IJavaElement) {
				project = ((IJavaElement)selected).getJavaProject().getProject();
			}
			else if (selected instanceof PackageFragmentRootContainer) {
				project = ((PackageFragmentRootContainer)selected).getJavaProject().getProject();
			}
		}

		if (project == null) {
			IEditorInput editorInput = HandlerUtil.getActiveEditorInput(event);

			if ((editorInput != null) && (editorInput.getAdapter(IResource.class) != null)) {
				project = ((IResource)editorInput.getAdapter(IResource.class)).getProject();
			}
		}

		if (project != null) {
			retval = executeServiceBuild(project);
		}

		return retval;
	}

	protected IStatus executeServiceBuild(IProject project) {
		IStatus retval = null;

		try {
			new BuildServiceJob(project).schedule();
			retval = Status.OK_STATUS;
		}
		catch (Exception e) {
			retval = ServiceCore.createErrorStatus("Unable to execute build-service command", e);
		}

		return retval;
	}

	protected IFile getServiceFile(IProject project) {
		IFolder docroot = CoreUtil.getDefaultDocrootFolder(project);

		if ((docroot != null) && docroot.exists()) {
			IPath path = new Path("WEB-INF/" + ILiferayConstants.SERVICE_XML_FILE);

			IFile serviceFile = docroot.getFile(path);

			if (serviceFile.exists()) {
				return serviceFile;
			}
		}

		return null;
	}

}