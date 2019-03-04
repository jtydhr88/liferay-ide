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

import com.liferay.ide.upgrade.plan.core.BaseUpgradeTask;
import com.liferay.ide.upgrade.plan.core.UpgradeTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gregory Amerson
 */
@Component(
	property = {
		"categoryId=code", "imagePath=icons/setup.png", "id=setup_development_environment", "order=2",
		"title=Setup Development Environment",
		"description=This step will set the basic parameters of Liferay Workspace. According to different upgrade " +
			"projects, workspace can be divided into two ways: import and create. The configuration of Workspace " +
				"Plugin Version, Target Platform and Server is also supported."
	},
	scope = ServiceScope.PROTOTYPE, service = UpgradeTask.class
)
public class SetupDevelopmentEnvironmentTask extends BaseUpgradeTask {
}