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

import com.liferay.ide.core.util.FileUtil;

import java.io.File;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * @author Andy Wu
 */
public class CustomJSPCompareViewLabelProvider7x extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		StyledString text = new StyledString();

		File file = (File)element;

		if (file.isDirectory()) {
			text.append(_getFileName(file));

			if (FileUtil.nameEndsWith(file, "resources")) {
				//cell.setImage(_imageProject);
			}
			else {
				//cell.setImage(_imageFolder);
			}

			String[] files = file.list(
				(dir, name) -> {
					if (!name.startsWith(".")) {
						return true;
					}
					else {
						return false;
					}
				});

			if (files != null) {
				text.append(" (" + files.length + ") ", StyledString.COUNTER_STYLER);
			}
		}
		else {
			//cell.setImage(_imageFile);

			text.append(_getFileName(file));

			//			if (_is70FileFound(file)) {
			//				text.append("(found)", StyledString.COUNTER_STYLER);
			//			}
			//			else {
			//				text.append("(unfound)", StyledString.DECORATIONS_STYLER);
			//			}
		}

		cell.setText(text.toString());
		cell.setStyleRanges(text.getStyleRanges());

		super.update(cell);
	}

	private String _getFileName(File file) {
		String name = file.getName();

		if (name.equals("resources")) {
			File parentFile = file.getParentFile();

			parentFile = parentFile.getParentFile();

			parentFile = parentFile.getParentFile();

			parentFile = parentFile.getParentFile();

			parentFile = parentFile.getParentFile();

			return parentFile.getName();
		}
		else {
			if (name.isEmpty()) {
				return file.getPath();
			}

			return name;
		}
	}

}