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

package com.liferay.ide.project.ui.repl;

import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.ui.util.UIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings("restriction")
public class LiferayReplEditor extends AbstractDecoratedTextEditor implements IDebugEventFilter {

	public static final String SERVICE_IMPORTS_CONTEXT = "LiferayReplEditor.service.imports";

	public static final String USER_IMPORTS_CONTEXT = "LiferayReplEditor.user.imports";

	public LiferayReplEditor() {
		ProjectUI projectUI = ProjectUI.getDefault();

		setDocumentProvider(projectUI.getReplDocumentProvider());

		IPreferenceStore preferenceStore = new ChainedPreferenceStore(
			new IPreferenceStore[] {PreferenceConstants.getPreferenceStore(), EditorsUI.getPreferenceStore()});

		setSourceViewerConfiguration(
			new LiferayReplViewerConfiguration(projectUI.getJavaTextTools(), preferenceStore, this));

		_replStateChangedListeners = new ArrayList<>(4);

		setPreferenceStore(preferenceStore);
		setEditorContextMenuId("#LiferayReplEditorContext");
		setRulerContextMenuId("#LiferayReplRulerContext");

		Bundle bundle = FrameworkUtil.getBundle(LiferayReplEditor.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceTracker = new ServiceTracker<>(bundleContext, ReplLauncher.class, null);

		_serviceTracker.open();
	}

	public void addReplStateChangedListener(ReplStateChangedListener replStateChangedListener) {
		if ((_replStateChangedListeners != null) && !_replStateChangedListeners.contains(replStateChangedListener)) {
			_replStateChangedListeners.add(replStateChangedListener);
		}
	}

	public void disconnectVM() {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();

		debugPlugin.removeDebugEventFilter(this);

		IDebugTarget jvm = _debugTarget;

		if (_debugTarget != null) {
			ReplLauncher replLauncher = _serviceTracker.getService();

			try {
				IBreakpoint breakpoint = replLauncher.getMagicBreakpoint(_debugTarget);

				if (breakpoint != null) {
					_debugTarget.breakpointRemoved(breakpoint, null);
				}

				if (_javaThread != null) {
					_javaThread.resume();
				}

				_debugTarget.terminate();
			}
			catch (DebugException de) {
				ProjectUI.logError(de);

				_showError(ProjectUI.createErrorStatus("Error disconnecting from VM", de));

				return;
			}

			_vmDisconnected();

			replLauncher.cleanup(jvm);
		}
	}

	@Override
	public void dispose() {
		disconnectVM();

		_debugModelPresentation.dispose();

		_replStateChangedListeners = null;

		ISourceViewer sourceViewer = getSourceViewer();

		if (sourceViewer instanceof JDISourceViewer) {
			((JDISourceViewer)sourceViewer).dispose();
		}

		IWorkbenchPartSite workbenchPartSite = getSite();

		IWorkbenchWindow workbenchWindow = workbenchPartSite.getWorkbenchWindow();

		IPartService partService = workbenchWindow.getPartService();

		partService.removePartListener(_partListener);

		_serviceTracker.close();

		super.dispose();
	}

	@Override
	public DebugEvent[] filterDebugEvents(DebugEvent[] debugEvents) {

		// TODO Auto-generated method stub

		return null;
	}

	public IFile getFile() {
		IEditorInput editorInput = getEditorInput();

		return editorInput.getAdapter(IFile.class);
	}

	public IJavaProject getJavaProject() {
		if (_javaProject == null) {
			try {
				_javaProject = _findJavaProject();
			}
			catch (CoreException ce) {
				ProjectUI.logError(ce);
				_showError(ce.getStatus());
			}
		}

		return _javaProject;
	}

	@Override
	public void init(IEditorSite editorSite, IEditorInput editorInput) throws PartInitException {
		super.init(editorSite, editorInput);

		IWorkbenchWindow workbenchWindow = editorSite.getWorkbenchWindow();

		IPartService partService = workbenchWindow.getPartService();

		partService.addPartListener(_partListener);
	}

	public boolean isVMConnected() {
		if (_debugTarget != null) {
			return true;
		}

		return false;
	}

	public void removeReplStateChangedListener(ReplStateChangedListener replStateChangedListener) {
		if (_replStateChangedListeners != null) {
			_replStateChangedListeners.remove(replStateChangedListener);
		}
	}

	protected void codeComplete(CompletionRequestor completionRequestor) throws JavaModelException {
	}

	@Override
	protected void createActions() {
		super.createActions();

		if (getFile() == null) {
			return;
		}

		Action action = new TextOperationAction(
			LiferayReplMessages.getBundle(), "LiferayReplEditor.ContentAssistProposal.", this,
			ISourceViewer.CONTENTASSIST_PROPOSALS);

		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

		setAction("ContentAssistProposal", action);

		setAction("Disconnect", new DisconnectAction(this));
		setAction("SelectImports", new SelectImportsAction(this));
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parentComposite, IVerticalRuler verticalRuler, int styles) {
		_annotationAccess = getAnnotationAccess();

		_overviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer sourceViewer = new JDISourceViewer(
			parentComposite, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles | SWT.LEFT_TO_RIGHT);

		// ensure decoration support has been created and configured.

		getSourceViewerDecorationSupport(sourceViewer);

		return sourceViewer;
	}

	@Override
	protected void doSetInput(IEditorInput editorInput) throws CoreException {
		super.doSetInput(editorInput);

		IFile file = getFile();

		if (file != null) {
			String property = file.getPersistentProperty(new QualifiedName(ProjectUI.PLUGIN_ID, USER_IMPORTS_CONTEXT));

			if (property != null) {
				_userImports = property.split(",");
			}

			property = file.getPersistentProperty(new QualifiedName(ProjectUI.PLUGIN_ID, SERVICE_IMPORTS_CONTEXT));

			if (property == null) {
				property =
					"com.liferay.portal.kernel.service.RoleLocalService,com.liferay.portal.kernel.service." +
						"UserLocalService";
			}

			_serviceImports = property.split(",");
		}
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menuManager) {
		super.editorContextMenuAboutToShow(menuManager);

		addGroup(menuManager, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_GENERATE);
		addGroup(menuManager, ITextEditorActionConstants.GROUP_FIND, IContextMenuConstants.GROUP_SEARCH);
		addGroup(menuManager, IContextMenuConstants.GROUP_SEARCH, IContextMenuConstants.GROUP_SHOW);

		if (getFile() != null) {
			addAction(menuManager, IContextMenuConstants.GROUP_ADDITIONS, "Run");
			addAction(menuManager, IContextMenuConstants.GROUP_ADDITIONS, "Disconnect");
			addAction(menuManager, IContextMenuConstants.GROUP_ADDITIONS, "SelectImports");
		}
	}

	protected void fireEvalStateChanged() {
		Shell shell = _getShell();

		if (shell != null) {
			UIUtil.async(
				() -> {
					if ((_replStateChangedListeners != null) && (shell != null) && !shell.isDisposed()) {
						List<ReplStateChangedListener> listeners = new ArrayList<>(_replStateChangedListeners);

						listeners.forEach(
							listener -> {
								try {
									listener.replStateChanged(this);
								}
								catch (Exception e) {
									ProjectUI.logError("Error in repl state listeners", e);
								}
							});
					}
				});
		}
	}

	protected String[] getImports() {
		Set<String> imports = new HashSet<>();

		Collections.addAll(imports, _serviceImports);
		Collections.addAll(imports, _userImports);

		String[] sortedImports = imports.toArray(new String[0]);

		Arrays.sort(sortedImports, (s1, s2) -> s1.compareTo(s2));

		return sortedImports;
	}

	protected boolean isInJavaProject() {
		try {
			if (_findJavaProject() != null) {
				return true;
			}

			return false;
		}
		catch (CoreException ce) {
			ProjectUI.logError(ce);
		}

		return false;
	}

	protected void reportNotInJavaProjectError() {
		String projectName = null;

		IFile file = getFile();

		if (file != null) {
			IProject project = file.getProject();

			projectName = project.getName();
		}

		String message = "";

		if (projectName != null) {
			message = projectName + " is not a Java project.";
		}

		_showError(message + " Unable to perform an evaluation outside of a Java project");
	}

	private void _evaluationEnds() {
	}

	private IJavaProject _findJavaProject() throws CoreException {
		IFile file = getFile();

		if (file != null) {
			IProject project = file.getProject();

			if (project.getNature(JavaCore.NATURE_ID) != null) {
				return JavaCore.create(project);
			}
		}

		return null;
	}

	private Shell _getShell() {
		return getSite().getShell();
	}

	private void _showError(IStatus status) {
		_evaluationEnds();

		if (!status.isOK()) {
			ErrorDialog.openError(_getShell(), "Error evaluating", null, status);
		}
	}

	private void _showError(String message) {
		_showError(ProjectUI.createErrorStatus(message));
	}

	private void _vmDisconnected() {
		_debugTarget = null;
		_javaThread = null;
		_evaluationContext = null;
		_launchedClasspath = null;

		if (_classFileEvaluationEngine != null) {
			_classFileEvaluationEngine.dispose();
		}

		_classFileEvaluationEngine = null;

		fireEvalStateChanged();
	}

	private IAnnotationAccess _annotationAccess;
	private IClassFileEvaluationEngine _classFileEvaluationEngine;
	private IDebugModelPresentation _debugModelPresentation = DebugUITools.newDebugModelPresentation(
		ProjectUI.PLUGIN_ID);
	private IDebugTarget _debugTarget;
	private IEvaluationContext _evaluationContext;
	private IJavaProject _javaProject;
	private IJavaThread _javaThread;
	private String[] _launchedClasspath;
	private IOverviewRuler _overviewRuler;

	private final IPartListener2 _partListener = new IPartListener2() {

		@Override
		public void partActivated(IWorkbenchPartReference workbenchPartReference) {
			String id = workbenchPartReference.getId();

			if ("com.liferay.ide.project.ui.LiferayReplEditor".equals(id)) {
				System.setProperty("LiferayReplEditor.active", "true");
			}
			else {
				System.setProperty("LiferayReplEditor.active", "false");
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

	};

	private List<ReplStateChangedListener> _replStateChangedListeners;
	private String[] _serviceImports;
	private final ServiceTracker<ReplLauncher, ReplLauncher> _serviceTracker;
	private String[] _userImports;

}