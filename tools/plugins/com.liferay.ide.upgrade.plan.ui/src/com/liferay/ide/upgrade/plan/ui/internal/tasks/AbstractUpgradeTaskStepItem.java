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
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStep;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionStatus;
import com.liferay.ide.upgrade.plan.core.util.ServicesLookup;
import com.liferay.ide.upgrade.plan.ui.Disposable;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Simon Jiang
 */
public abstract class AbstractUpgradeTaskStepItem implements IExpansionListener, UpgradeTaskItem {

	public AbstractUpgradeTaskStepItem(
		UpgradeTaskViewer taskViewer, ScrolledForm scrolledForm, UpgradeTaskStepAction upgradeTaskStepAction,
		boolean expansionState) {

		viewer = taskViewer;
		formToolkit = new LiferayFormToolkit(scrolledForm.getDisplay());
		_scrolledForm = scrolledForm;
		stepAction = upgradeTaskStepAction;

		Bundle bundle = FrameworkUtil.getBundle(upgradeTaskStepAction.getClass());

		UpgradeTaskStep taskStepService = ServicesLookup.getSingleService(
			UpgradeTaskStep.class, "(id=" + upgradeTaskStepAction.getStepId() + ")");

		if ((taskStepService != null) && CoreUtil.isNotNullOrEmpty(taskStepService.getImagePath())) {
			URL entry = bundle.getEntry(taskStepService.getImagePath());

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

		UpgradeTaskStepActionStatus stepStatus = upgradeTaskStepAction.getStatus();

		if (stepStatus.equals(UpgradeTaskStepActionStatus.COMPLETED)) {
			_stepActionImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_ACTION_COMPLETE_IMAGE);
		}
		else if (stepStatus.equals(UpgradeTaskStepActionStatus.SKIPPED)) {
			_stepActionImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_ACTION_SKIP_IMAGE);
		}
		else {
			_stepActionImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_ACTION_BLANK_IMAGE);
		}

		final Composite itemComposite = _createComposite(
			formToolkit, _scrolledForm.getBody(), 2, 1, GridData.FILL_HORIZONTAL);

		Section section = formToolkit.createLiferaySection(
			_scrolledForm, itemComposite, formToolkit, upgradeTaskStepAction.getTitle(), _stepActionImage,
			Section.TITLE_BAR | Section.TWISTIE);

		section.setExpanded(expansionState);

		section.addExpansionListener(this);

		GridLayoutFactory gridLayoutFactory = GridLayoutFactory.fillDefaults();

		gridLayoutFactory.margins(0, 0);

		section.setLayout(gridLayoutFactory.create());

		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();

		gridDataFactory.grab(true, false);

		section.setLayoutData(gridDataFactory.create());

		section.setText(upgradeTaskStepAction.getTitle());

		section.addExpansionListener(this);

		Composite bodyComposite = formToolkit.createComposite(section);

		section.setClient(bodyComposite);

		disposables.add(() -> section.dispose());

		bodyComposite.setLayout(new TableWrapLayout());

		bodyComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		disposables.add(() -> bodyComposite.dispose());

		Label label = formToolkit.createLabel(bodyComposite, stepAction.getDescription());

		disposables.add(() -> label.dispose());

		if (stepAction == null) {
			return;
		}

		contentComposite = formToolkit.createComposite(bodyComposite);

		GridLayout buttonGridLayout = new GridLayout(2, false);

		buttonGridLayout.marginHeight = 2;
		buttonGridLayout.marginWidth = 2;
		buttonGridLayout.verticalSpacing = 2;

		contentComposite.setLayout(buttonGridLayout);

		contentComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));

		disposables.add(() -> contentComposite.dispose());

		addStepActions();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void dispose() {
		for (Disposable disposable : disposables) {
			try {
				disposable.dispose();
			}
			catch (Throwable t) {
			}
		}
	}

	@Override
	public void expansionStateChanged(ExpansionEvent expansionEvent) {
		ISelection selection = new StructuredSelection(stepAction);

		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, selection);

		_listeners.forEach(
			selectionChangedListener -> {
				selectionChangedListener.selectionChanged(selectionChangedEvent);
			});

		_scrolledForm.reflow(true);
	}

	@Override
	public void expansionStateChanging(ExpansionEvent expansionEvent) {
		viewer.setCurrentTaskStepAction(stepAction);
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

	protected abstract void addStepActions();

	protected Composite contentComposite;
	protected List<Disposable> disposables = new ArrayList<>();
	protected LiferayFormToolkit formToolkit;
	protected UpgradeTaskStepAction stepAction;
	protected UpgradeTaskViewer viewer;

	protected class LiferayFormToolkit extends FormToolkit {

		public LiferayFormToolkit(Display display) {
			super(display);
		}

		public LiferayFormToolkit(FormToolkit formToolkit) {
			super(formToolkit.getColors());
		}

		public Section createLiferaySection(
			final ScrolledForm form, Composite itemComposite, FormToolkit toolkit, String title, Image image,
			int style) {

			Label label1 = new Label(itemComposite, SWT.TOP);

			label1.setImage(image);

			final GridData tableData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);

			label1.setLayoutData(tableData);

			Section section = toolkit.createSection(itemComposite, style);

			section.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			section.setExpanded(false);
			section.addExpansionListener(
				new ExpansionAdapter() {

					public void expansionStateChanged(ExpansionEvent e) {
						form.reflow(true);
					}

				});

			return section;
		}

	}

	private Composite _createComposite(FormToolkit formToolkit, Composite parent, int columns, int hspan, int fill) {
		Composite g = formToolkit.createComposite(parent, SWT.NONE);

		g.setLayout(new GridLayout(columns, false));
		g.setFont(parent.getFont());

		GridData gd = new GridData(fill);

		gd.horizontalSpan = hspan;
		gd.verticalSpan = 1;
		g.setLayoutData(gd);

		return g;
	}

	private ListenerList<ISelectionChangedListener> _listeners = new ListenerList<>();
	private ScrolledForm _scrolledForm;
	private Image _stepActionImage;

}