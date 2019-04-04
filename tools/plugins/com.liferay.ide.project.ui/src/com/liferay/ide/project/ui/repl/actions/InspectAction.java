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


import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.liferay.ide.project.ui.repl.JavaSnippetEditor;
import com.liferay.ide.ui.util.UIUtil;

/**
 * @author Terry Jia
 */
@SuppressWarnings({"restriction", "unchecked"})
public class InspectAction extends EvaluateAction {

	@Override
	protected void displayResult(IEvaluationResult result) {
		final Display display= JDIDebugUIPlugin.getStandardDisplay();

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed()) {
					showExpressionView();

					JavaInspectExpression javaInspectExpression = new JavaInspectExpression(result);

					DebugPlugin.getDefault().getExpressionManager().addExpression(javaInspectExpression);
				}
				evaluationCleanup();
			}
		});
	}

	protected void showExpressionView() {
		if (getTargetPart().getSite().getId().equals(IDebugUIConstants.ID_EXPRESSION_VIEW)) {
			return;
		}

		IWorkbenchPage page = UIUtil.getActivePage();

		if (page == null) {
			return;
		}

		IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);

		if (part == null) {
			try {
				page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			}
			catch (PartInitException e) {
				reportError(e.getStatus().getMessage());
			}
		}
		else {
			page.bringToTop(part);
		}
	}

	@Override
	protected void run() {
		IWorkbenchPart part= getTargetPart();

		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor)part).evalSelection(JavaSnippetEditor.RESULT_INSPECT);
			return;
		}

		Object selection= getSelectedObject();

		if (!(selection instanceof IStructuredSelection)) {
			super.run();
			return;
		}

		Iterator<IJavaVariable> variables = ((IStructuredSelection)selection).iterator();

		while (variables.hasNext()) {
			IJavaVariable var = variables.next();

			try {
				JavaInspectExpression expr = new JavaInspectExpression(var.getName(), (IJavaValue)var.getValue());
				DebugPlugin.getDefault().getExpressionManager().addExpression(expr);
			}
			catch (DebugException e) {
				JDIDebugUIPlugin.statusDialog(e.getStatus());
			}
		}

		showExpressionView();
	}

	@Override
	protected IDataDisplay getDataDisplay() {
		return getDirectDataDisplay();
	}
}
