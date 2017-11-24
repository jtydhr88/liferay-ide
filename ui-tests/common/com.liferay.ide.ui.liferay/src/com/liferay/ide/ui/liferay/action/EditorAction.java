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

package com.liferay.ide.ui.liferay.action;

import com.liferay.ide.ui.liferay.UIAction;
import com.liferay.ide.ui.liferay.page.editor.DesignEditor;
import com.liferay.ide.ui.liferay.page.editor.PomXmlEditor;
import com.liferay.ide.ui.liferay.page.editor.PreviewEditor;
import com.liferay.ide.ui.liferay.page.editor.SourceEditor;
import com.liferay.ide.ui.swtbot.page.Editor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;

/**
 * @author Terry Jia
 */
public class EditorAction extends UIAction {

	public EditorAction(SWTWorkbenchBot bot) {
		super(bot);
	}

	public void close() {
		_editor.close();
	}

	public String getContent() {
		return _editor.getText();
	}

	public void save() {
		_editor.save();
	}

	public void setText(String text) {
		_editor.setText(text);
	}

	public void switchTabDesign() {
		_designEditor.getDesign().click();
	}
	
	public void switchTabPomXml() {
		_pomXmlEditor.getPomXml().click();
	}

	public void switchTabPreview() {
		_previewEditor.getPreview().click();
	}

	public void switchTabSource() {
		_sourceEditor.getSource().click();
	}

	private final DesignEditor _designEditor = new DesignEditor(bot);
	private final Editor _editor = new Editor(bot);
	private final PomXmlEditor _pomXmlEditor = new PomXmlEditor(bot);
	private final PreviewEditor _previewEditor = new PreviewEditor(bot);
	private final SourceEditor _sourceEditor = new SourceEditor(bot);

}
