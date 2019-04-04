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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.IDataDisplay;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import com.liferay.ide.project.ui.repl.LiferayReplEditor;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class DisplayAction extends EvaluateAction {

	@Override
	protected void displayResult(final IEvaluationResult evaluationResult) {
		if (evaluationResult.hasErrors()) {
			final Display display = JDIDebugUIPlugin.getStandardDisplay();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (display.isDisposed()) {
						return;
					}
					reportErrors(evaluationResult);
					evaluationCleanup();
				}
			});
			return;
		}

		final String snippet = evaluationResult.getSnippet();
		IJavaValue resultValue = evaluationResult.getValue();
		try {
			String sig = null;
			IJavaType type = resultValue.getJavaType();
			if (type != null) {
				sig = type.getSignature();
			}
			if ("V".equals(sig)) {
				displayStringResult(snippet, ActionMessages.DisplayAction_no_result_value);
			} else {
				final String resultString;
				if (sig != null) {
					resultString = NLS.bind(ActionMessages.DisplayAction_type_name_pattern,
							new Object[] { resultValue.getReferenceTypeName() });
				} else {
					resultString = "";
				}
				getDebugModelPresentation().computeDetail(resultValue, new IValueDetailListener() {
					@Override
					public void detailComputed(IValue value, String result) {
						displayStringResult(snippet, NLS.bind(ActionMessages.DisplayAction_result_pattern,
								new Object[] { resultString, trimDisplayResult(result) }));
					}
				});
			}
		} catch (DebugException x) {
			displayStringResult(snippet, getExceptionMessage(x));
		}
	}

	protected void displayStringResult(final String snippet, final String resultString) {
		final IDataDisplay directDisplay = getDirectDataDisplay();
		final Display display = JDIDebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed()) {
					IDataDisplay dataDisplay = getDataDisplay();
					if (dataDisplay != null) {
						if (directDisplay == null) {
							dataDisplay.displayExpression(snippet);
						}
						dataDisplay.displayExpressionValue(trimDisplayResult(resultString));
					}
				}
				evaluationCleanup();
			}
		});
	}

	@Override
	protected void run() {
		IWorkbenchPart part = getTargetPart();
		if (part instanceof LiferayReplEditor) {
			((LiferayReplEditor) part).evalSelection(LiferayReplEditor.RESULT_DISPLAY);
			return;
		}
		super.run();
	}

	public static String trimDisplayResult(String result) {
		int max = DebugUITools.getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_DETAIL_LENGTH);
		if (max > 0 && result.length() > max) {
			result = result.substring(0, max) + "...";
		}
		return result;
	}

}
