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

package com.liferay.ide.upgrade.plan.ui.internal;

import com.liferay.ide.ui.util.UIUtil;
import com.liferay.ide.upgrade.plan.core.UpgradeEvent;
import com.liferay.ide.upgrade.plan.core.UpgradeListener;
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanAcessor;
import com.liferay.ide.upgrade.plan.core.UpgradePlanStartedEvent;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeStep;
import com.liferay.ide.upgrade.plan.core.UpgradeStepStatus;
import com.liferay.ide.upgrade.plan.core.UpgradeStepStatusChangedEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradePlanViewer implements UpgradeListener, IDoubleClickListener, UpgradePlanAcessor {

	public UpgradePlanViewer(Composite parentComposite) {
		_treeViewer = new TreeViewer(parentComposite);

		_treeViewer.addDoubleClickListener(this);
		_treeViewer.setContentProvider(new UpgradePlanContentProvider());
		_treeViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new UpgradePlanLabelProvider()));

		_treeViewer.setInput(UpgradePlanContentProvider.NO_UPGRADE_PLAN_ACTIVE);

		Bundle bundle = FrameworkUtil.getBundle(UpgradePlanView.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_upgradePlannerServiceTracker = new ServiceTracker<>(bundleContext, UpgradePlanner.class, null);

		_upgradePlannerServiceTracker.open();

		UpgradePlanner upgradePlanner = _upgradePlannerServiceTracker.getService();

		upgradePlanner.addListener(this);
	}

	public void addPostSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		_treeViewer.addPostSelectionChangedListener(selectionChangedListener);
	}

	public void dispose() {
		UpgradePlanner upgradePlanner = _upgradePlannerServiceTracker.getService();

		upgradePlanner.removeListener(this);

		_upgradePlannerServiceTracker.close();
	}

	@Override
	public void doubleClick(DoubleClickEvent doubleClickEvent) {
		Optional<Object> selectOptional = Optional.of(
			doubleClickEvent.getSelection()
		).filter(
			selection -> selection instanceof IStructuredSelection
		).map(
			IStructuredSelection.class::cast
		).map(
			IStructuredSelection::getFirstElement
		);

		selectOptional.filter(
			item -> item instanceof UpgradeStep
		).ifPresent(
			s -> {
				_treeViewer.setExpandedState(s, !_treeViewer.getExpandedState(s));

				_treeViewer.refresh();
			}
		);

		selectOptional.filter(
			UpgradePlanContentProvider.NO_STEPS::equals
		).ifPresent(
			s -> {
				Viewer viewer = doubleClickEvent.getViewer();

				Control control = viewer.getControl();

				WizardDialog wizardDialog = new WizardDialog(control.getShell(), new NewUpgradePlanWizard());

				wizardDialog.open();
			}
		);
	}

	public Object getInput() {
		return _treeViewer.getInput();
	}

	public ISelection getSelection() {
		if (_treeViewer != null) {
			return _treeViewer.getSelection();
		}

		return null;
	}

	public Object[] getTreeExpansion() {
		return _treeViewer.getExpandedElements();
	}

	public TreeViewer getTreeViewer() {
		return _treeViewer;
	}

	public void initTreeExpansion(String[] stepIds) {
		Stream.of(
			stepIds
		).map(
			this::getStep
		).map(
			step -> Adapters.adapt(step, UpgradeStep.class)
		).filter(
			Objects::nonNull
		).forEach(
			step -> {
				_treeViewer.expandToLevel(step, 1, false);
			}
		);
	}

	@Override
	public void onUpgradeEvent(UpgradeEvent upgradeEvent) {
		if (upgradeEvent instanceof UpgradePlanStartedEvent) {
			UpgradePlanStartedEvent upgradePlanStartedEvent = (UpgradePlanStartedEvent)upgradeEvent;

			UpgradePlan upgradePlan = upgradePlanStartedEvent.getUpgradePlan();

			UIUtil.async(() -> _treeViewer.setInput(upgradePlan));
		}
		else if (upgradeEvent instanceof UpgradeStepStatusChangedEvent) {
			UIUtil.async(
				() -> {
					UpgradeStepStatusChangedEvent statusEvent = Adapters.adapt(
						upgradeEvent, UpgradeStepStatusChangedEvent.class);

					UpgradeStepStatus newStatus = statusEvent.getNewStatus();

					if (!newStatus.equals(UpgradeStepStatus.INCOMPLETE) &&
						!newStatus.equals(UpgradeStepStatus.FAILED)) {

						ISelection selection = _treeViewer.getSelection();

						_changeSelection(selection, true, false);
					}

					refresh();
				});
		}
	}

	public void refresh() {
		if (_treeViewer != null) {
			Object[] elements = _treeViewer.getExpandedElements();
			TreePath[] treePaths = _treeViewer.getExpandedTreePaths();
			_treeViewer.refresh(true);
			_treeViewer.setExpandedElements(elements);
			_treeViewer.setExpandedTreePaths(treePaths);
		}
	}

	private void _changeSelection(ISelection selection, boolean deepFind, boolean findFirstLeaf) {
		IStructuredSelection structureSelection = (IStructuredSelection)selection;

		Object selectedObject = structureSelection.getFirstElement();

		IContentProvider contentProvider = _treeViewer.getContentProvider();

		if (contentProvider == null) {
			return;
		}

		ITreeContentProvider treeContentProvider = Adapters.adapt(contentProvider, ITreeContentProvider.class);

		boolean hasChildren = treeContentProvider.hasChildren(selectedObject);

		UpgradeStep upgradeStep = Adapters.adapt(selectedObject, UpgradeStep.class);

		if (upgradeStep == null) {
			return;
		}

		double selectedOrder = upgradeStep.getOrder();

		if (deepFind && hasChildren) {
			Object[] childrenElements = treeContentProvider.getChildren(selectedObject);

			ISelection newSelection = new StructuredSelection(childrenElements[0]);

			_treeViewer.setSelection(newSelection);

			_changeSelection(newSelection, true, true);
		}

		Object parent = treeContentProvider.getParent(selectedObject);

		if (deepFind && hasChildren) {
			return;
		}

		if (deepFind && !hasChildren && (parent != null) && findFirstLeaf) {
			return;
		}

		if (!deepFind && (parent == null)) {
			Object input = _treeViewer.getInput();

			UpgradePlan upgradePlan = Adapters.adapt(input, UpgradePlan.class);

			List<UpgradeStep> upgradeTasks = upgradePlan.getRootSteps();

			Stream<UpgradeStep> stream = upgradeTasks.stream();

			stream.filter(
				element -> element.getOrder() > selectedOrder
			).findFirst(
			).ifPresent(
				element -> {
					ISelection newSelection = new StructuredSelection(element);

					_treeViewer.expandToLevel(element, 1);
					_treeViewer.setSelection(newSelection);

					_changeSelection(newSelection, true, true);
				}
			);
		}

		if (parent == null) {
			return;
		}

		Optional<UpgradeStep> optional = Stream.of(
			treeContentProvider.getChildren(parent)
		).map(
			childObject -> Adapters.adapt(childObject, UpgradeStep.class)
		).filter(
			element -> element.getOrder() > selectedOrder
		).findFirst();

		if (optional.isPresent()) {
			upgradeStep = optional.get();

			ISelection newSelection = new StructuredSelection(upgradeStep);

			_treeViewer.setSelection(newSelection);

			if (!deepFind) {
				_treeViewer.expandToLevel(upgradeStep, 1, true);

				_changeSelection(newSelection, true, true);
			}
		}
		else {
			_treeViewer.collapseToLevel(parent, 1);

			ISelection newSelection = new StructuredSelection(parent);

			_changeSelection(newSelection, false, true);
		}
	}

	private TreeViewer _treeViewer;
	private ServiceTracker<UpgradePlanner, UpgradePlanner> _upgradePlannerServiceTracker;

}