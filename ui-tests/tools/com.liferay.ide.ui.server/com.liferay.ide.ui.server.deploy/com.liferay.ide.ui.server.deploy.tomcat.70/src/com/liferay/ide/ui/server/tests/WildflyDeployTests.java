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

package com.liferay.ide.ui.server.tests;

import com.liferay.ide.ui.liferay.support.server.PureWildfly70Support;
import com.liferay.ide.ui.liferay.support.server.ServerSupport;
import com.liferay.ide.ui.liferay.util.RuleUtil;
import com.liferay.ide.ui.server.deploy.base.Server7xDeployBase;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Terry Jia
 * @author Rui Wang
 */
public class WildflyDeployTests extends Server7xDeployBase {

	@ClassRule
	public static RuleChain chain = RuleUtil.getTomcat7xRunningRuleChain(bot, getServer());

	public static ServerSupport getServer() {
		if ((server == null) || !(server instanceof PureWildfly70Support)) {
			server = new PureWildfly70Support(bot);
		}

		return server;
	}

	@Test
	public void deployFragment() {
		super.deployFragment();
	}

	@Test
	public void deployModule() {
		super.deployModule();
	}

	@Test
	public void deployWar() {
		super.deployWar();
	}

	@Override
	protected String getVersion() {
		return "7.0";
	}

}