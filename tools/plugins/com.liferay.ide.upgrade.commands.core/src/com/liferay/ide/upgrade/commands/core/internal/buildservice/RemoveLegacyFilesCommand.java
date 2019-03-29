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

package com.liferay.ide.upgrade.commands.core.internal.buildservice;

import com.liferay.ide.upgrade.commands.core.ResourceSelection;
import com.liferay.ide.upgrade.commands.core.buildservice.RemoveLegacyFilesCommandKeys;
import com.liferay.ide.upgrade.commands.core.internal.UpgradeCommandsCorePlugin;
import com.liferay.ide.upgrade.plan.core.UpgradeCommand;
import com.liferay.ide.upgrade.plan.core.UpgradeCommandPerformedEvent;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Simon Jiang
 * @author Terry Jia
 */
@Component(
	property = "id=" + RemoveLegacyFilesCommandKeys.ID, scope = ServiceScope.PROTOTYPE, service = UpgradeCommand.class
)
public class RemoveLegacyFilesCommand implements UpgradeCommand {

	@Override
	public IStatus perform(IProgressMonitor progressMonitor) {
		List<IProject> projects = _resourceSelection.selectProjects(
			"Select Liferay Service Builder Projects", false, ResourceSelection.SERVICE_BUILDER_PROJECTS);

		if (projects.isEmpty()) {
			return Status.CANCEL_STATUS;
		}

		for (IProject project : projects) {
			try {
				String relativePath = "/docroot/WEB-INF/src/META-INF";

				IFile portletSpringXML = project.getFile(relativePath + "/portlet-spring.xml");

				if (portletSpringXML.exists()) {
					portletSpringXML.delete(true, progressMonitor);
				}

				IFile shardDataSourceSpringXML = project.getFile(relativePath + "/shard-data-source-spring.xml");

				if (shardDataSourceSpringXML.exists()) {
					shardDataSourceSpringXML.delete(true, progressMonitor);
				}

				// for 6.2 maven project

				IFolder metaInfFolder = project.getFolder("/src/main/resources/META-INF/");

				if (metaInfFolder.exists()) {
					metaInfFolder.delete(true, progressMonitor);
				}
			}
			catch (CoreException ce) {
				IStatus error = UpgradeCommandsCorePlugin.createErrorStatus("Unable to delete legacy files.", ce);

				UpgradeCommandsCorePlugin.log(error);

				return error;
			}
		}

		_upgradePlanner.dispatch(new UpgradeCommandPerformedEvent(this, projects));

		return Status.OK_STATUS;
	}

	@Reference
	private ResourceSelection _resourceSelection;

	@Reference
	private UpgradePlanner _upgradePlanner;

}