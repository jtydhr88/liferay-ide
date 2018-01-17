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

package com.liferay.ide.ui.liferay.page.wizard;

import com.liferay.ide.ui.swtbot.page.ComboBox;
import com.liferay.ide.ui.swtbot.page.Radio;
import com.liferay.ide.ui.swtbot.page.Text;
import com.liferay.ide.ui.swtbot.page.ToolbarButtonWithTooltip;
import com.liferay.ide.ui.swtbot.page.Wizard;

import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * @author Haoyi Sun
 */
public class NewKaleoWorkflowWizard extends Wizard {

	public NewKaleoWorkflowWizard(SWTBot bot) {
		super(bot, 1);
	}

	public ToolbarButtonWithTooltip getAddResourceActionBtn() {
		return new ToolbarButtonWithTooltip(getShell().bot(), ADD_RESOURCE_ACTION);
	}

	public ToolbarButtonWithTooltip getAddRoleBtn() {
		return new ToolbarButtonWithTooltip(getShell().bot(), ADD_ROLE);
	}

	public Radio getAssignCreator() {
		return new Radio(getShell().bot(), 0);
	}

	public Radio getAssignResourceActions() {
		return new Radio(getShell().bot(), 5);
	}

	public Radio getAssignRoleById() {
		return new Radio(getShell().bot(), 2);
	}

	public Radio getAssignRoleType() {
		return new Radio(getShell().bot(), 1);
	}

	public Radio getAssignScriptedAssignment() {
		return new Radio(getShell().bot(), 3);
	}

	public Radio getAssignUser() {
		return new Radio(getShell().bot(), 4);
	}

	public ToolbarButtonWithTooltip getDeleteRoleBtn() {
		return new ToolbarButtonWithTooltip(getShell().bot(), DELETE);
	}

	public Text getEmailAddress() {
		return new Text(bot, EMAIL_ADDRESS);
	}

	public Text getResourceAction() {
		return new Text(bot, 0);
	}

	public Text getRoleId() {
		return new Text(bot, ROLE_ID);
	}

	public Text getScreenName() {
		return new Text(bot, SCREEN_NAME);
	}

	public ComboBox getScriptLanguage() {
		return new ComboBox(bot, SCRIPT_LANGUAGE);
	}

	public Text getUserId() {
		return new Text(bot, USER_ID);
	}

}