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

package com.liferay.ide.gradle.core;

import com.liferay.ide.core.Artifact;
import com.liferay.ide.core.ServiceBuilderHelper;
import com.liferay.ide.core.util.FileUtil;
import com.liferay.ide.gradle.core.parser.GradleDependencyUpdater;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.BladeCLIException;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Terry Jia
 */
@Component(property = "type=gradle", service = ServiceBuilderHelper.class)
public class GradleServiceBuilderHelper implements ServiceBuilderHelper {

	@Override
	public List<Artifact> getServiceBuilderDependencies() {
		List<Artifact> dependencies = Collections.emptyList();

		try {
			Path tempFolderPath = Files.createTempDirectory("service-builder-temp");

			StringBuilder sb = new StringBuilder();

			sb.append("create ");
			sb.append("-d \"");
			sb.append(tempFolderPath.toString());
			sb.append("\" ");
			sb.append("-t ");
			sb.append("service-builder");
			sb.append(" ");

			String projectName = "sbtemp";

			sb.append("\"");
			sb.append(projectName);
			sb.append("\" ");

			BladeCLI.execute(sb.toString());

			String serviceBuildGradle = projectName + "/" + projectName + "-service/build.gradle";

			Path serviceBuildGradlePath = tempFolderPath.resolve(serviceBuildGradle);

			String content = FileUtil.readContents(serviceBuildGradlePath.toFile(), true);

			GradleDependencyUpdater gradleDependencyUpdater = new GradleDependencyUpdater(content);

			dependencies = gradleDependencyUpdater.getDependencies(false, "compileOnly");

			dependencies.remove(dependencies.size() - 1);

			File tempFile = tempFolderPath.toFile();

			FileUtil.deleteDir(tempFile, true);
		}
		catch (IOException ioe) {
		}
		catch (BladeCLIException bclie) {
		}

		return dependencies;
	}

}