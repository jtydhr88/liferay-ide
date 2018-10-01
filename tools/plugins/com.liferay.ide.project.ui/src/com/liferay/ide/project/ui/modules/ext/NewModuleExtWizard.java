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

package com.liferay.ide.project.ui.modules.ext;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.modules.ext.NewModuleExtOp;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.project.ui.modules.BaseProjectWizard;
import com.liferay.ide.ui.util.UIUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Charles Wu
 */
public class NewModuleExtWizard extends BaseProjectWizard<NewModuleExtOp> {

	public NewModuleExtWizard() {
		super(_createDefaultOp(), DefinitionLoader.sdef(NewModuleExtWizard.class).wizard());
	}

	@Override
	protected void performPostFinish() {
		super.performPostFinish();

		final NewModuleExtOp op = element().nearest(NewModuleExtOp.class);

		final IProject project = CoreUtil.getProject(SapphireUtil.getContent(op.getProjectName()));

		try {
			addToWorkingSets(project);
		}
		catch (Exception ex) {
			ProjectUI.logError("Unable to add project to working set", ex);
		}

		openLiferayPerspective(project);
	}

	private static NewModuleExtOp _createDefaultOp() {
		if (LiferayWorkspaceUtil.getGradleWorkspaceProject() == null) {
			Shell activeShell = UIUtil.getActiveShell();

			MessageDialog.openError(
				activeShell, "No available Gradle workspace project",
				"We recommend Liferay Gradle workspace to develop Module ext project!");

			throw new RuntimeException();
		}

		return NewModuleExtOp.TYPE.instantiate();
	}

}