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
 * @author Gregory Amerson
 */
@SuppressWarnings("restriction")
public class LiferayReplViewerConfiguration extends JavaSourceViewerConfiguration {

	public LiferayReplViewerConfiguration(
		JavaTextTools javaTextTools, IPreferenceStore preferenceStore, LiferayReplEditor liferayReplEditor) {

		super(javaTextTools.getColorManager(), preferenceStore, liferayReplEditor, IJavaPartitions.JAVA_PARTITIONING);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant contentAssistant = new ContentAssistant();

		contentAssistant.enableColoredLabels(true);

		IContentAssistProcessor contentAssistProcessor = getContentAssistantProcessor();

		if (contentAssistProcessor instanceof LiferayReplCompletionProcessor) {
			((LiferayReplCompletionProcessor)contentAssistProcessor).setContentAssistant(contentAssistant);
		}

		contentAssistant.setContentAssistProcessor(contentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(contentAssistant, getColorManager());

		contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return contentAssistant;
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return new LiferayReplCompletionProcessor((LiferayReplEditor)getEditor());
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}

}