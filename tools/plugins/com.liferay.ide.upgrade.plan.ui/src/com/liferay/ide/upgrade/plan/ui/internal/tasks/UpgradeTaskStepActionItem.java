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

import com.liferay.ide.ui.util.UIUtil;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepAction;
import com.liferay.ide.upgrade.plan.core.UpgradeTaskStepActionStatus;
import com.liferay.ide.upgrade.plan.ui.internal.UpgradePlanUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Terry Jia
 * @author Gregory Amerson
 * @author Simon Jiang
 */
public class UpgradeTaskStepActionItem extends AbstractUpgradeTaskStepItem {

	public UpgradeTaskStepActionItem(
		UpgradeTaskViewer taskViewer, ScrolledForm scrolledForm, UpgradeTaskStepAction upgradeTaskStepAction,
		boolean expansionState) {

		super(taskViewer, scrolledForm, upgradeTaskStepAction, expansionState);
	}

	@Override
	protected void addStepActions() {
		Image taskStepActionPerformImage = UpgradePlanUIPlugin.getImage(
			UpgradePlanUIPlugin.TASK_STEP_ACTION_PERFORM_IMAGE);

		ImageHyperlink performImageHyperlink = createImageHyperlink(
			formToolkit, contentComposite, taskStepActionPerformImage, this, "Click to perform");

		performImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		performImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					new Job("Performing " + stepAction.getTitle() + "...") {

						@Override
						protected IStatus run(IProgressMonitor progressMonitor) {
							return stepAction.perform(progressMonitor);
						}

					}.schedule();
				}

			});

		disposables.add(() -> performImageHyperlink.dispose());

		Label fillLabel = formToolkit.createLabel(contentComposite, null);

		GridData gridData = new GridData();

		gridData.widthHint = 16;

		fillLabel.setLayoutData(gridData);

		disposables.add(() -> fillLabel.dispose());

		Image taskStepActionCompleteImage = UpgradePlanUIPlugin.getImage(
			UpgradePlanUIPlugin.TASK_STEP_ACTION_COMPLETE_IMAGE);

		ImageHyperlink completeImageHyperlink = createImageHyperlink(
			formToolkit, contentComposite, taskStepActionCompleteImage, this, "Click when complete");

		completeImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		completeImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					new Job("Completing " + stepAction.getTitle() + "...") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							stepAction.setStatus(UpgradeTaskStepActionStatus.COMPLETED);

							UIUtil.async(
								() -> {
									viewer.setCurrentTaskStepAction(stepAction);
									viewer.refreshSelection();
								});

							return Status.OK_STATUS;
						}

					}.schedule();
				}

			});

		Image taskStepActionSkipImage = UpgradePlanUIPlugin.getImage(UpgradePlanUIPlugin.TASK_STEP_ACTION_SKIP_IMAGE);

		ImageHyperlink skipImageHyperlink = createImageHyperlink(
			formToolkit, contentComposite, taskStepActionSkipImage, this, "Skip");

		skipImageHyperlink.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		skipImageHyperlink.addHyperlinkListener(
			new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					stepAction.setStatus(UpgradeTaskStepActionStatus.SKIPPED);

					UIUtil.async(
						() -> {
							viewer.setCurrentTaskStepAction(stepAction);
							viewer.refreshSelection();
						});
				}

			});

		disposables.add(() -> performImageHyperlink.dispose());
	}

}