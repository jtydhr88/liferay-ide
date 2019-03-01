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

import com.liferay.ide.upgrade.plan.core.UpgradePlanner;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStep;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionStatus;
import com.liferay.ide.upgrade.plan.core.util.ServicesLookup;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradeTaskStepIntroItem extends AbstractUpgradeTaskStepItem {

	public UpgradeTaskStepIntroItem(
		UpgradeTaskViewer taskViewer, ScrolledForm scrolledForm, UpgradeTaskStep upgradeTaskStep,
		boolean expansionState) {

		super(taskViewer, scrolledForm, new UpgradeGradeIntroStepAction(upgradeTaskStep), expansionState);

		_upgradeTaskStep = upgradeTaskStep;
	}

	@Override
	protected void addStepActions() {
		Image taskStepRestartImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_RESTART_IMAGE);

		ImageHyperlink taskStepRestartImageHyperlink = createImageHyperlink(
			formToolkit, contentComposite, taskStepRestartImage, this, "Click to restart");

		taskStepRestartImageHyperlink.setEnabled(true);

		taskStepRestartImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		taskStepRestartImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					new Job(stepAction.getTitle() + " restarting.") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							return _restartStep();
						}

					}.schedule();
				}

			});

		disposables.add(() -> taskStepRestartImageHyperlink.dispose());
	}

	private IStatus _restartStep() {
		UpgradePlanner upgradePlanner = ServicesLookup.getSingleService(UpgradePlanner.class, null);

		upgradePlanner.restartStep(_upgradeTaskStep);

		return Status.OK_STATUS;
	}

	private UpgradeTaskStep _upgradeTaskStep;

	private static class UpgradeGradeIntroStepAction implements UpgradeTaskStepAction {

		public UpgradeGradeIntroStepAction(UpgradeTaskStep upgradeTaskStep) {
			_upgradeTaskStep = upgradeTaskStep;
		}

		@Override
		public String getDescription() {
			return _upgradeTaskStep.getDescription();
		}

		@Override
		public String getId() {
			return _upgradeTaskStep.getId();
		}

		@Override
		public String getImagePath() {
			return _upgradeTaskStep.getImagePath();
		}

		@Override
		public double getOrder() {
			return 0;
		}

		@Override
		public UpgradeTaskStepActionStatus getStatus() {
			return UpgradeTaskStepActionStatus.COMPLETED;
		}

		@Override
		public String getStepId() {
			return _upgradeTaskStep.getId();
		}

		@Override
		public String getTitle() {
			return "Introduction";
		}

		@Override
		public IStatus perform(IProgressMonitor progressMonitor) {
			return Status.OK_STATUS;
		}

		@Override
		public void setStatus(UpgradeTaskStepActionStatus status) {
		}

		private UpgradeTaskStep _upgradeTaskStep;

	}

}