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

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;

/**
 * @author Gregory Amerson
 */
public class SelectImportsAction extends ReplAction {

	public SelectImportsAction(LiferayReplEditor liferayReplEditor) {
		super(liferayReplEditor);

		setText("Select Imports");
		setToolTipText("Select Imports");
		setDescription("Select Imports");
		ISharedImages sharedImages = JavaUI.getSharedImages();

		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJS_IMPCONT));

	}

	@Override
	public void run() {
		LiferayReplEditor editor = getEditor();

		if (!editor.isInJavaProject()) {
			editor.reportNotInJavaProjectError();

			return;
		}

		_chooseImports();
	}

	private void _chooseImports() {
		LiferayReplEditor editor = getEditor();

		String[] imports = editor.getImports();

		//new SelectImportsDialog(editor, imports).open();
	}

}