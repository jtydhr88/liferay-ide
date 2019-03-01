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

package com.liferay.ide.upgrade.plan.ui.internal.tasks;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElement;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStep;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.ui.Disposable;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanViewer;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradeTaskViewer implements ISelectionProvider {

	public UpgradeTaskViewer(Composite parentComposite, UpgradePlanViewer upgradePlanViewer) {
		_formToolkit = new FormToolkit(parentComposite.getDisplay());

		_scrolledForm = _formToolkit.createScrolledForm(parentComposite);

		_formToolkit.decorateFormHeading(_scrolledForm.getForm());

		Composite composite = _scrolledForm.getBody();

		GridLayoutFactory gridLayoutFactory = GridLayoutFactory.fillDefaults();

		composite.setLayout(gridLayoutFactory.create());

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();

		gridDataFactory.grab(true, true);

		composite.setLayoutData(gridDataFactory.create());

		_disposables.add(() -> _formToolkit.dispose());
		_disposables.add(() -> _scrolledForm.dispose());

		if (_currentSelection != upgradePlanViewer.getSelection()) {
			_currentTaskStepAction = null;
		}

		_currentSelection = upgradePlanViewer.getSelection();

		_updateFromSelection(_currentSelection);

		upgradePlanViewer.addPostSelectionChangedListener(
			selectionChangedEvent -> {
				ISelection selection = selectionChangedEvent.getSelection();

				if (_currentSelection != upgradePlanViewer.getSelection()) {
					_currentTaskStepAction = null;
				}

				_currentSelection = upgradePlanViewer.getSelection();

				_updateFromSelection(selection);
			});
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.add(listener);
	}

	public void dispose() {
		for (Disposable disposable : _disposables) {
			try {
				disposable.dispose();
			}
			catch (Throwable t) {
			}
		}
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	public void refreshSelection() {
		if (_currentSelection != null) {
			_updateFromSelection(_currentSelection);
		}
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.remove(listener);
	}

	public void setCurrentTaskStepAction(UpgradeTaskStepAction currentTaskStepAction) {
		_currentTaskStepAction = currentTaskStepAction;
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	private void _clearScrolledForm() {
		Composite bodyComposite = _scrolledForm.getBody();

		for (Control control : bodyComposite.getChildren()) {
			control.dispose();
		}
	}

	private void _fireSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
		_listeners.forEach(
			selectionChangedListener -> {
				try {
					selectionChangedListener.selectionChanged(selectionChangedEvent);
				}
				catch (Exception e) {
					UpgradePlanUIPlugin.logError("Error in selection changed listener.", e);
				}
			});
	}

	private UpgradePlanElement _getSelectedUpgradePlanElement(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection)selection;

			Object object = structuredSelection.getFirstElement();

			if (object instanceof UpgradePlanElement) {
				return (UpgradePlanElement)object;
			}
		}

		return null;
	}

	private void _updateFromSelection(ISelection selection) {
		_clearScrolledForm();

		UpgradePlanElement upgradePlanElement = _getSelectedUpgradePlanElement(selection);

		if (upgradePlanElement instanceof UpgradeTaskStep) {
			UpgradeTaskStep upgradeTaskStep = (UpgradeTaskStep)upgradePlanElement;

			_scrolledForm.setText(upgradeTaskStep.getTitle());

			_updateTaskStepItems(upgradeTaskStep);
		}
		else if (upgradePlanElement != null) {
			_scrolledForm.setText(upgradePlanElement.getTitle());

			Bundle bundle = FrameworkUtil.getBundle(upgradePlanElement.getClass());

			if ((upgradePlanElement != null) && CoreUtil.isNotNullOrEmpty(upgradePlanElement.getImagePath())) {
				URL entry = bundle.getEntry(upgradePlanElement.getImagePath());

				if (entry != null) {
					ImageDescriptor imgDescriptor = ImageDescriptor.createFromURL(entry);

					_scrolledForm.setImage(imgDescriptor.createImage());
				}
				else {
					_scrolledForm.setImage(null);
				}
			}
			else {
				_scrolledForm.setImage(null);
			}

			_formToolkit.createLabel(_scrolledForm.getBody(), upgradePlanElement.getDescription());
		}

		_scrolledForm.reflow(true);
	}

	private void _updateTaskStepItems(UpgradeTaskStep upgradeTaskStep) {
		UpgradeTaskStepIntroItem upgradeTaskStepIntroItem = new UpgradeTaskStepIntroItem(
			this, _scrolledForm, upgradeTaskStep, _currentTaskStepAction != null ? false : true);

		upgradeTaskStepIntroItem.addSelectionChangedListener(this::_fireSelectionChanged);

		for (UpgradeTaskStepAction upgradeTaskStepAction : upgradeTaskStep.getActions()) {
			boolean expanded = false;

			if ((_currentTaskStepAction != null) &&
				(upgradeTaskStepAction.getOrder() > _currentTaskStepAction.getOrder())) {

				expanded = true;
			}

			UpgradeTaskStepActionItem upgradeTaskStepActionItem = new UpgradeTaskStepActionItem(
				this, _scrolledForm, upgradeTaskStepAction, expanded);

			upgradeTaskStepActionItem.addSelectionChangedListener(this::_fireSelectionChanged);
		}
	}

	private ISelection _currentSelection;
	private UpgradeTaskStepAction _currentTaskStepAction;
	private List<Disposable> _disposables = new ArrayList<>();
	private FormToolkit _formToolkit;
	private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();
	private ScrolledForm _scrolledForm;

}