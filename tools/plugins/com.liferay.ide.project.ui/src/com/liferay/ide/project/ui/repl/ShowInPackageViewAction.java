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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class ShowInPackageViewAction extends Action {

	private JavaSnippetEditor fEditor;

	/**
	 * Creates a new <code>ShowInPackageViewAction</code>.
	 *
	 * @param site the site providing context information for this action
	 */
	public ShowInPackageViewAction() {
		super(SnippetMessages.getString("ShowInPackageViewAction.label")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("ShowInPackageViewAction.description")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("ShowInPackageViewAction.tooltip")); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.SHOW_IN_PACKAGEVIEW_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call this
	 * constructor.
	 */
	public ShowInPackageViewAction(JavaSnippetEditor editor) {
		this();
		fEditor = editor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IFile file = fEditor.getFile();
		if (file == null) {
			return;
		}
		PackageExplorerPart view = PackageExplorerPart.openInActivePerspective();
		if (!reveal(view, file)) {
			MessageDialog.openInformation(fEditor.getShell(), getDialogTitle(),
					SnippetMessages.getString("ShowInPackageViewAction.not_found")); //$NON-NLS-1$
		}
	}

	private boolean reveal(PackageExplorerPart view, Object element) {
		if (view == null) {
			return false;
		}
		view.selectReveal(new StructuredSelection(element));
		IElementComparer comparer = view.getTreeViewer().getComparer();
		Object selected = getSelectedElement(view);
		if (comparer != null ? comparer.equals(element, selected) : element.equals(selected)) {
			return true;
		}
		return false;
	}

	private Object getSelectedElement(PackageExplorerPart view) {
		return ((IStructuredSelection) view.getSite().getSelectionProvider().getSelection()).getFirstElement();
	}

	private static String getDialogTitle() {
		return SnippetMessages.getString("ShowInPackageViewAction.dialog.title"); //$NON-NLS-1$
	}
}
