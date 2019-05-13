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

package com.liferay.ide.upgrade.plugins.ui.internal.customjsps;

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.ui.util.SWTUtil;
import com.liferay.ide.upgrade.plan.ui.util.UIUtil;

import java.io.File;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Andy Wu
 * @author Terry Jia
 */
public class CustomJSPCompareView extends ViewPart implements CustomJSPSupport {

	public static final String ID = "com.liferay.ide.upgrade.plugins.ui.views.ConvertJspView";

	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.H_SCROLL);

		GridLayout sashLayout = new GridLayout(1, false);

		sashLayout.marginHeight = 0;
		sashLayout.marginWidth = 0;

		sashForm.setLayout(sashLayout);

		GridData sashFormLayoutData = new GridData(GridData.FILL_BOTH);

		sashForm.setLayoutData(sashFormLayoutData);

		_createTreePart62(sashForm);
		_createTreePart7x(sashForm);
	}

	@Override
	public void setFocus() {
	}

	private void _compare(File originalFilePath, File changedFilePath, String leftLabel, String rightLabel) {
		CompareConfiguration config = new CompareConfiguration();

		config.setLeftEditable(false);
		config.setLeftLabel(leftLabel);

		config.setRightEditable(false);
		config.setRightLabel(rightLabel);

		CompareEditorInput editorInput = new CompareEditorInput(config) {

			@Override
			protected Object prepareInput(IProgressMonitor monitor)
				throws InterruptedException, InvocationTargetException {

				return new DiffNode(_originalItem, _changedItem);
			}

			private CompareItem _changedItem = new CompareItem(changedFilePath);
			private CompareItem _originalItem = new CompareItem(originalFilePath);

		};

		editorInput.setTitle("Compare ('" + originalFilePath + "'-'" + changedFilePath + "')");

		CompareUI.openCompareEditor(editorInput);
	}

	private void _createTreePart7x(Composite parent) {
		ScrolledComposite container7x = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		Composite part7x = SWTUtil.createComposite(container7x, 1, 1, GridData.FILL_BOTH, 0, 0);

		FillLayout layout = new FillLayout();

		layout.marginHeight = 0;
		layout.marginWidth = 0;

		container7x.setLayout(layout);

		container7x.setMinSize(410, 200);
		container7x.setExpandHorizontal(true);
		container7x.setExpandVertical(true);
		container7x.setContent(part7x);

		Label label7x = new Label(part7x, SWT.NONE);

		label7x.setText("New JSP (double-click to compare 6.2 with 7.x)");

		_treeViewer7x = new TreeViewer(part7x, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

		Tree tree = _treeViewer7x.getTree();

		tree.setLayoutData(gridData);

		_treeViewer7x.setContentProvider(new CustomJSPCompareViewContentProvider());
		_treeViewer7x.setLabelProvider(new CustomJSPCompareViewLabelProvider7x());
		_treeViewer7x.addDoubleClickListener(new DoubleClickExpandListener(_treeViewer7x));

		_treeViewer7x.addDoubleClickListener(
			event -> {
				ISelection selection = event.getSelection();

				File file = (File)((ITreeSelection)selection).getFirstElement();

				if (file.isDirectory()) {
					return;
				}

				String[] paths = get70FilePaths(file);

				if (paths[0] != null) {
					IProject project = CoreUtil.getProject(file);

					//_compare(paths[0], paths[1], "6.2 original JSP", "New 7.x JSP in " + project.getName());
				}
				else {
					MessageDialog.openInformation(
						UIUtil.getActiveShell(), "file not found", "There is no such file in liferay 7");
				}
			});

		_treeViewer7x.setComparator(
			new ViewerComparator() {

				@Override
				public int category(Object element) {
					File file = (File)element;

					if (file.isDirectory()) {
						return -1;
					}
					else {
						return super.category(element);
					}
				}

			});
	}

	private void _createTreePart62(Composite parent) {
		ScrolledComposite container62 = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		Composite part62 = SWTUtil.createComposite(container62, 1, 1, GridData.FILL_BOTH, 0, 0);

		FillLayout layout = new FillLayout();

		layout.marginHeight = 0;
		layout.marginWidth = 0;

		container62.setLayout(layout);

		container62.setMinSize(410, 200);
		container62.setExpandHorizontal(true);
		container62.setExpandVertical(true);
		container62.setContent(part62);

		Label label62 = new Label(part62, SWT.NONE);

		label62.setText("6.2 Custom JSPs (double-click to compare with 6.2)");

		_treeViewer62 = new TreeViewer(part62, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

		Tree tree = _treeViewer62.getTree();

		tree.setLayoutData(gridData);

		_treeViewer62.setContentProvider(new CustomJSPCompareViewContentProvider());
		_treeViewer62.setLabelProvider(new CustomJSPCompareViewLabelProvider62());
		_treeViewer62.addDoubleClickListener(new DoubleClickExpandListener(_treeViewer62));

		File project62 = new File(
			"/Users/terryjia/work/upgrade/plugins-sdk-with-git/hooks/sample-application-adapter-hook/");

		_treeViewer62.addDoubleClickListener(
			event -> {
				ISelection selection = event.getSelection();

				File file = (File)((ITreeSelection)selection).getFirstElement();

				if (file.isDirectory()) {
					return;
				}

				File[] paths = getCompareFiles62(file, project62);

				if (paths[0] != null) {
					_compare(paths[0], paths[1], "6.2 original JSP", "custom JSP");
				}
				else {
					MessageDialog.openInformation(
						UIUtil.getActiveShell(), "File not found", "There is no such file in liferay 62");
				}
			});

		_treeViewer62.setComparator(
			new ViewerComparator() {

				@Override
				public int category(Object element) {
					File file = (File)element;

					if (file.isDirectory()) {
						return -1;
					}

					return super.category(element);
				}

			});

		_treeViewer62.setInput(getCustomJspDir(project62));
	}

	private TreeViewer _treeViewer7x;
	private TreeViewer _treeViewer62;

}