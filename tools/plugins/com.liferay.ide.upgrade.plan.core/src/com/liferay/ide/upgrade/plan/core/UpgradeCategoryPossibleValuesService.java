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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sapphire.PossibleValuesService;

/**
 * @author Simon Jiang
 */
public class UpgradeCategoryPossibleValuesService extends PossibleValuesService {

	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected void compute(Set<String> values) {
		values.addAll(_upgradeCategoryValues);
	}

	@Override
	protected void initPossibleValuesService() {
		_upgradeCategoryValues = new HashSet<>();

		List<UpgradeTaskCategory> upgradeCategories = NewUpgradePlanOpMethods.getUpgradeCategories();

		Stream<UpgradeTaskCategory> upgradeCategorieStream = upgradeCategories.stream();

		_upgradeCategoryValues = upgradeCategorieStream.map(
			upgradeTaskCategory -> upgradeTaskCategory.getId()
		).collect(
			Collectors.toSet()
		);
	}

	private Set<String> _upgradeCategoryValues;

}