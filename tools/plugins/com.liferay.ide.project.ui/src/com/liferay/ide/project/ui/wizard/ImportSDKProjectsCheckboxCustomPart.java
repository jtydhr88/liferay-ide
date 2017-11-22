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

package com.liferay.ide.project.ui.wizard;

import com.liferay.ide.project.core.ProjectRecord;
import com.liferay.ide.project.core.model.ProjectNamedItem;
import com.liferay.ide.project.core.model.SDKProjectsImportOp;
import com.liferay.ide.project.core.util.ProjectImportUtil;
import com.liferay.ide.project.core.util.ProjectUtil;
import com.liferay.ide.project.ui.ProjectUI;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.platform.PathBridge;

/**
 * @author Simon Jiang
 */
public class ImportSDKProjectsCheckboxCustomPart extends ProjectsCheckboxCustomPart {

	@Override
	public void dispose() {
		if (_listener != null) {
			Value<Object> sdkProperty = _op().property(SDKProjectsImportOp.PROP_SDK_LOCATION);

			sdkProperty.detach(_listener);
		}

		super.dispose();
	}

	@Override
	protected ElementList<ProjectNamedItem> getCheckboxList() {
		return _op().getSelectedProjects();
	}

	@Override
	protected List<ProjectCheckboxElement> getInitItemsList() {
		List<ProjectCheckboxElement> checkboxElementList = new ArrayList<>();
		Value<Path> sdkLocationPath = _op().getSdkLocation();

		Path sdkLocation = sdkLocationPath.content();

		if ((sdkLocation == null) || !sdkLocation.toFile().exists()) {
			return checkboxElementList;
		}

		final ProjectRecord[] projectRecords = _updateProjectsList(PathBridge.create(sdkLocation).toPortableString());

		if (projectRecords == null) {
			return checkboxElementList;
		}

		String context = null;

		for (ProjectRecord projectRecord : projectRecords) {
			final String projectLocation = projectRecord.getProjectLocation().toPortableString();

			context = projectRecord.getProjectName() + " (" + projectLocation + ")";

			ProjectCheckboxElement checkboxElement = new ProjectCheckboxElement(
				projectRecord.getProjectName(), context, projectRecord.getProjectLocation().toPortableString());

			if (!projectRecord.hasConflicts()) {
				checkboxElementList.add(checkboxElement);
			}
		}

		_sortProjectCheckboxElement(checkboxElementList);

		return checkboxElementList;
	}

	@Override
	protected IStyledLabelProvider getLableProvider() {
		return new SDKImportProjectsLabelProvider();
	}

	@Override
	protected ElementList<ProjectNamedItem> getSelectedElements() {
		return _op().getSelectedProjects();
	}

	@Override
	protected void init() {
		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(final PropertyContentEvent event) {
				PropertyDef eventDef = event.property().definition();

				if (eventDef.equals(SDKProjectsImportOp.PROP_SDK_LOCATION)) {
					Value<Path> sdkLocationPath = _op().getSdkLocation();

					Path sdkLocation = sdkLocationPath.content();

					if (sdkLocation != null) {
						IStatus status = ProjectImportUtil.validateSDKPath(sdkLocation.toPortableString());

						if (status.isOK()) {
							if (sdkLocation.toFile().exists()) {
								checkAndUpdateCheckboxElement();
							}
						}
						else {
							checkBoxViewer.remove(checkboxElements);
							updateValidation();
						}
					}
				}
			}

		};

		Value<Object> sdkLocation = _op().property(SDKProjectsImportOp.PROP_SDK_LOCATION);

