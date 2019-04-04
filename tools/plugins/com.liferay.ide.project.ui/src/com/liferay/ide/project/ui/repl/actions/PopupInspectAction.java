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

package com.liferay.ide.project.ui.repl.actions;

import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class PopupInspectAction extends InspectAction {

	public static final String ACTION_DEFININITION_ID = "org.eclipse.jdt.debug.ui.commands.Inspect"; //$NON-NLS-1$

	JavaInspectExpression expression;

	private ITextEditor fTextEditor;
	private ISelection fSelectionBeforeEvaluation;

	@Override
	protected void displayResult(final IEvaluationResult result) {
		IWorkbenchPart part = getTargetPart();
		final StyledText styledText = getStyledText(part);
		if (styledText == null) {
			super.displayResult(result);
		} else {
			expression = new JavaInspectExpression(result);
			JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					showPopup(styledText);
				}
			});
		}
		evaluationCleanup();
	}

	protected void showPopup(StyledText textWidget) {
		IWorkbenchPart part = getTargetPart();
		if (part instanceof ITextEditor) {
			fTextEditor = (ITextEditor) part;
			fSelectionBeforeEvaluation = getTargetSelection();
		}
		DebugPopup displayPopup = new InspectPopupDialog(getShell(), getPopupAnchor(textWidget), ACTION_DEFININITION_ID,
				expression) {
			@Override
			public boolean close() {
				boolean returnValue = super.close();
				if (fTextEditor != null && fTextEditor.getSelectionProvider() != null
						&& fSelectionBeforeEvaluation != null) {
					fTextEditor.getSelectionProvider().setSelection(fSelectionBeforeEvaluation);
					fTextEditor = null;
					fSelectionBeforeEvaluation = null;
				}
				return returnValue;
			}
		};
		displayPopup.open();
	}
}
