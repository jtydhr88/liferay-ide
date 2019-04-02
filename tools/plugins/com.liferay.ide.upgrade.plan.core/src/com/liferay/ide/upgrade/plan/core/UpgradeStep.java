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

import com.liferay.ide.core.util.ListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Adapters;

/**
 * @author Gregory Amerson
 * @author Simon Jiang
 * @author Terry Jia
 */
public class UpgradeStep {

	public UpgradeStep(
		UpgradePlanner upgradePlanner, String title, String description, String imagePath, String url,
		String requirement, UpgradeStepStatus status, String commandId, UpgradeStep parent) {

		_upgradePlanner = upgradePlanner;
		_title = title;
		_description = description;
		_icon = imagePath;
		_url = url;
		_requirement = requirement;
		_status = status;
		_commandId = commandId;
		_parent = parent;
	}

	public void appendChild(UpgradeStep upgradeStep) {
		_children.add(upgradeStep);
	}

	public boolean completed() {
		if (ListUtil.isNotEmpty(_children)) {
			for (UpgradeStep childUpgradeStep : _children) {
				if (!childUpgradeStep.completed()) {
					return false;
				}
			}

			return true;
		}

		if ((getStatus() == UpgradeStepStatus.COMPLETED) || (getStatus() == UpgradeStepStatus.SKIPPED)) {
			return true;
		}

		return false;
	}

	public boolean enabled() {
		if (_parent == null) {
			return true;
		}

		List<UpgradeStep> siblings = _parent.getChildren();

		for (UpgradeStep sibling : siblings) {
			if (sibling.equals(this)) {
				break;
			}

			if (_isRequiredIncompleted(sibling)) {
				return false;
			}
		}

		if (_parent.enabled()) {
			return true;
		}

		return false;
	}

	public boolean equals(Object object) {
		if ((object instanceof UpgradeStep) == false) {
			return false;
		}

		UpgradeStep baseUpgradeStep = Adapters.adapt(object, UpgradeStep.class);

		if (baseUpgradeStep == null) {
			return false;
		}

		if (isEqualIgnoreCase(_description, baseUpgradeStep._description) &&
			isEqualIgnoreCase(_icon, baseUpgradeStep._icon) &&
			isEqualIgnoreCase(_requirement, baseUpgradeStep._requirement) &&
			isEqualIgnoreCase(_commandId, baseUpgradeStep._commandId) &&
			isEqualIgnoreCase(_url, baseUpgradeStep._url) && isEqual(_children, baseUpgradeStep._children) &&
			isEqualIgnoreCase(_title, baseUpgradeStep._title) && _status.equals(baseUpgradeStep._status)) {

			return true;
		}

		return false;
	}

	public List<UpgradeStep> getChildren() {
		return _children;
	}

	public String getCommandId() {
		return _commandId;
	}

	public String getDescription() {
		return "<form>" + _description + "</form>";
	}

	public String getIcon() {
		if (completed()) {
			return "icons/completed.gif";
		}
		else if (_status.equals(UpgradeStepStatus.FAILED)) {
			return "icons/failed.png";
		}

		// TODO need to use the special icon instead

		return "icons/step_default.gif";
	}

	public UpgradeStep getParent() {
		return _parent;
	}

	public UpgradeStepRequirement getRequirement() {
		return UpgradeStepRequirement.valueOf(UpgradeStepRequirement.class, _requirement.toUpperCase());
	}

	public UpgradeStepStatus getStatus() {
		return _status;
	}

	public String getTitle() {
		return _title;
	}

	public String getUrl() {
		return _url;
	}

	public int hashCode() {
		int hash = 31;

		hash = 31 * hash + (_description != null ? _description.hashCode() : 0);
		hash = 31 * hash + (_icon != null ? _icon.hashCode() : 0);

		Stream<UpgradeStep> childrenStream = _children.stream();

		int childrenHashCodes = childrenStream.map(
			child -> child.hashCode()
		).reduce(
			0, Integer::sum
		).intValue();

		hash = 31 * hash + (_requirement != null ? _requirement.hashCode() : 0);
		hash = 31 * hash + childrenHashCodes;
		hash = 31 * hash + (_url != null ? _url.hashCode() : 0);
		hash = 31 * hash + (_title != null ? _title.hashCode() : 0);
		hash = 31 * hash + (_commandId != null ? _commandId.hashCode() : 0);
		hash = 31 * hash + (_status != null ? _status.hashCode() : 0);

		return hash;
	}

	public <T extends UpgradeStep> boolean isEqual(Collection<T> source, Collection<T> target) {
		if (source == null) {
			if (target == null) {
				return true;
			}

			return false;
		}

		if (target == null) {
			return false;
		}

		if (source.size() != target.size()) {
			return false;
		}

		if (ListUtil.isEmpty(source) && ListUtil.isEmpty(target)) {
			return true;
		}

		Stream<T> targetStream = target.stream();

		Collection<String> targetTitles = targetStream.map(
			element -> element.getTitle()
		).collect(
			Collectors.toList()
		);

		Stream<T> sourceStream = source.stream();

		return sourceStream.filter(
			element -> targetTitles.contains(element.getTitle())
		).findAny(
		).isPresent();
	}

	public boolean isEqualIgnoreCase(String original, String target) {
		if (original != null) {
			return original.equalsIgnoreCase(target);
		}

		if (target == null) {
			return true;
		}

		return false;
	}

	public void setStatus(UpgradeStepStatus status) {
		UpgradeStepStatusChangedEvent upgradeStepStatusChangedEvent = new UpgradeStepStatusChangedEvent(
			this, _status, status);

		_status = status;

		_upgradePlanner.dispatch(upgradeStepStatusChangedEvent);
	}

	private boolean _isRequiredIncompleted(UpgradeStep upgradeStep) {
		if (UpgradeStepRequirement.REQUIRED.equals(upgradeStep.getRequirement()) && !upgradeStep.completed()) {
			return true;
		}

		return false;
	}

	private List<UpgradeStep> _children = new ArrayList<>();
	private String _commandId;
	private String _description;
	private String _icon;
	private UpgradeStep _parent;
	private String _requirement;
	private UpgradeStepStatus _status = UpgradeStepStatus.INCOMPLETE;
	private String _title;
	private UpgradePlanner _upgradePlanner;
	private String _url;

}