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

package com.liferay.ide.upgrade.problems.test.apichanges;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.liferay.ide.upgrade.plan.core.UpgradeProblem;
import com.liferay.ide.upgrade.problems.core.FileMigrator;
import com.liferay.ide.upgrade.problems.core.test.Util;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public class DDLRecordLegacyAPITest extends APITestBase {

	@Test
	public void dDLRecordLegacyAPITest() throws Exception {
		FileMigrator fmigrator = context.getService(fileMigrators[0]);

		List<UpgradeProblem> problems = fmigrator.analyze(getTestFile());

		context.ungetService(fileMigrators[0]);

		Assert.assertNotNull(problems);
		Assert.assertEquals("", 3, problems.size());

		UpgradeProblem problem = problems.get(0);

		Assert.assertEquals("", 30, problem.getLineNumber());

		if (Util.isWindows()) {
			Assert.assertEquals("", 1361, problem.getStartOffset());
			Assert.assertEquals("", 1426, problem.getEndOffset());
		}
		else {
			Assert.assertEquals("", 1332, problem.getStartOffset());
			Assert.assertEquals("", 1397, problem.getEndOffset());
		}

		problem = problems.get(1);

		Assert.assertEquals("", 132, problem.getLineNumber());

		if (Util.isWindows()) {
			Assert.assertEquals("", 4220, problem.getStartOffset());
			Assert.assertEquals("", 4263, problem.getEndOffset());
		}
		else {
			Assert.assertEquals("", 4089, problem.getStartOffset());
			Assert.assertEquals("", 4132, problem.getEndOffset());
		}

		problem = problems.get(2);

		Assert.assertEquals("", 145, problem.getLineNumber());

		if (Util.isWindows()) {
			Assert.assertEquals("", 4619, problem.getStartOffset());
			Assert.assertEquals("", 4699, problem.getEndOffset());
		}
		else {
			Assert.assertEquals("", 4475, problem.getStartOffset());
			Assert.assertEquals("", 4554, problem.getEndOffset());
		}
	}

	@Override
	public int getExpectedNumber() {
		return 3;
	}

	@Override
	public String getComponentName() {
		return "com.liferay.ide.upgrade.problems.core.internal.liferay70.DDLRecordLegacyAPI";
	}

	@Override
	public File getTestFile() {
		return new File("projects/legacy-apis-ant-portlet/docroot/WEB-INF/src/com/liferay/EditRecordAction.java");
	}

}