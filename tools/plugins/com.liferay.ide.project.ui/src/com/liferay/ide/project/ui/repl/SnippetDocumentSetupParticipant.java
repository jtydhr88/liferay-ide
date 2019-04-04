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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class SnippetDocumentSetupParticipant implements IDocumentSetupParticipant {

	public SnippetDocumentSetupParticipant() {
	}

	/*
	 * @see
	 * org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.
	 * jface.text.IDocument)
	 */
	@Override
	public void setup(IDocument document) {
		if (document != null) {
			JavaTextTools tools = JDIDebugUIPlugin.getDefault().getJavaTextTools();
			tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		}
	}
}
