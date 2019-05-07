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

package com.liferay.ide.upgrade.problems.core.internal;

import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.upgrade.plan.core.UpgradeProblem;
import com.liferay.ide.upgrade.problems.core.AutoFileMigrateException;
import com.liferay.ide.upgrade.problems.core.AutoFileMigrator;
import com.liferay.ide.upgrade.problems.core.CUCache;
import com.liferay.ide.upgrade.problems.core.FileSearchResult;
import com.liferay.ide.upgrade.problems.core.JavaFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings("rawtypes")
public abstract class JavaImportsMigrator extends AbstractFileMigrator<JavaFile> implements AutoFileMigrator {

	public static String getPrefix() {
		return _PREFIX;
	}

	public JavaImportsMigrator() {
		super(JavaFile.class);
	}

	public JavaImportsMigrator(Map<String, String> importFixes) {
		super(JavaFile.class);

		_importFixes = importFixes;
	}

	@Override
	public int correctProblems(File file, Collection<UpgradeProblem> upgradeProblems) throws AutoFileMigrateException {
		int problemsFixed = 0;

		List<String> importsToRewrite = new ArrayList<>();

		for (UpgradeProblem upgradeProblem : upgradeProblems) {
			boolean problemFound = false;

			if (upgradeProblem.getAutoCorrectContext() instanceof String) {
				String importData = upgradeProblem.getAutoCorrectContext();

				if ((importData != null) && importData.startsWith(_PREFIX)) {
					String importValue = importData.substring(_PREFIX.length());

					if (_importFixes.containsKey(importValue)) {
						importsToRewrite.add(upgradeProblem.getLineNumber() + "," + importValue);

						problemFound = true;
					}
				}
			}

			if (problemFound) {
				problemsFixed++;
			}
		}

		if (ListUtil.isNotEmpty(importsToRewrite)) {
			try (InputStream inputStream = Files.newInputStream(file.toPath())) {
				String[] lines = _readLines(inputStream);

				String[] editedLines = new String[lines.length];

				System.arraycopy(lines, 0, editedLines, 0, lines.length);

				for (String importData : importsToRewrite) {
					String[] importMap = importData.split(",");

					try {
						int lineNumber = Integer.parseInt(importMap[0]);

						String importName = importMap[1];

						if ((lineNumber > 0) && (lineNumber < editedLines.length)) {
							editedLines[lineNumber - 1] = editedLines[lineNumber - 1].replaceAll(
								importName, _importFixes.get(importName));
						}
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}

				StringBuilder sb = new StringBuilder();

				for (String editedLine : editedLines) {
					sb.append(editedLine);
					sb.append(System.getProperty("line.separator"));
				}

				try (FileWriter writer = new FileWriter(file)) {
					writer.write(sb.toString());
				}

				_clearCache(file);

				return problemsFixed;
			}
			catch (IOException ioe) {
				throw new AutoFileMigrateException("Unable to auto-correct", ioe);
			}
		}

		return 0;
	}

	@Override
	public List<FileSearchResult> searchFile(File file, JavaFile javaFile) {
		List<FileSearchResult> searchResults = new ArrayList<>();

		for (String importName : _importFixes.keySet()) {
			FileSearchResult importResult = javaFile.findImport(importName);

			if (importResult != null) {
				importResult.autoCorrectContext = _PREFIX + importName;

				searchResults.add(importResult);
			}
		}

		return searchResults;
	}

	public void setImportFixes(Map<String, String> importFixes) {
		_importFixes = importFixes;
	}

	private static String[] _readLines(InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}

		List<String> lines = new ArrayList<>();

		try (InputStreamReader inputReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputReader)) {

			String line;

			while ((line = bufferedReader.readLine()) != null) {
				StringBuffer contents = new StringBuffer(line);

				lines.add(contents.toString());
			}
		}
		catch (Exception e) {
		}

		return lines.toArray(new String[lines.size()]);
	}

	private void _clearCache(File file) {
		try {
			Collection<ServiceReference<CUCache>> src = context.getServiceReferences(CUCache.class, null);

			for (ServiceReference<CUCache> sr : src) {
				CUCache cache = context.getService(sr);

				cache.unget(file);
			}
		}
		catch (InvalidSyntaxException ise) {
			ise.printStackTrace();
		}
	}

	private static final String _PREFIX = "import:";

	private Map<String, String> _importFixes;

}