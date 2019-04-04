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

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.ui.javaeditor.BasicCompilationUnitEditorActionContributor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class SnippetEditorActionContributor extends BasicCompilationUnitEditorActionContributor {

	protected JavaSnippetEditor fSnippetEditor;

	private StopAction fStopAction;
	private SelectImportsAction fSelectImportsAction;
	private SnippetOpenOnSelectionAction fOpenOnSelectionAction;
	private SnippetOpenHierarchyOnSelectionAction fOpenOnTypeSelectionAction;

	public SnippetEditorActionContributor() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.
	 * eclipse.jface.action.IToolBarManager)
	 */
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {

		if (fStopAction == null) {
			toolBarManager.add(new Separator(IJavaDebugUIConstants.EVALUATION_GROUP));
			return;
		}
		toolBarManager.add(fStopAction);
		toolBarManager.add(fSelectImportsAction);
		toolBarManager.update(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.
	 * jface.action.IMenuManager)
	 */
	@Override
	public void contributeToMenu(IMenuManager menu) {
		if (fOpenOnSelectionAction == null) {
			return;
		}
		super.contributeToMenu(menu);

		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnSelectionAction);
			navigateMenu.appendToGroup(IWorkbenchActionConstants.OPEN_EXT, fOpenOnTypeSelectionAction);
			navigateMenu.setVisible(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.
	 * IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {

		super.setActiveEditor(part);
		fSnippetEditor = null;
		if (part instanceof JavaSnippetEditor) {
			fSnippetEditor = (JavaSnippetEditor) part;
			if (fOpenOnSelectionAction == null) {
				initializeActions();
				contributeToMenu(getActionBars().getMenuManager());
				contributeToToolBar(getActionBars().getToolBarManager());
			}
		}

		if (fOpenOnSelectionAction != null) {
			fStopAction.setEditor(fSnippetEditor);
			fSelectImportsAction.setEditor(fSnippetEditor);
			fOpenOnSelectionAction.setEditor(fSnippetEditor);
			fOpenOnTypeSelectionAction.setEditor(fSnippetEditor);
		}

		updateStatus(fSnippetEditor);
	}

	protected void initializeActions() {

		fOpenOnSelectionAction = new SnippetOpenOnSelectionAction(fSnippetEditor);
		fOpenOnTypeSelectionAction = new SnippetOpenHierarchyOnSelectionAction(fSnippetEditor);
		fStopAction = new StopAction(fSnippetEditor);

		fSelectImportsAction = new SelectImportsAction(fSnippetEditor);
		if (fSnippetEditor.getFile() == null) {
			fSelectImportsAction.setEnabled(false);
		}
	}

	protected void updateStatus(JavaSnippetEditor editor) {
		String message = ""; //$NON-NLS-1$
		if (editor != null && editor.isEvaluating()) {
			message = SnippetMessages.getString("SnippetActionContributor.evalMsg"); //$NON-NLS-1$
		}
		getActionBars().getStatusLineManager().setMessage(message);
	}
}
