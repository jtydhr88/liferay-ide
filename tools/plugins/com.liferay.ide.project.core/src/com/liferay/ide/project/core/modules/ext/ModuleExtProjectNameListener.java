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

package com.liferay.ide.project.core.modules.ext;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.platform.PathBridge;

/**
 * @author Charles Wu
 */
public class ModuleExtProjectNameListener extends FilteredListener<PropertyContentEvent> {

	public static void updateLocation(NewModuleExtOp op) {
		String currentProjectName = SapphireUtil.getContent(op.getProjectName());

		if (CoreUtil.isNullOrEmpty(currentProjectName)) {
			return;
		}

		boolean useDefaultLocation = SapphireUtil.getContent(op.getUseDefaultLocation());

		if (useDefaultLocation) {
			Path newLocationBase = null;

			IProject liferayWorkspaceProject = LiferayWorkspaceUtil.getWorkspaceProject();

			String extFolder = LiferayWorkspaceUtil.getModuleExtDir(liferayWorkspaceProject);

			if ((extFolder != null) && (liferayWorkspaceProject != null)) {
				IPath path = liferayWorkspaceProject.getLocation();

				IPath appendPath = path.append(extFolder);

				newLocationBase = PathBridge.create(appendPath);
			}

			if (newLocationBase != null) {
				op.setLocation(newLocationBase);
			}
		}
	}

	@Override
	protected void handleTypedEvent(PropertyContentEvent event) {
		updateLocation(op(event));
	}

	protected NewModuleExtOp op(PropertyContentEvent event) {
		Property property = event.property();

		Element element = property.element();

		return element.nearest(NewModuleExtOp.class);
	}

}