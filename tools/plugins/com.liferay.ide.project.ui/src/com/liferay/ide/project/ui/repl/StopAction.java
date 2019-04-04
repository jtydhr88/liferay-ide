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

package com.liferay.ide.project.ui.repl;

import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class StopAction extends SnippetAction {

	public StopAction(JavaSnippetEditor editor) {
		super(editor);

		setText(SnippetMessages.getString("StopAction.label"));
		setToolTipText(SnippetMessages.getString("StopAction.tooltip"));
		setDescription(SnippetMessages.getString("StopAction.description"));

		setImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET));
		setDisabledImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET_DISABLED));
		setHoverImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_TOOL_TERMSNIPPET_HOVER));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.TERMINATE_SCRAPBOOK_VM_ACTION);
	}

	@Override
	public void run() {
		getEditor().shutDownVM();
	}

	@Override
	public void snippetStateChanged(JavaSnippetEditor editor) {
		setEnabled(editor != null && editor.isVMLaunched());
	}

}
