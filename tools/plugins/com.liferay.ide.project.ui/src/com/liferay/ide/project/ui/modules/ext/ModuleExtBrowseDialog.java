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

package com.liferay.ide.project.ui.modules.ext;

import com.liferay.ide.core.Artifact;
import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.modules.ext.NewModuleExtOp;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.ui.util.UIUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.AbstractElementListSelectionDialog;

/**
 * @author Charles Wu
 */
public class ModuleExtBrowseDialog extends AbstractElementListSelectionDialog {

	public ModuleExtBrowseDialog(Shell parent, Value<?> property) {
		super(parent, new ColumnLabelProvider());

		_property = property;

		ValueProperty definition = _property.definition();

		setHelpAvailable(false);
		setTitle(definition.getLabel(false, CapitalizationType.TITLE_STYLE, false));
	}

	@Override
	public boolean close() {

		// must be reset since the shell will changed

		_job = null;

		return super.close();
	}

	@Override
	protected void computeResult() {
		setResult(Arrays.asList(getSelectedElements()));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite contents = (Composite)super.createDialogArea(parent);

		_customLabel = _createCustomMessage(contents);

		// make the private label disappear

		createMessageArea(contents).setLayoutData(new GridData(0, 0));

		_filterText = createFilterText(contents);
		_createRefreshButtonArea(contents);
		createFilteredList(contents);

		Object[] elements = _getElements(false);

		setListElements(elements);

		if (elements.length == 0) {
			_customLabel.setText("Please click refresh button to get list available");
		}

		setSelection(getInitialElementSelections().toArray());

		return contents;
	}

	private Label _createCustomMessage(Composite composite) {
		Label label = new Label(composite, SWT.NONE);

		label.setText(_defaultMessage);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		return label;
	}

	private Button _createRefreshButtonArea(Composite composite) {
		Composite container = new Composite(composite, SWT.NONE);
		GridData data = new GridData();

		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		container.setLayoutData(data);

		container.setLayout(new GridLayout(5, false));

		_filterText.setParent(container);
		_filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1));

		Button refreshButon = new Button(container, SWT.PUSH);

		refreshButon.setText("Refresh");
		refreshButon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		refreshButon.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> _refreshAction(composite)));

		return refreshButon;
	}

	private Object[] _getElements(boolean reload) {
		IWorkspaceProject gradleWorkspaceProject = LiferayWorkspaceUtil.getGradleWorkspaceProject();

		List<Artifact> artifacts = gradleWorkspaceProject.getTargetPlatformArtifacts(reload);

		Stream<Artifact> stream = artifacts.stream();

		return stream.filter(
			artifact -> "com.liferay".equals(artifact.getGroup())
		).toArray();
	}

	private void _refreshAction(Composite composite) {
		NewModuleExtOp op = _property.nearest(NewModuleExtOp.class);

		if (SapphireUtil.getContent(op.getTargetPlatformVersion()) == null) {
			_customLabel.setText("No Target Plarform configuration detected in gradle.properties");

			return;
		}
		else {
			if (_job == null) {
				_job = new Job("Reading target platform configuration") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						Object[] elements = _getElements(true);

						UIUtil.async(
							() -> {
								if (!composite.isDisposed()) {
									setListElements(elements);
								}
							});

						return Status.OK_STATUS;
					}

				};

				_job.addJobChangeListener(
					new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							UIUtil.async(
								() -> {
									if (!composite.isDisposed()) {
										_customLabel.setText(_defaultMessage);
										_filterText.setEnabled(true);
										composite.setCursor(null);
									}
								});
						}

						@Override
						public void running(IJobChangeEvent event) {
							UIUtil.async(
								() -> {
									if (!composite.isDisposed()) {
										_customLabel.setText("Refreshing bundle list");
										composite.setCursor(new Cursor(null, SWT.CURSOR_WAIT));
									}
								});
						}

					});
			}
			else if (_job.getState() == Job.RUNNING) {
				return;
			}

			_job.schedule();
		}
	}

	private static Job _job;

	private Label _customLabel;
	private String _defaultMessage = "Select Original Module Names";
	private Text _filterText;
	private Value<?> _property;

}