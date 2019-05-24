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

package com.liferay.ide.ui.liferay.support.upgrade;

import com.liferay.ide.ui.liferay.support.SupportBase;
import com.liferay.ide.ui.swtbot.UI;
import com.liferay.ide.ui.swtbot.page.Perspective;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

/**
 * @author Ashley Yuan
 */
public class LiferayUpgradePlanSupport extends SupportBase {

	public LiferayUpgradePlanSupport(SWTWorkbenchBot bot) {
		super(bot);
	}

	@Override
	public void before() {
		super.before();

		Perspective upgradePlannerPerspective = ide.getUpgradePlannerPerspective();

		upgradePlannerPerspective.activate();

		dialogAction.openPreferencesDialog();

		dialogAction.preferences.openLiferayUpgradePlannerTry();

		dialogAction.upgradePlanner.deleteOutline(0);

		dialogAction.upgradePlanner.addOutline();

		dialogAction.plannerOutline.addUrl(UI.UPGRADE_CODE_OUTLINE);

		dialogAction.confirm(UI.APPLY_AND_CLOSE);
	}

}