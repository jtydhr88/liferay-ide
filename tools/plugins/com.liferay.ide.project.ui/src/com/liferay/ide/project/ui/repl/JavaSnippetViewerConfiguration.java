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

import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class JavaSnippetViewerConfiguration extends JavaSourceViewerConfiguration {

	public JavaSnippetViewerConfiguration(JavaTextTools tools, IPreferenceStore preferenceStore,
			LiferayReplEditor editor) {
		super(tools.getColorManager(), preferenceStore, editor, IJavaPartitions.JAVA_PARTITIONING);
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return new JavaSnippetCompletionProcessor((LiferayReplEditor) getEditor());
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		assistant.enableColoredLabels(true);

		IContentAssistProcessor contentAssistProcessor = getContentAssistantProcessor();

		if (contentAssistProcessor instanceof JavaSnippetCompletionProcessor) {
			((JavaSnippetCompletionProcessor) contentAssistProcessor).setContentAssistant(assistant);
		}

		assistant.setContentAssistProcessor(contentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}
}
