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

import com.liferay.ide.core.util.CoreUtil;

import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.dto.BundleDTO;

/**
 * @author Simon Jiang
 */

public abstract class GogoCommand {

	protected String bsn;
	protected long bid = -1;
	protected BundleDTO bundle;;
	protected Stack<String> statckResult;
	protected GogoBundleDeployer helper;

	public GogoCommand() {

		statckResult = new Stack<String>();
	}

	public void setHelper(GogoBundleDeployer helper) {

		this.helper = helper;
	}

	protected void before()
		throws CoreException {

	}

	protected void after()
		throws CoreException {

	}

	protected abstract void execute()
		throws CoreException;

	public void run()
		throws CoreException {

		before();
		execute();
		after();
	}

	public String getResult() {

		return !statckResult.empty() ? statckResult.pop() : null;
	}

	protected void fillResult(String result) {

		if (!CoreUtil.isNullOrEmpty(result)) {
			statckResult.push(result);
		}
	}

	protected void cleanResult() {

		statckResult.clear();
	}

}
