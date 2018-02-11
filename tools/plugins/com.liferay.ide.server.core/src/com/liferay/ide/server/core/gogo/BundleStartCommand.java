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

package com.liferay.ide.server.core.gogo;

import com.liferay.ide.core.IBundleProject;
import com.liferay.ide.server.core.LiferayServerCore;
import com.liferay.ide.server.core.portal.BundleDTOWithStatus.ResponseState;

import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.Bundle;

/**
 * @author Simon Jiang
 */

public class BundleStartCommand extends BundleCommand {

	public BundleStartCommand(IBundleProject project) {

		super(project);
	}

	@Override
	protected void execute()
		throws CoreException {

		try {
			if (bid != -1) {
				String response = helper.run("start " + bid);
				fillResult(response);
			}
		}
		catch (Exception e) {
			throw new CoreException(LiferayServerCore.createErrorStatus(e));
		}
	}

	@Override
	protected void after()
		throws CoreException {

		super.after();
		try {
			if (bundle != null && bundle.state == Bundle.ACTIVE) {
				setResponseState(ResponseState.ok);
			}
			else {
				setResponseState(ResponseState.error);
			}
		}
		catch (Exception e) {
			throw new CoreException(LiferayServerCore.createErrorStatus(e));
		}
	}
}
