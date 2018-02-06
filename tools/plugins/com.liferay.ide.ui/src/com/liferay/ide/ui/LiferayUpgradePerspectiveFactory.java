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

package com.liferay.ide.ui;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author Lovett Li
 */
public class LiferayUpgradePerspectiveFactory extends AbstractPerspectiveFactory {

	public static final String ID = "com.liferay.ide.eclipse.ui.perspective.liferayupgrade";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		_createLayout(layout);
		addShortcuts(layout);
	}

	private void _createLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.20F, editorArea);

		topLeft.addView(ID_PROJECT_EXPLORER_VIEW);
		topLeft.addPlaceholder(ID_PACKAGE_EXPLORER_VIEW);
		topLeft.addPlaceholder(ID_J2EE_HIERARCHY_VIEW);
		topLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
		topLeft.addPlaceholder(JavaUI.ID_PACKAGES_VIEW);

		((ModeledPageLayout)layout).stackView(ID_LIFERAY_UPGRADE_VIEW, layout.getEditorArea(), true);

		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.8F, editorArea);

		bottom.addView(ID_MARKERS_VIEW);
		bottom.addView(ID_CONSOLE_VIEW);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IProgressConstants.PROGRESS_VIEW_ID);
		bottom.addView(ID_SEARCH_VIEW);
	}

}