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
import com.liferay.ide.ui.liferay.page.editor.LayoutTplEditor;
import com.liferay.ide.ui.liferay.page.editor.PomXmlEditor;
import com.liferay.ide.ui.liferay.page.editor.ServerEditor;
import com.liferay.ide.ui.liferay.page.editor.ServiceXmlEditor;
import com.liferay.ide.ui.liferay.page.editor.WorkflowXmlEditor;
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

	public KaleoWorkflowEditorAction kaleoWorkflow = new KaleoWorkflowEditorAction();
	public LayoutTplEditorAction layoutTpl = new LayoutTplEditorAction();
	public PomXmlEditorAction pomXml = new PomXmlEditorAction();
	public ServerEditorAction server = new ServerEditorAction();
	public ServiceXmlEditorAction serviceXml = new ServiceXmlEditorAction();

	public class KaleoWorkflowEditorAction {

		public void switchTabDiagram() {
			_workflowXmlEditor.getDiagramTab().click();

			ide.sleep(2000);
		}

		public void switchTabSource() {
			_workflowXmlEditor.getSourceTab().click();

			ide.sleep(2000);
		}

		private final WorkflowXmlEditor _workflowXmlEditor = new WorkflowXmlEditor(bot);

	}

	public class LayoutTplEditorAction {

		public void switchTabDesign() {
			_layoutTplEditor.getDesignTab().click();

			ide.sleep(2000);
		}

		public void switchTabPreview() {
			_layoutTplEditor.getPreviewTab().click();

			ide.sleep(2000);
		}

		public void switchTabSource() {
			_layoutTplEditor.getSourceTab().click();

			ide.sleep(2000);
		}

		private final LayoutTplEditor _layoutTplEditor = new LayoutTplEditor(bot);

	}

	public class PomXmlEditorAction {

		public void switchTabPomXml() {
			_pomXmlEditor.getPomXml().click();
		}

		private final PomXmlEditor _pomXmlEditor = new PomXmlEditor(bot);

	}

	public class ServerEditorAction {

		public void selectCustomLaunchSettings() {
			_serverEditor.getCustomLaunchSettings().click();
		}

		public void selectDefaultLaunchSettings() {
			_serverEditor.getDefaultLaunchSettings().click();
		}

		public void selectUseDeveloperMode() {
			_serverEditor.getUseDeveloperMode().select();
		}

		public void setHttpPort(String httpPort) {
			_serverEditor.getHttpPort().setText(httpPort);
		}

		private final ServerEditor _serverEditor = new ServerEditor(bot);

	}

	public class ServiceXmlEditorAction {

		public void switchTabDiagram() {
			_serviceXmlEditor.getDiagramTab();

			ide.sleep(2000);
		}

		public void switchTabOverview() {
			_serviceXmlEditor.getOverviewTab().click();

			ide.sleep(2000);
		}

		public void switchTabSource() {
			_serviceXmlEditor.getSourceTab().click();

			ide.sleep(2000);
		}

		private final ServiceXmlEditor _serviceXmlEditor = new ServiceXmlEditor(bot);

	}

	private final Editor _editor = new Editor(bot);

}