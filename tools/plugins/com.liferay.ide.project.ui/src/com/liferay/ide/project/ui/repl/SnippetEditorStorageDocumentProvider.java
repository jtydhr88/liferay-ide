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

import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class SnippetEditorStorageDocumentProvider extends StorageDocumentProvider {

	/*
	 * @see
	 * org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.
	 * Object, org.eclipse.jface.text.IDocument)
	 */
	@Override
	protected void setupDocument(Object element, IDocument document) {
		if (document != null) {
			JavaTextTools tools = JDIDebugUIPlugin.getDefault().getJavaTextTools();
			tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
		}
	}
}
