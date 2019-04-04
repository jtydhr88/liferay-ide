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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.actions.OpenAction;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class SnippetOpenOnSelectionAction extends OpenAction {

	protected JavaSnippetEditor fEditor;
	private String fDialogTitle;
	private String fDialogMessage;

	public SnippetOpenOnSelectionAction(JavaSnippetEditor editor) {
		super(editor.getSite());
		fEditor = editor;
		setResources();
		setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);

		IHandler handler = new ActionHandler(this);
		IHandlerService service = editor.getSite().getService(IHandlerService.class);
		service.activateHandler(IJavaEditorActionDefinitionIds.OPEN_EDITOR, handler);
	}

	protected void setResources() {
		setText(SnippetMessages.getString("SnippetOpenOnSelectionAction.label"));
		setDescription(SnippetMessages.getString("SnippetOpenOnSelectionAction.tooltip"));
		setToolTipText(SnippetMessages.getString("SnippetOpenOnSelectionAction.description"));
		setDialogTitle(SnippetMessages.getString("SnippetOpenOnSelectionDialog.title"));
		setDialogMessage(SnippetMessages.getString("SnippetOpenOnSelectionDialog.message"));
	}

	protected void setDialogTitle(String title) {
		fDialogTitle = title;
	}

	protected void setDialogMessage(String message) {
		fDialogMessage = message;
	}

	protected void setEditor(JavaSnippetEditor contentEditor) {
		fEditor = contentEditor;
	}

	/**
	 * Shows a dialog for resolving an ambiguous java element. Utility method that
	 * can be called by sub-classes.
	 */
	protected IJavaElement selectJavaElement(List<IJavaElement> elements, Shell shell, String title, String message) {
		int nResults = elements.size();
		if (nResults == 0) {
			return null;
		}
		if (nResults == 1) {
			return elements.get(0);
		}
		int flags = JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_QUALIFIED
				| JavaElementLabelProvider.SHOW_ROOT;

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new JavaElementLabelProvider(flags));
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements.toArray());
		if (dialog.open() == Window.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults = selection.length;
				for (int i = 0; i < nResults; i++) {
					Object current = selection[i];
					if (current instanceof IJavaElement) {
						return (IJavaElement) current;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Filters out source references from the given code resolve results. A utility
	 * method that can be called by sub-classes.
	 */
	protected List<IJavaElement> filterResolveResults(IJavaElement[] codeResolveResults) {
		int nResults = codeResolveResults.length;
		List<IJavaElement> refs = new ArrayList<>(nResults);
		for (int i = 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference) {
				refs.add(codeResolveResults[i]);
			}
		}
		return refs;
	}

	@Override
	public void run() {
		if (fEditor == null) {
			return;
		}
		try {
			IJavaElement[] result = fEditor.codeResolve();
			if (result != null && result.length > 0) {
				IJavaElement chosen = selectJavaElement(filterResolveResults(result), getShell(), fDialogTitle,
						fDialogMessage);
				if (chosen != null) {
					run(new StructuredSelection(chosen));
					return;
				}
			}
		} catch (JavaModelException x) {
			JDIDebugUIPlugin.log(x);
		}
	}

	/**
	 * @see SelectionDispatchAction#selectionChanged(ITextSelection)
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
		setEnabled(fEditor != null);
	}
}
