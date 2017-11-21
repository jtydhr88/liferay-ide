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

package com.liferay.ide.ui.liferay.page.editor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

import com.liferay.ide.ui.swtbot.page.CTabItem;
import com.liferay.ide.ui.swtbot.page.Editor;

/**
 * @author Joye Luo
 */
public class ServiceXmlEditor extends Editor {

	public ServiceXmlEditor(SWTWorkbenchBot bot) {
		super(bot);

		_source = new CTabItem(bot, SOURCE);
		_overview = new CTabItem(bot, OVERVIEW );
		_diagram = new CTabItem(bot, Diagram);
	}

	public ServiceXmlEditor(SWTWorkbenchBot bot, String editorName) {
		super(bot, editorName);

		_source = new CTabItem(bot, SOURCE);
		_overview = new CTabItem(bot, OVERVIEW );
		_diagram = new CTabItem(bot, Diagram);
	}

	public CTabItem getSourceTab() {
		return _source;
	}

	public CTabItem getOverviewTab() {
		return _overview;
	}

	public CTabItem getDiagramTab() {
		return _diagram;
	}

	private CTabItem _source;
	private CTabItem _overview;
	private CTabItem _diagram;
}
