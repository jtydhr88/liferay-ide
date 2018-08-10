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

package com.liferay.ide.ui.swtbot.eclipse.page;

import com.liferay.ide.ui.swtbot.page.Dialog;
import com.liferay.ide.ui.swtbot.page.Text;

import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * @author Terry Jia
 */
public class TextDialog extends Dialog {

	public TextDialog(SWTBot bot) {
		super(bot);
	}

	public TextDialog(SWTBot bot, String cancelBtnLabel, String confirmBtnLabel) {
		super(bot, cancelBtnLabel, confirmBtnLabel);
	}

	public Text getText() {
		return new Text(getShell().bot());
	}

	public void setDialog(String viewName) {
		getText().setText(viewName);
	}

}