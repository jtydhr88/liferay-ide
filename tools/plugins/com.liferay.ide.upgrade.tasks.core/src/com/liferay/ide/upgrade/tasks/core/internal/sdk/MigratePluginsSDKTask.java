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

package com.liferay.ide.upgrade.tasks.core.internal.sdk;

import com.liferay.ide.upgrade.plan.core.BaseUpgradeTask;
import com.liferay.ide.upgrade.plan.core.UpgradeTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Terry Jia
 */
@Component(
	property = {"categoryId=code", "id=migrate_plugins_sdk", "order=4", "title=Migrate Plugins SDK",
		"description=This step will help you��You can initialize the current latest SDK project, or import an existing SDK project, or you can quickly move the original Plugins SDK project into Liferay Workspace"},
	scope = ServiceScope.PROTOTYPE, service = UpgradeTask.class
)
public class MigratePluginsSDKTask extends BaseUpgradeTask {
}