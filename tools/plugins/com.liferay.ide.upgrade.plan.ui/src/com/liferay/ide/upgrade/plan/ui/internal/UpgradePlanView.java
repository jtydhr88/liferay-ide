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
import com.liferay.ide.upgrade.plan.core.UpgradePlan;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElement;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElementStatus;
import com.liferay.ide.upgrade.plan.core.UpgradePlanElementStatusChangedEvent;
import com.liferay.ide.upgrade.plan.core.UpgradePlanStartedEvent;
import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeTask;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionPerformedEvent;
import com.liferay.ide.upgrade.plan.ui.internal.tasks.UpgradePlanElementViewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradePlanView extends ViewPart implements ISelectionProvider {

	public static final String ID = "com.liferay.ide.upgrade.plan.view";

	public UpgradePlanView() {
		Bundle bundle = FrameworkUtil.getBundle(UpgradePlanView.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_upgradePlannerServiceTracker = new ServiceTracker<>(bundleContext, UpgradePlanner.class, null);

		_upgradePlannerServiceTracker.open();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void createPartControl(Composite parentComposite) {
		_createPartControl(parentComposite);

		IViewSite viewSite = getViewSite();

		viewSite.setSelectionProvider(this);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (_upgradePlanViewer != null) {
			_upgradePlanViewer.dispose();
		}

		if (_upgradePlanElementViewer != null) {
			_upgradePlanElementViewer.dispose();
		}

		_upgradePlannerServiceTracker.close();
	}

	@Override
	public ISelection getSelection() {
		return _upgradePlanElementViewer.getSelection();
	}

	public UpgradePlanViewer getUpgradePlanViewer() {
		return _upgradePlanViewer;
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		_memento = memento;

		UpgradePlanner upgradePlanner = _upgradePlannerServiceTracker.getService();

		Optional.ofNullable(
			memento
		).map(
			m -> m.getString("activeUpgradePlanName")
		).filter(
			Objects::nonNull
		).ifPresent(
			upgradePlanName -> {
				UpgradePlan upgradePlan = upgradePlanner.loadUpgradePlan(upgradePlanName);

				upgradePlanner.startUpgradePlan(upgradePlan);
			}
		);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);

		Object upgradePlanViewerInput = _upgradePlanViewer.getInput();

		if (upgradePlanViewerInput instanceof UpgradePlan) {
			UpgradePlan upgradePlan = (UpgradePlan)upgradePlanViewerInput;

			UpgradePlanner upgradePlanner = _upgradePlannerServiceTracker.getService();

			_saveTreeExpansion(memento, _upgradePlanViewer.getTreeExpansion());

			upgradePlanner.saveUpgradePlan(upgradePlan);

			memento.putString("activeUpgradePlanName", upgradePlan.getName());
		}
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void setSelection(ISelection selection) {
		_upgradePlanElementViewer.setSelection(selection);
	}

	private void _changeSelection(ISelection selection, boolean deepFind, boolean findFirstLeaf) {
		if (_upgradePlanViewer != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structureSelection = (IStructuredSelection)selection;

				Object selectedObject = structureSelection.getFirstElement();

				if (selectedObject == null) {
					return;
				}

				TreeViewer treeViewer = _upgradePlanViewer.getTreeViewer();

				IContentProvider contentProvider = treeViewer.getContentProvider();

				ITreeContentProvider upgradePlanTreeContentProvider = Adapters.adapt(
					contentProvider, ITreeContentProvider.class);

				Object parentObject = upgradePlanTreeContentProvider.getParent(selectedObject);

				UpgradePlanElement selectedElement = Adapters.adapt(selectedObject, UpgradePlanElement.class);

				if (deepFind) {
					if (!upgradePlanTreeContentProvider.hasChildren(selectedObject)) {
						if (parentObject != null) {
							Object[] childrenElement = upgradePlanTreeContentProvider.getChildren(parentObject);

							boolean lastOne = true;

							for (Object childObject : childrenElement) {
								UpgradePlanElement childElement = Adapters.adapt(childObject, UpgradePlanElement.class);

								if (findFirstLeaf) {
									return;
								}

								if (childElement.getOrder() > selectedElement.getOrder()) {
									selectedElement = childElement;
									lastOne = false;

									break;
								}
							}

							ISelection nextSelection = null;

							if (lastOne) {
								treeViewer.collapseToLevel(parentObject, 1);
								nextSelection = new StructuredSelection(parentObject);

								_changeSelection(nextSelection, false, true);
							}
							else {
								nextSelection = new StructuredSelection(selectedElement);

								treeViewer.setSelection(nextSelection);
							}
						}
						else {
							Object treeInput = _upgradePlanViewer.getInput();

							UpgradePlan upgradePlan = Adapters.adapt(treeInput, UpgradePlan.class);

							List<UpgradeTask> upgradePlanTasks = upgradePlan.getTasks();

							UpgradePlanElement nextSelectedElement = null;

							for (UpgradePlanElement planElement : upgradePlanTasks) {
								if (planElement.getOrder() > selectedElement.getOrder()) {
									nextSelectedElement = planElement;

									break;
								}
							}

							if (nextSelectedElement != null) {
								ISelection newSelection = new StructuredSelection(nextSelectedElement);
								treeViewer.expandToLevel(nextSelectedElement, 1);
								treeViewer.setSelection(newSelection);
								_changeSelection(newSelection, true, true);
							}
						}
					}
					else {
						Object[] childrenElements = upgradePlanTreeContentProvider.getChildren(selectedObject);

						if ((childrenElements != null) && (childrenElements.length > 0)) {
							ISelection newSelection = new StructuredSelection(childrenElements[0]);

							treeViewer.setSelection(newSelection);
							_changeSelection(newSelection, true, true);
						}
					}
				}
				else {
					if (parentObject != null) {
						Object[] childrenObjects = upgradePlanTreeContentProvider.getChildren(parentObject);
						boolean lastOne = true;

						for (Object childObject : childrenObjects) {
							UpgradePlanElement childElement = Adapters.adapt(childObject, UpgradePlanElement.class);

							if (childElement.getOrder() > selectedElement.getOrder()) {
								lastOne = false;
								selectedElement = childElement;

								break;
							}
						}

						ISelection nextSelection = null;

						if (lastOne) {
							treeViewer.collapseToLevel(parentObject, 1);
							nextSelection = new StructuredSelection(parentObject);

							_changeSelection(nextSelection, false, true);
						}
						else {
							nextSelection = new StructuredSelection(selectedElement);

							treeViewer.setSelection(nextSelection);

							treeViewer.expandToLevel(selectedElement, 1, true);
							_changeSelection(nextSelection, true, true);
						}
					}
					else {
						Object viewInput = _upgradePlanViewer.getInput();

						UpgradePlan upgradePlan = Adapters.adapt(viewInput, UpgradePlan.class);

						List<UpgradeTask> tasks = upgradePlan.getTasks();

						UpgradePlanElement nextSelectElement = null;

						for (UpgradePlanElement element : tasks) {
							if (element.getOrder() > selectedElement.getOrder()) {
								nextSelectElement = element;

								break;
							}
						}

						if (nextSelectElement != null) {
							ISelection newSelection = new StructuredSelection(nextSelectElement);
							treeViewer.expandToLevel(nextSelectElement, 1);
							treeViewer.setSelection(newSelection);
							_changeSelection(newSelection, true, true);
						}
					}
				}
			}
		}
	}

	private void _createPartControl(Composite parentComposite) {
		parentComposite.setLayout(new FillLayout());

		SashForm sashForm = new SashForm(parentComposite, SWT.HORIZONTAL);

		_upgradePlanViewer = new UpgradePlanViewer(sashForm);

		_upgradePlanViewer.addPostSelectionChangedListener(this::_fireSelectionChanged);

		UpgradePlanner upgradePlanner = _upgradePlannerServiceTracker.getService();

		upgradePlanner.addListener(
			upgradeEvent -> {
				if (upgradeEvent instanceof UpgradePlanStartedEvent) {
					UpgradePlanStartedEvent upgradePlanStartedEvent = (UpgradePlanStartedEvent)upgradeEvent;

					UpgradePlan upgradePlan = upgradePlanStartedEvent.getUpgradePlan();

					if (upgradePlan != null) {
						UIUtil.async(
							() -> {
								setContentDescription("Active upgrade plan: " + upgradePlan.getName());

								if (_memento != null) {
									_upgradePlanViewer.initTreeExpansion(_loadTreeExpansion());
								}
							});
					}
				}
			});

		upgradePlanner.addListener(
			upgradeEvent -> {
				if (upgradeEvent instanceof UpgradeTaskStepActionPerformedEvent) {
					UIUtil.refreshCommonView("org.eclipse.ui.navigator.ProjectExplorer");
				}
				else if (upgradeEvent instanceof UpgradePlanElementStatusChangedEvent) {
					UIUtil.sync(
						() -> {
							if (_upgradePlanViewer != null) {
								UpgradePlanElementStatusChangedEvent statusEvent = Adapters.adapt(
									upgradeEvent, UpgradePlanElementStatusChangedEvent.class);

								UpgradePlanElementStatus newUpgradePlanElementStatus = statusEvent.getNewStatus();

								if (!newUpgradePlanElementStatus.equals(UpgradePlanElementStatus.INCOMPLETE)) {
									_changeSelection(_upgradePlanViewer.getSelection(), true, false);
								}
							}

							_upgradePlanViewer.refresh();
						});
				}
			});

		_upgradePlanElementViewer = new UpgradePlanElementViewer(sashForm, _upgradePlanViewer);

		_upgradePlanElementViewer.addSelectionChangedListener(this::_fireSelectionChanged);

		setContentDescription(
			"No active upgrade plan. Use view menu 'New Upgrade Plan' action to start a new upgrade.");
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

	private Map<String, Set<String>> _loadTreeExpansion() {
		Map<String, Set<String>> treeExpansionMap = new HashMap<>();

		IMemento upgradePlanTreeExpansionMemento = _memento.getChild("upgradePlanTreeExpansion");

		if (upgradePlanTreeExpansionMemento != null) {
			IMemento tasksMemento = upgradePlanTreeExpansionMemento.getChild("tasks");

			if (tasksMemento != null) {
				IMemento[] taskMementos = tasksMemento.getChildren("task");

				Set<String> taskExpansionSet = new HashSet<>();

				for (IMemento taskMemento : taskMementos) {
					taskExpansionSet.add(taskMemento.getString("id"));
				}

				treeExpansionMap.put("task", taskExpansionSet);
			}

			IMemento stepsMemento = upgradePlanTreeExpansionMemento.getChild("steps");

			if (stepsMemento != null) {
				IMemento[] stepMementos = stepsMemento.getChildren("step");

				Set<String> stepExpansionSet = new HashSet<>();

				for (IMemento stepMemento : stepMementos) {
					stepExpansionSet.add(stepMemento.getString("id"));
				}

				treeExpansionMap.put("step", stepExpansionSet);
			}
		}

		return treeExpansionMap;
	}

	private void _saveTreeExpansion(IMemento memento, Map<String, Set<String>> expansionMap) {
		IMemento upgradePlanTreeExpansionMemento = memento.createChild("upgradePlanTreeExpansion");

		IMemento tasksExpansionMemento = upgradePlanTreeExpansionMemento.createChild("tasks");

		Set<String> taskExpansions = expansionMap.get("task");

		for (String taskId : taskExpansions) {
			IMemento taskExpansionMemento = tasksExpansionMemento.createChild("task");

			taskExpansionMemento.putString("id", taskId);
		}

		IMemento stepsExpansionMemento = upgradePlanTreeExpansionMemento.createChild("steps");

		Set<String> stepExpansions = expansionMap.get("step");

		for (String stepId : stepExpansions) {
			IMemento stepExpansionMemento = stepsExpansionMemento.createChild("step");

			stepExpansionMemento.putString("id", stepId);
		}
	}

	private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();
	private IMemento _memento;
	private UpgradePlanElementViewer _upgradePlanElementViewer;
	private ServiceTracker<UpgradePlanner, UpgradePlanner> _upgradePlannerServiceTracker;
	private UpgradePlanViewer _upgradePlanViewer;

}