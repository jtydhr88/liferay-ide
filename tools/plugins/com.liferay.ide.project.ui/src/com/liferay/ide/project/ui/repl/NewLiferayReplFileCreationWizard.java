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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

import com.liferay.ide.ui.util.UIUtil;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class NewLiferayReplFileCreationWizard extends Wizard implements INewWizard {

	private NewLiferayReplFileWizardPage _newLiferayReplFileWizardPage;
	private IStructuredSelection _structuredSelection;

	public NewLiferayReplFileCreationWizard() {
		setNeedsProgressMonitor(true);

		setWindowTitle(SnippetMessages.getString("NewSnippetFileCreationWizard.title"));
	}

	@Override
	public void addPages() {
		super.addPages();

		if (_structuredSelection == null) {
			IJavaElement elem = _getActiveEditorJavaInput();

			if (elem != null) {
				_structuredSelection = new StructuredSelection(elem);
			}
			else {
				_structuredSelection = StructuredSelection.EMPTY;
			}
		}

		_newLiferayReplFileWizardPage = new NewLiferayReplFileWizardPage(_structuredSelection);

		addPage(_newLiferayReplFileWizardPage);
	}

	@Override
	public boolean performFinish() {
		return _newLiferayReplFileWizardPage.finish();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection structuredSelection) {
		_structuredSelection = structuredSelection;

		setDefaultPageImageDescriptor(JavaDebugImages.getImageDescriptor(JavaDebugImages.IMG_WIZBAN_NEWSCRAPPAGE));
	}

	private IJavaElement _getActiveEditorJavaInput() {
		IWorkbenchPage page = UIUtil.getActivePage();

		if (page != null) {
			IEditorPart part = page.getActiveEditor();

			if (part != null) {
				IEditorInput editorInput = part.getEditorInput();

				if (editorInput != null) {
					return editorInput.getAdapter(IJavaElement.class);
				}
			}
		}

		return null;
	}
}
