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
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class SelectImportsAction extends SnippetAction {

	public SelectImportsAction(LiferayReplEditor editor) {
		super(editor);
		setText(SnippetMessages.getString("SelectImports.label")); //$NON-NLS-1$
		setToolTipText(SnippetMessages.getString("SelectImports.tooltip")); //$NON-NLS-1$
		setDescription(SnippetMessages.getString("SelectImports.description")); //$NON-NLS-1$
		ISharedImages sharedImages = JavaUI.getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJS_IMPCONT));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaDebugHelpContextIds.SCRAPBOOK_IMPORTS_ACTION);
	}

	/**
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		if (!getEditor().isInJavaProject()) {
			getEditor().reportNotInJavaProjectError();
			return;
		}
		chooseImports();
	}

	private void chooseImports() {
		String[] imports = getEditor().getImports();
		Dialog dialog = new SelectImportsDialog(getEditor(), imports);
		dialog.open();
	}

	/**
	 * @see ILiferayReplStateChangedListener#snippetStateChanged(LiferayReplEditor)
	 */
	@Override
	public void snippetStateChanged(LiferayReplEditor editor) {
		setEnabled(editor != null && !editor.isEvaluating());
	}
}
