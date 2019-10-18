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

package com.liferay.ide.upgrade.plan.core;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.core.util.StringUtil;
import com.liferay.ide.core.util.ZipUtil;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradePlanCorePlugin extends Plugin {

	public static final String CUSTOMER_OUTLINE_KEY = "customer-outline";

	public static final String DEFAULT_OUTLINE_KEY = "default-outline";

	public static final String ID = "com.liferay.ide.upgrade.plan.core";

	public static final String OFFLINE_UNZIP_FOLDER = "offline-outline";

	public static final List<String> defaultUpgradePlanOutlines = Arrays.asList(
		"https://portal.liferay.dev/docs/7-2/tutorials/-/knowledge_base/t/upgrading-code-to-product-ver",
		"https://portal.liferay.dev/docs/7-2/deploy/-/knowledge_base/d/upgrading-to-product-ver");

	public static IStatus createErrorStatus(String msg) {
		return new Status(IStatus.ERROR, ID, msg);
	}

	public static IStatus createErrorStatus(String msg, Exception e) {
		return new Status(IStatus.ERROR, ID, msg, e);
	}

	public static IUpgradePlanOutline getFilterOutlines(String name) {
		List<IUpgradePlanOutline> lists = new ArrayList<>();

		lists.addAll(getOutlines(DEFAULT_OUTLINE_KEY));
		lists.addAll(getOutlines(CUSTOMER_OUTLINE_KEY));

		for (IUpgradePlanOutline outline : lists) {
			if (StringUtil.equals(name, outline.getName())) {
				return outline;
			}
		}

		return null;
	}

	public static UpgradePlanCorePlugin getInstance() {
		return _instance;
	}

	public static List<IUpgradePlanOutline> getOutlines(String key) {
		IPreferencesService preferencesService = Platform.getPreferencesService();

		String defaultOutlines = preferencesService.getString(UpgradePlanCorePlugin.ID, key, "", null);

		if (CoreUtil.isNullOrEmpty(defaultOutlines)) {
			return Collections.emptyList();
		}

		List<String> outlineList = StringUtil.stringToList(defaultOutlines, "|");

		if (ListUtil.isEmpty(outlineList)) {
			return Collections.emptyList();
		}

		return Lists.transform(
			outlineList,
			new Function<String, IUpgradePlanOutline>() {

				@Override
				public IUpgradePlanOutline apply(String input) {
					String[] outlineArray = StringUtil.stringToArray(input, ",");

					return new UpgradePlanOutline(
						outlineArray[0].trim(), outlineArray[1].trim(), Boolean.parseBoolean(outlineArray[2].trim()));
				}

			});
	}

	public static void logError(String msg) {
		ILog log = _instance.getLog();

		log.log(createErrorStatus(msg));
	}

	public static void logError(String msg, Exception e) {
		ILog log = _instance.getLog();

		log.log(createErrorStatus(msg, e));
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		_instance = this;

		_initDefaultOutline();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		_instance = null;

		super.stop(context);
	}

	private void _initDefaultOutline() throws Exception {
		IPreferencesService preferencesService = Platform.getPreferencesService();

		String defaultOutlines = preferencesService.getString(UpgradePlanCorePlugin.ID, "default-outlines", "", null);

		if (CoreUtil.isNullOrEmpty(defaultOutlines)) {
			IPath pluginStateLocation = _instance.getStateLocation();

			IPath defaultOutlinePath = pluginStateLocation.append(OFFLINE_UNZIP_FOLDER);

			Bundle bundle = Platform.getBundle(UpgradePlanCorePlugin.ID);

			Enumeration<URL> entryUrls = bundle.findEntries("resources/", "*.zip", true);

			if (ListUtil.isEmpty(entryUrls)) {
				return;
			}

			List<UpgradePlanOutline> defaultOutlineLists = new ArrayList<>();

			while (entryUrls.hasMoreElements()) {
				URL fileURL = FileLocator.toFileURL(entryUrls.nextElement());

				File outlineFile = new File(fileURL.getFile());

				ZipUtil.unzip(outlineFile, defaultOutlinePath.toFile());
				String defaultOutlineFileName = FilenameUtils.removeExtension(outlineFile.getName());

				IPath outlinePath = defaultOutlinePath.append(defaultOutlineFileName);

				defaultOutlineLists.add(new UpgradePlanOutline(defaultOutlineFileName, outlinePath.toOSString(), true));
			}

			String defaultOutlineString = StringUtil.objectToString(defaultOutlineLists.iterator(), "|");

			_prefstore.put(DEFAULT_OUTLINE_KEY, defaultOutlineString);

			_prefstore.flush();
		}
	}

	private static UpgradePlanCorePlugin _instance;

	private IEclipsePreferences _prefstore = InstanceScope.INSTANCE.getNode(ID);

}