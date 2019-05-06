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

package com.liferay.ide.upgrade.plan.ui.internal;

import javax.inject.Named;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Christopher Bryan Boyd
 * @author Terry Jia
 */
public class ExpandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof UpgradePlanView) {
			UpgradePlanView upgradePlanView = (UpgradePlanView)part;

			_execute(upgradePlanView);
		}

		return null;
	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		if (activePart == null) {
			return;
		}

		if (activePart.getObject() instanceof UpgradePlanView) {
			UpgradePlanView upgradePlanView = (UpgradePlanView)activePart.getObject();

			_execute(upgradePlanView);
		}
	}

	private void _execute(UpgradePlanView upgradePlanView) {
		UpgradePlanViewer upgradePlanViewer = upgradePlanView.getUpgradePlanViewer();

		TreeViewer treeViewer = upgradePlanViewer.getTreeViewer();

		Tree tree = treeViewer.getTree();

		try {
			tree.setRedraw(false);
			treeViewer.expandAll();
		}
		finally {
			tree.setRedraw(true);
		}
	}

}