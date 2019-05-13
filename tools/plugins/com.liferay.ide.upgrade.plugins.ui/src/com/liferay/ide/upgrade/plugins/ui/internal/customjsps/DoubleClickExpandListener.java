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

package com.liferay.ide.upgrade.plugins.ui.internal.customjsps;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Andy Wu
 */
public class DoubleClickExpandListener implements IDoubleClickListener {

	public DoubleClickExpandListener(TreeViewer treeViewer) {
		_treeViewer = treeViewer;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		final IStructuredSelection selection = (IStructuredSelection)event.getSelection();

		if ((selection == null) || selection.isEmpty()) {
			return;
		}

		Object obj = selection.getFirstElement();

		ITreeContentProvider provider = (ITreeContentProvider)_treeViewer.getContentProvider();

		if (!provider.hasChildren(obj)) {
			return;
		}

		if (_treeViewer.getExpandedState(obj)) {
			_treeViewer.collapseToLevel(obj, AbstractTreeViewer.ALL_LEVELS);
		}
		else {
			_treeViewer.expandToLevel(obj, 1);
		}
	}

	private TreeViewer _treeViewer;

}