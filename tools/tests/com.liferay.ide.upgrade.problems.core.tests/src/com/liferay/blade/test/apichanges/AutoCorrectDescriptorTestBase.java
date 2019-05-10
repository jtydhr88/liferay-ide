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

package com.liferay.blade.test.apichanges;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.liferay.ide.upgrade.plan.core.UpgradeProblem;
import com.liferay.ide.upgrade.problems.core.AutoFileMigrator;
import com.liferay.ide.upgrade.problems.core.FileMigrator;

/**
 * @author Seiphon Wang
 */
public abstract class AutoCorrectDescriptorTestBase {

	@Test
	public void autoCorrectProblems() throws Exception {
		File tempFolder = Files.createTempDirectory("autocorrect").toFile();

		File testFile = new File(tempFolder, "test.xml");

		tempFolder.deleteOnExit();

		Files.copy(getOriginalTestFile().toPath(), testFile.toPath());

		List<UpgradeProblem> problems = null;
		FileMigrator migrator = null;

		Collection<ServiceReference<FileMigrator>> mrefs =
			context.getServiceReferences(FileMigrator.class, "(version=" + getVersion() + ")");

		for (ServiceReference<FileMigrator> mref : mrefs) {
			migrator = context.getService(mref);

			Class<?> clazz = migrator.getClass();

			if (clazz.getName().contains(getImplClassName())) {
				problems = migrator.analyze(testFile);

				break;
			}
		}

		Assert.assertEquals("", 1, problems.size());

		File dest = new File(tempFolder, "Updated.xml");

		Files.copy(testFile.toPath(), dest.toPath());

		int problemsFixed = ((AutoFileMigrator)migrator).correctProblems(dest, problems);

		Assert.assertEquals("", 1, problemsFixed);

		problems = migrator.analyze(dest);

		Assert.assertEquals("", 0, problems.size());
	}

	public abstract String getImplClassName();

	public abstract File getOriginalTestFile();

	public abstract String getVersion();

	protected BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

}
