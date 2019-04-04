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

import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import com.liferay.ide.project.ui.repl.JavaSnippetEditor;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class ExecuteAction extends EvaluateAction {

	@Override
	protected void displayResult(final IEvaluationResult result) {
		if (result.hasErrors()) {
			final Display display = JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(result);
					evaluationCleanup();
				}
			});
		} else {
			evaluationCleanup();
		}
	}

	@Override
	protected void run() {
		IWorkbenchPart part = getTargetPart();
		if (part instanceof JavaSnippetEditor) {
			((JavaSnippetEditor) part).evalSelection(JavaSnippetEditor.RESULT_RUN);
			return;
		}
		super.run();
	}

	@Override
	protected IDataDisplay getDataDisplay() {
		return super.getDirectDataDisplay();
	}

}
