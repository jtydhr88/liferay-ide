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

import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionStatus;
import com.liferay.ide.upgrade.plan.ui.Disposable;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 */
public class UpgradeTaskStepActionItem implements IExpansionListener, UpgradeTaskItem {

	public UpgradeTaskStepActionItem(
		FormToolkit formToolkit, ScrolledForm scrolledForm, UpgradeTaskStepAction upgradeTaskStepAction) {

		_formToolkit = formToolkit;
		_scrolledForm = scrolledForm;
		_upgradeTaskStepAction = upgradeTaskStepAction;

		Composite parentComposite = _formToolkit.createComposite(_scrolledForm.getBody());

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();

		gridDataFactory.grab(true, true);

		parentComposite.setLayoutData(gridDataFactory.create());

		_disposables.add(() -> parentComposite.dispose());

		parentComposite.setLayout(new TableWrapLayout());

		Label label = _formToolkit.createLabel(parentComposite, _upgradeTaskStepAction.getDescription());

		_disposables.add(() -> label.dispose());

		if (_upgradeTaskStepAction == null) {
			return;
		}

		_buttonComposite = _formToolkit.createComposite(parentComposite);

		GridLayout buttonGridLayout = new GridLayout(2, false);

		buttonGridLayout.marginHeight = 2;
		buttonGridLayout.marginWidth = 2;
		buttonGridLayout.verticalSpacing = 2;

		_buttonComposite.setLayout(buttonGridLayout);

		_buttonComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		_disposables.add(() -> _buttonComposite.dispose());

		Image taskStepActionPerformImage = UpgradePlanUIPlugin.getImage(
			UpgradePlanUIPlugin.TASK_STEP_ACTION_PERFORM_IMAGE);

		ImageHyperlink performImageHyperlink = createImageHyperlink(
			_formToolkit, _buttonComposite, taskStepActionPerformImage, this, "Click to perform");

		performImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		performImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					new Job("Performing " + _upgradeTaskStepAction.getTitle() + "...") {

						@Override
						protected IStatus run(IProgressMonitor progressMonitor) {
							return _perform(progressMonitor);
						}

					}.schedule();
				}

			});

		_disposables.add(() -> performImageHyperlink.dispose());

		Label fillLabel = _formToolkit.createLabel(_buttonComposite, null);

		GridData gridData = new GridData();

		gridData.widthHint = 16;

		fillLabel.setLayoutData(gridData);

		_disposables.add(() -> fillLabel.dispose());

		Image taskStepActionCompleteImage = UpgradePlanUIPlugin.getImage(
			UpgradePlanUIPlugin.TASK_STEP_ACTION_COMPLETE_IMAGE);

		ImageHyperlink completeImageHyperlink = createImageHyperlink(
			_formToolkit, _buttonComposite, taskStepActionCompleteImage, this, "Click when complete");

		completeImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		completeImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					new Job("Completing " + _upgradeTaskStepAction.getTitle() + "...") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							_complete();

							return Status.OK_STATUS;
						}

					}.schedule();
				}

			});

		Image taskStepActionSkipImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_ACTION_SKIP_IMAGE);

		ImageHyperlink skipImageHyperlink = createImageHyperlink(
			_formToolkit, _buttonComposite, taskStepActionSkipImage, this, "Skip");

		skipImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		skipImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					_skip();
				}

			});

		_disposables.add(() -> performImageHyperlink.dispose());

		if (!upgradeTaskStepAction.enabled() || upgradeTaskStepAction.completed(upgradeTaskStepAction)) {
			performImageHyperlink.setEnabled(false);
			completeImageHyperlink.setEnabled(false);
			skipImageHyperlink.setEnabled(false);
		}
		else {
			performImageHyperlink.setEnabled(true);
			completeImageHyperlink.setEnabled(true);
			skipImageHyperlink.setEnabled(true);
		}
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
	public void expansionStateChanged(ExpansionEvent expansionEvent) {
		ISelection selection = new StructuredSelection(_upgradeTaskStepAction);

		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);

		_listeners.forEach(
			selectionChangedListener -> {
				selectionChangedListener.selectionChanged(selectionChangedEvent);
			});

		_scrolledForm.reflow(true);
	}

	@Override
	public void expansionStateChanging(ExpansionEvent expansionEvent) {
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	private void _complete() {
		_upgradeTaskStepAction.setStatus(UpgradeTaskStepActionStatus.COMPLETED);
	}

	private IStatus _perform(IProgressMonitor progressMonitor) {
		return _upgradeTaskStepAction.perform(progressMonitor);
	}

	private void _skip() {
		_upgradeTaskStepAction.setStatus(UpgradeTaskStepActionStatus.SKIPPED);
	}

	private Composite _buttonComposite;
	private List<Disposable> _disposables = new ArrayList<>();
	private FormToolkit _formToolkit;
	private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();
	private ScrolledForm _scrolledForm;
	private final UpgradeTaskStepAction _upgradeTaskStepAction;

}