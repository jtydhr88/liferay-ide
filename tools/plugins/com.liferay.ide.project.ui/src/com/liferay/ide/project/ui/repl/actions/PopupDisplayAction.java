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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.DisplayView;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class PopupDisplayAction extends DisplayAction {

	public static final String ACTION_DEFINITION_ID = "org.eclipse.jdt.debug.ui.commands.Display"; //$NON-NLS-1$

	private String snippet;
	private String resultString;
	private ITextEditor fTextEditor;
	private ISelection fSelectionBeforeEvaluation;

	public PopupDisplayAction() {
		super();
	}

	private void showPopup(StyledText textWidget) {
		IWorkbenchPart part = getTargetPart();
		if (part instanceof ITextEditor) {
			fTextEditor = (ITextEditor) part;
			fSelectionBeforeEvaluation = getTargetSelection();
		}
		DebugPopup displayPopup = new DisplayPopup(getShell(), textWidget);
		displayPopup.open();

	}

	private class DisplayPopup extends DebugPopup {
		public DisplayPopup(Shell shell, StyledText textWidget) {
			super(shell, getPopupAnchor(textWidget), ACTION_DEFINITION_ID);
		}

		@Override
		protected String getActionText() {
			return ActionMessages.PopupDisplayAction_2;
		}

		@Override
		protected void persist() {
			IDataDisplay directDisplay = getDirectDataDisplay();
			Display display = JDIDebugUIPlugin.getStandardDisplay();

			if (!display.isDisposed()) {
				IDataDisplay dataDisplay = getDataDisplay();
				if (dataDisplay != null) {
					if (directDisplay == null) {
						dataDisplay.displayExpression(snippet);
					}
					dataDisplay.displayExpressionValue(resultString);
				}
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			GridData gd = new GridData(GridData.FILL_BOTH);
			StyledText text = new StyledText(parent,
					SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
			text.setLayoutData(gd);

			text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			text.setText(resultString);
			return text;
		}

		@Override
		public boolean close() {
			boolean returnValue = super.close();
			if (fTextEditor != null && fSelectionBeforeEvaluation != null) {
				fTextEditor.getSelectionProvider().setSelection(fSelectionBeforeEvaluation);
				fTextEditor = null;
				fSelectionBeforeEvaluation = null;
			}
			return returnValue;
		}
	}

	@Override
	protected void displayStringResult(String currentSnippet, String currentResultString) {
		IWorkbenchPart part = getTargetPart();
		if (part instanceof DisplayView) {
			super.displayStringResult(currentSnippet, currentResultString);
			return;
		}

		final StyledText textWidget = EvaluateAction.getStyledText(part);
		if (textWidget == null) {
			super.displayStringResult(currentSnippet, currentResultString);
		} else {
			snippet = currentSnippet;
			resultString = currentResultString;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					showPopup(textWidget);
				}
			});
			evaluationCleanup();
		}
	}

}
