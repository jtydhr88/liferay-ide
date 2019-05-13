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

import java.io.File;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Andy Wu
 * @author Terry Jia
 */
public class CustomJSPCompareViewContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		File file = (File)parentElement;

		File[] files = file.listFiles(
			(dir, name) -> {
				if (name.startsWith(".")) {
					return false;
				}

				return true;
			});

		return files;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return ((File)inputElement).listFiles();
	}

	@Override
	public Object getParent(Object element) {
		File file = (File)element;

		return file.getParentFile();
	}

	@Override
	public boolean hasChildren(Object element) {
		File file = (File)element;

		if (file.isDirectory()) {
			return true;
		}

		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}