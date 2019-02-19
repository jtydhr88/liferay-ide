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

import com.liferay.ide.upgrade.plan.core.internal.NewUpgradePlanOpMethods;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Result;
import org.eclipse.sapphire.services.ValueImageService;

/**
 * @author Simon Jiang
 */
public final class UpgradeCategoryValueImageService extends ValueImageService {

	@Override
	public ImageData provide(final String value)
	{

		if (value != null) {
			List<UpgradeTaskCategory> upgradeCategories = NewUpgradePlanOpMethods.getUpgradeCategories();

			Stream<UpgradeTaskCategory> upgradeCategorieStream = upgradeCategories.stream();

			UpgradeTaskCategory upgradeTaskCategory = upgradeCategorieStream.filter(
				upgradeCategory -> value.equalsIgnoreCase(upgradeCategory.getId())
			).findFirst(
			).get();

			String imagePath = upgradeTaskCategory.getImagePath();

			Result<ImageData> imageResult = ImageData.readFromClassLoader(
				UpgradeCategoryValueImageService.class, imagePath);

			return imageResult.required();
		}

		return null;
	}

	@Override
	protected void init() {
		if (_upgradeCategories == null) {
			_upgradeCategories = NewUpgradePlanOpMethods.getUpgradeCategories();
		}
	}

	private static List<UpgradeTaskCategory> _upgradeCategories;

}