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

package com.liferay.ide.upgrade.tasks.core.internal.code;

import com.liferay.ide.upgrade.plan.core.BaseUpgradeTaskStep;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStep;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gregory Amerson
 */
@Component(
	property = {
		"id=configure_liferay_workspace_settings", "requirement=required", "order=3",
		"taskId=setup_development_environment", "title=Configure Liferay Workspace Settings",
		"description=Configuring target plarform in your Liferay Workspace helps you target a specific release of Liferay Portal, so dependencies get resolved properly."
	},
	scope = ServiceScope.PROTOTYPE, service = UpgradeTaskStep.class
)
public class ConfigureLiferayWorkspaceSettingsStep extends BaseUpgradeTaskStep {
}