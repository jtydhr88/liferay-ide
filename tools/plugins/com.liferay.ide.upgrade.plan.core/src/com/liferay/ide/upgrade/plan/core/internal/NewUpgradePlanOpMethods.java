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

package com.liferay.ide.upgrade.plan.core.internal;

import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.upgrade.plan.core.NewUpgradePlanOp;
import com.liferay.ide.upgrade.plan.core.UpgradeCategoryElement;
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskCategory;
import com.liferay.ide.upgrade.plan.core.util.ServicesLookup;

import java.nio.file.Paths;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ProgressMonitor;
import org.eclipse.sapphire.modeling.Status;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class NewUpgradePlanOpMethods {

	public static String upgradeCategoryCode = "code";

	public static final Status execute(NewUpgradePlanOp newUpgradePlanOp, ProgressMonitor progressMonitor) {
		ServiceTracker<UpgradePlanner, UpgradePlanner> serviceTracker = _getServiceTracker();

		UpgradePlanner upgradePlanner = serviceTracker.getService();

		if (upgradePlanner == null) {
			return Status.createErrorStatus("Could not get UpgradePlanner service");
		}

		String name = SapphireUtil.getContent(newUpgradePlanOp.getName());

		String currentVersion = SapphireUtil.getContent(newUpgradePlanOp.getCurrentVersion());

		String targetVersion = SapphireUtil.getContent(newUpgradePlanOp.getTargetVersion());

		Path path = SapphireUtil.getContent(newUpgradePlanOp.getLocation());

		ElementList<UpgradeCategoryElement> selectedUpgradeCategories = newUpgradePlanOp.getSelectedUpgradeCategories();

		Stream<UpgradeCategoryElement> upgradeCategoryElements = selectedUpgradeCategories.stream();

		List<String> categories = upgradeCategoryElements.map(
			category -> SapphireUtil.getContent(category.getUpgradeCategory())
		).collect(
			Collectors.toList()
		);

		java.nio.file.Path sourceCodeLocation = null;

		if (path != null) {
			sourceCodeLocation = Paths.get(path.toOSString());
		}

		UpgradePlan upgradePlan = upgradePlanner.newUpgradePlan(
			name, currentVersion, targetVersion, sourceCodeLocation, categories);

		if (upgradePlan == null) {
			return Status.createErrorStatus("Could not create upgrade plan named: " + name);
		}

		upgradePlanner.startUpgradePlan(upgradePlan);

		return Status.createOkStatus();
	}

	public static List<UpgradeTaskCategory> getUpgradeCategories() {
		try {
			Bundle bundle = FrameworkUtil.getBundle(NewUpgradePlanOpMethods.class);

			BundleContext bundleContext = bundle.getBundleContext();

			return ServicesLookup.getOrderedServices(
				bundleContext, bundleContext.getServiceReferences(UpgradeTaskCategory.class, null));
		}
		catch (InvalidSyntaxException ise) {
		}

		return Collections.emptyList();
	}

	private static ServiceTracker<UpgradePlanner, UpgradePlanner> _getServiceTracker() {
		if (_serviceTracker == null) {
			Bundle bundle = FrameworkUtil.getBundle(NewUpgradePlanOpMethods.class);

			BundleContext bundleContext = bundle.getBundleContext();

			_serviceTracker = new ServiceTracker<>(bundleContext, UpgradePlanner.class, null);

			_serviceTracker.open();
		}

		return _serviceTracker;
	}

	private static ServiceTracker<UpgradePlanner, UpgradePlanner> _serviceTracker;

}