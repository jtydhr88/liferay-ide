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

package com.liferay.ide.upgrade.plugins.ui.internal.servicewrapper;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.project.core.model.ProjectName;
import com.liferay.ide.project.ui.modules.BaseProjectWizard;
import com.liferay.ide.upgrade.plugins.core.NewBasicLiferayModuleProjectOp;
import com.liferay.ide.upgrade.plugins.ui.internal.UpgradePluginsUIPlugin;

import java.io.File;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.platform.PathBridge;
import org.eclipse.sapphire.ui.def.DefinitionLoader;

/**
 * @author Seiphon Wang
 */
public class NewBasicLiferayModuleProjectWizard extends BaseProjectWizard<NewBasicLiferayModuleProjectOp> {

	public NewBasicLiferayModuleProjectWizard(
		NewBasicLiferayModuleProjectOp newLiferayModuleProjectOp, Path targetProjectLocation) {

		super(
			_createDefaultOp(newLiferayModuleProjectOp, targetProjectLocation),
			DefinitionLoader.sdef(NewBasicLiferayModuleProjectWizard.class).wizard());
	}

	@Override
	protected void performPostFinish() {
		super.performPostFinish();

		final List<IProject> projects = new ArrayList<>();

		final NewBasicLiferayModuleProjectOp op = element().nearest(NewBasicLiferayModuleProjectOp.class);

		ElementList<ProjectName> projectNames = op.getProjectNames();

		for (ProjectName projectName : projectNames) {
			final IProject newProject = CoreUtil.getProject(get(projectName.getName()));

			if (newProject != null) {
				projects.add(newProject);
			}
		}

		for (final IProject project : projects) {
			try {
				addToWorkingSets(project);
			}
			catch (Exception ex) {
				UpgradePluginsUIPlugin.logError("Unable to add project to working set", ex);
			}
		}

		if (ListUtil.isNotEmpty(projects)) {
			IProject finalProject = projects.get(0);

			openLiferayPerspective(finalProject);
		}
	}

	private static NewBasicLiferayModuleProjectOp _createDefaultOp(
		NewBasicLiferayModuleProjectOp newLiferayModuleProjectOp, Path path) {

		File file = path.toFile();

		IPath newPath = new org.eclipse.core.runtime.Path(file.getPath());

		newLiferayModuleProjectOp.setLocation(PathBridge.create(newPath));

		return newLiferayModuleProjectOp;
	}

}