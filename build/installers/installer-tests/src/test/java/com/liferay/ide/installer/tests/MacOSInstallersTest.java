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

package com.liferay.ide.installer.tests;

import com.liferay.ide.installer.tests.extensions.TempFolder;
import com.liferay.ide.installer.tests.extensions.TempFolderExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Gregory Amerson
 */
@DisplayName("MacOS installers")
@EnabledOnOs(OS.MAC)
@ExtendWith(TempFolderExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class MacOSInstallersTest {

	private TempFolder tempFolder;
	private Path liferayProjectSdkInstallerPath;
	private Path mountPoint;
	private Path installerExecutable;
	private Path tokenPath = Paths.get(userHomeDir, ".liferay/token");;
	private Path binPath = Paths.get(System.getProperty("user.home"), "Library/PackageManager/bin");
	private Path bladePath = binPath.resolve("blade");
	private Path bndPath = binPath.resolve("bnd");
	private Path gwPath = binPath.resolve("gw");
	private Path jpmPath = binPath.resolve("jpm");

	@BeforeAll
	public void setUp() throws Exception {
		liferayProjectSdkInstallerPath = Paths.get(installersOutputDir, "LiferayProjectSDK-2018.9.14-osx-installer.dmg");

		Assertions.assertTrue(Files.exists(liferayProjectSdkInstallerPath));

		mountPoint = Paths.get(installersOutputDir, "LiferayProjectSDK-2018.9.14-osx-installer.mountpoint");

		ProcessBuilder processBuilder = new ProcessBuilder();

		List<String> args = processBuilder.command();

		args.add("hdiutil");
		args.add("attach");
		args.add("-mountpoint");
		args.add(mountPoint.toString());
		args.add(liferayProjectSdkInstallerPath.toString());

		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();

		Assertions.assertEquals(0, process.waitFor());

		installerExecutable = mountPoint.resolve("LiferayProjectSDK-2018.9.14-osx-installer.app/Contents/MacOS/installbuilder.sh");

		Assertions.assertTrue(Files.exists(installerExecutable));
	}

	@AfterAll
	public void tearDown() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();

		List<String> args = processBuilder.command();

		args.add("hdiutil");
		args.add("detach");
		args.add(mountPoint.toString());

		Process process = processBuilder.start();

		Assertions.assertEquals(0, process.waitFor());
	}

	@DisplayName("headless install skipping workspace init")
	@Test
	public void headlessInstallSkipWorkspace() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();

		List<String> args = processBuilder.command();

		args.add(installerExecutable.toString());
		args.add("--mode");
		args.add("unattended");
		args.add("--initworkspace");
		args.add("skip");
		args.add("--isdxp");
		args.add("ce");
		args.add("--bundleversion");
		args.add("7_1");

		Process process = processBuilder.start();

		String output = _readStream(process.getInputStream());

		Assertions.assertEquals(0, process.waitFor(), output);

		Assertions.assertTrue(Files.exists(bladePath));
		Assertions.assertTrue(Files.exists(bndPath));
		Assertions.assertTrue(Files.exists(gwPath));
		Assertions.assertTrue(Files.exists(jpmPath));
		Assertions.assertFalse(Files.exists(tokenPath));
	}

	private String _readStream(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}

		return stringBuilder.toString();
	}

	@DisplayName("headless install workspace init")
	@Test
	public void headlessInstallInitWorkspace() throws Exception {
		Path workspaceDir = tempFolder.newDirectory("ws");

		ProcessBuilder processBuilder = new ProcessBuilder();

		List<String> args = processBuilder.command();

		args.add(installerExecutable.toString());
		args.add("--mode");
		args.add("unattended");
		args.add("--initworkspace");
		args.add("workspace");
		args.add("--workspace");
		args.add(workspaceDir.toString());
		args.add("--isdxp");
		args.add("ce");
		args.add("--bundleversion");
		args.add("7_1");

		processBuilder.redirectOutput();
		processBuilder.redirectError();

		Process process = processBuilder.start();

		String output = _readStream(process.getInputStream());

		Assertions.assertEquals(0, process.waitFor(), output);

		Path gradlePropertiesPath = workspaceDir.resolve("gradle.properties");

		Assertions.assertTrue(Files.exists(gradlePropertiesPath));

		Assertions.assertTrue(Files.exists(bladePath));
		Assertions.assertTrue(Files.exists(bndPath));
		Assertions.assertTrue(Files.exists(gwPath));
		Assertions.assertTrue(Files.exists(jpmPath));
		Assertions.assertFalse(Files.exists(tokenPath));
	}

	private static final String installersOutputDir = System.getProperty("installersOutputDir");
	private static final String userHomeDir = System.getProperty("user.home");
}