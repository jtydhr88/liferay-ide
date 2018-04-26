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

package com.liferay.ide.server.ui.action;

import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.ui.util.SWTUtil;

import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.DeleteServerDialogExtension;

/**
 * @author Simon Jiang
 */
public class LiferayPortalDeleteServerAction extends DeleteServerDialogExtension {

	@Override
	public void createControl(Composite parent) {
		SWTUtil.createCheckButton(parent, "Delete Related Runtime", null, false, 1);
	}

	@Override
	public boolean isEnabled() {
		return ListUtil.isNotEmpty(servers);
	}

	@Override
	public void performPostDeleteAction(IProgressMonitor monitor) {
		Stream<IServer> serverStream = Stream.of(servers);

		serverStream.forEach(
			server -> {
				try {
					IRuntime runtime = server.getRuntime();

					server.delete();

					if (runtime != null) {
						runtime.delete();
					}
				}
				catch (CoreException ce) {
					LiferayServerCore.logError(ce);
				}
			});
	}

	@Override
	public void performPreDeleteAction(IProgressMonitor monitor) {
	}

}