		sdkLocation.attach(_listener);
	}

	@Override
	protected void updateValidation() {
		retval = Status.createOkStatus();

		Value<Path> sdkLocationPath = _op().getSdkLocation();

		Path sdkLocation = sdkLocationPath.content();

		if (sdkLocation != null) {
			IStatus status = ProjectImportUtil.validateSDKPath(sdkLocation.toPortableString());

			if (status.isOK()) {
				final int projectsCount = checkBoxViewer.getTable().getItemCount();
				final int selectedProjectsCount = checkBoxViewer.getCheckedElements().length;

				if (projectsCount == 0) {
					retval = Status.createErrorStatus("No available projects can be imported.");
				}

				if ((projectsCount > 0) && (selectedProjectsCount == 0)) {
					retval = Status.createErrorStatus("At least one project must be specified.");
				}
			}
		}
		else {
			retval = Status.createErrorStatus("SDK path cannot be empty");
		}

		refreshValidation();
	}

	protected long lastModified;
	protected Object[] selectedProjects = new ProjectRecord[0];
	protected IProject[] wsProjects;

	@SuppressWarnings({"rawtypes", "unchecked"})
	private Object[] _getProjectRecords() {
		List projectRecords = new ArrayList();

		for (int i = 0; i < selectedProjects.length; i++) {
			ProjectRecord projectRecord = (ProjectRecord)selectedProjects[i];

			if (_isProjectInWorkspace(projectRecord.getProjectName())) {
				projectRecord.setHasConflicts(true);
			}

			projectRecords.add(selectedProjects[i]);
		}

		return projectRecords.toArray(new ProjectRecord[projectRecords.size()]);
	}

	private IProject[] _getProjectsInWorkspace() {
		if (wsProjects == null) {
			wsProjects = ProjectUtil.getAllPluginsSDKProjects();
		}

		return wsProjects;
	}

	private boolean _isProjectInWorkspace(String projectName) {
		if (projectName == null) {
			return false;
		}

		IProject[] workspaceProjects = _getProjectsInWorkspace();

		for (int i = 0; i < workspaceProjects.length; i++) {
			if (projectName.equals(workspaceProjects[i].getName())) {
				return true;
			}
		}

		return false;
	}

	private SDKProjectsImportOp _op() {
		return getLocalModelElement().nearest(SDKProjectsImportOp.class);
	}

	private List<ProjectCheckboxElement> _sortProjectCheckboxElement(List<ProjectCheckboxElement> checkboxElementList) {
		Comparator<ProjectCheckboxElement> projectElementComparator = new Comparator<ProjectCheckboxElement>() {

			@Override
			public int compare(ProjectCheckboxElement o1, ProjectCheckboxElement o2) {
				return o1.name.compareTo(o2.name);
			}

		};

		checkboxElementList.sort(projectElementComparator);

		return checkboxElementList;
	}

	private ProjectRecord[] _updateProjectsList(final String path) {

		// on an empty path empty selectedProjects

		if ((path == null) || (path.length() == 0)) {
			selectedProjects = new ProjectRecord[0];

			return null;
		}

		final File directory = new File(path);

		long modified = directory.lastModified();

		lastModified = modified;

		final boolean dirSelected = true;

		try {
			selectedProjects = new ProjectRecord[0];

			Collection<File> eclipseProjectFiles = new ArrayList<>();

			Collection<File> liferayProjectDirs = new ArrayList<>();

			if (dirSelected && directory.isDirectory()) {
				if (!ProjectUtil.collectSDKProjectsFromDirectory(
						eclipseProjectFiles, liferayProjectDirs, directory, null, true, new NullProgressMonitor())) {

					return null;
				}

				selectedProjects = new ProjectRecord[eclipseProjectFiles.size() + liferayProjectDirs.size()];

				int index = 0;

				for (File eclipseProjectFile : eclipseProjectFiles) {
					selectedProjects[index++] = new ProjectRecord(eclipseProjectFile);
				}

				for (File liferayProjectDir : liferayProjectDirs) {
					selectedProjects[index++] = new ProjectRecord(liferayProjectDir);
				}
			}
		}
		catch (Exception e) {
			ProjectUI.logError(e);
		}

		Object[] projects = _getProjectRecords();

		return (ProjectRecord[])projects;
	}

	private FilteredListener<PropertyContentEvent> _listener;

}