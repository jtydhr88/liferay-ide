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

package com.liferay.ide.project.core.util;

import com.liferay.ide.project.core.ITargetPlatformConstant;
import com.liferay.ide.project.core.ProjectCore;
import com.liferay.ide.project.core.modules.ServiceContainer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

/**
 * @author Lovett Li
 * @author Simon Jiang
 */
public class TargetPlatformUtil {

	public static List<String> getAllTargetPlatfromVersions() throws IOException {
		final URL url = FileLocator.toFileURL(_getProjectCoreBundle().getEntry("OSGI-INF/target-platform"));

		final File targetPlatfolder = new File(url.getFile());

		final List<String> tpVersionList = new ArrayList<>();

		if (targetPlatfolder.isDirectory()) {
			File[] tpVersionFolder = targetPlatfolder.listFiles();

			if (tpVersionFolder != null) {
				for (File tp : tpVersionFolder) {
					String tpVersion = tp.getName().split("-", 2)[1].toUpperCase();

					tpVersionList.add(tpVersion);
				}
			}
		}

		return tpVersionList;
	}

	public static ServiceContainer getServiceBundle(String serviceName) throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("service-dependency");

		return _getBundleAndVersion(tpIndexFile, serviceName);
	}

	public static ServiceContainer getServiceDependencyList() throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("service-dependency");

		return _getServicesDependencyNameList(tpIndexFile);
	}

	public static ServiceContainer getServicesList() throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("service");

		return _getServicesNameList(tpIndexFile);
	}

	public static ServiceContainer getServiceWrapperBundle(String servicewrapperName) throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("servicewrapper-dependency");

		return _getBundleAndVersion(tpIndexFile, servicewrapperName);
	}

	public static ServiceContainer getServiceWrapperDependencyList() throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("servicewrapper-dependency");

		return _getServicesDependencyNameList(tpIndexFile);
	}

	public static ServiceContainer getServiceWrapperList() throws Exception {
		File tpIndexFile = _checkCurrentTargetPlatform("servicewrapper");

		return _getServicesNameList(tpIndexFile);
	}

	@SuppressWarnings("unchecked")
	public static ServiceContainer getThirdPartyBundleList(String serviceName) throws Exception {
		final URL url = FileLocator.toFileURL(
			_getProjectCoreBundle().getEntry("OSGI-INF/liferay-thirdparty-bundles.json"));

		final File tpFile = new File(url.getFile());

		final ObjectMapper mapper = new ObjectMapper();

		Map<String, List<String>> map = mapper.readValue(tpFile, Map.class);

		List<String> serviceBundle = map.get(serviceName);

		if ((serviceBundle != null) && !serviceBundle.isEmpty()) {
			return new ServiceContainer(serviceBundle.get(0), serviceBundle.get(1), serviceBundle.get(2));
		}

		return null;
	}

	private static File _checkCurrentTargetPlatform(String type) throws IOException {
		String currentVersion = Platform.getPreferencesService().getString(
			ProjectCore.PLUGIN_ID, ITargetPlatformConstant.CURRENT_TARGETFORM_VERSION,
			ITargetPlatformConstant.DEFAULT_TARGETFORM_VERSION, null);

		String repalceVersionString = currentVersion.replace("[", "");

		currentVersion = repalceVersionString.replace("]", "").toLowerCase();

		return _useSpecificTargetPlatform(currentVersion, type);
	}

	@SuppressWarnings("unchecked")
	private static ServiceContainer _getBundleAndVersion(File tpFile, String serviceName) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();

		Map<String, List<String>> map = mapper.readValue(tpFile, Map.class);

		List<String> serviceBundle = map.get(serviceName);

		if ((serviceBundle != null) && !serviceBundle.isEmpty()) {
			return new ServiceContainer(serviceBundle.get(0), serviceBundle.get(1), serviceBundle.get(2));
		}

		return null;
	}

	private static Bundle _getProjectCoreBundle() {
		return ProjectCore.getDefault().getBundle();
	}

	@SuppressWarnings("unchecked")
	private static ServiceContainer _getServicesDependencyNameList(File tpFile) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();

		Map<String, String[]> serviceDependencyList = mapper.readValue(tpFile, Map.class);

		String[] services = serviceDependencyList.keySet().toArray(new String[0]);

		return new ServiceContainer(Arrays.asList(services));
	}

	@SuppressWarnings("unchecked")
	private static ServiceContainer _getServicesNameList(File tpFile) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();

		List<String> serviceList = mapper.readValue(tpFile, List.class);

		String[] services = serviceList.toArray(new String[0]);

		return new ServiceContainer(Arrays.asList(services));
	}

	private static File _useSpecificTargetPlatform(String currentVersion, String type) throws IOException {
		URL url;
		url = FileLocator.toFileURL(
			_getProjectCoreBundle().getEntry("OSGI-INF/target-platform/liferay-" + currentVersion));

		final File tpFolder = new File(url.getFile());

		File[] listFiles = tpFolder.listFiles(
			new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (type.equals("service") && name.endsWith("services.json")) {
						return true;
					}

					if (type.equals("servicewrapper") && name.endsWith("servicewrappers.json")) {
						return true;
					}

					if (type.equals("service-dependency") && name.endsWith("service-dependency.json")) {
						return true;
					}

					if (type.equals("servicewrapper-dependency") && name.endsWith("servicewrapper-dependency.json")) {
						return true;
					}

					return false;
				}

			});

		return listFiles[0];
	}

}