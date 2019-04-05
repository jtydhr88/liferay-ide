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

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.progress.IProgressService;
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
public class LiferayReplEditor extends AbstractDecoratedTextEditor implements IDebugEventFilter, IEvaluationListener {

	public static final int RESULT_DISPLAY = 1;

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

	public void evalSelection(int resultMode) {
		if (!isInJavaProject()) {
			reportNotInJavaProjectError();

			return;
		}

		if (isEvaluating()) {
			return;
		}

		_checkCurrentProject();

		_evalStart();

		_resultMode = resultMode;

		_buildAndLaunch();

		if (_debugTarget == null) {
			_evalEnd();

			return;
		}

		fireEvalStateChanged();

		ISelectionProvider selectionProvider = getSelectionProvider();

		ITextSelection textSelection = (ITextSelection)selectionProvider.getSelection();

		String snippet = textSelection.getText();

		_snippetStart = textSelection.getOffset();

		_snippetEnd = _snippetStart + textSelection.getLength();

		_eval(snippet);
	}

	@Override
	public void evaluationComplete(IEvaluationResult result) {

		// TODO Auto-generated method stub

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

	public boolean isEvaluating() {
		return _evaluating;
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

	private void _buildAndLaunch() {
		IJavaProject javaProject = getJavaProject();

		if (javaProject == null) {
			return;
		}

		IProject project = javaProject.getProject();

		IWorkspace workspace = project.getWorkspace();

		boolean shouldBuild = false;

		if (!workspace.isAutoBuilding() || !javaProject.hasBuildState()) {
			shouldBuild = true;
		}

		if (shouldBuild) {
			if (!_performIncrementalBuild(project)) {
				return;
			}
		}

		boolean changed = false;

		if (_classPathHasChanged() || _workingDirHasChanged()) {
			changed = true;
		}

		boolean launch = false;

		if ((_debugTarget == null) || changed) {
			launch = true;
		}

		if (changed) {
			disconnectVM();
		}

		if (_debugTarget == null) {
			_checkMultipleEditors();
		}

		if (launch && (_debugTarget == null)) {
			_connectVM();

			ReplLauncher replLauncher = _serviceTracker.getService();

			_debugTarget = replLauncher.getDebugTarget(getFile());
		}
	}

	private void _checkCurrentProject() {
		IFile file = getFile();

		if (file == null) {
			return;
		}
	}

	private void _checkMultipleEditors() {
		ReplLauncher replLauncher = _serviceTracker.getService();

		_debugTarget = replLauncher.getDebugTarget(getFile());

		if (_debugTarget != null) {
			DebugPlugin debugPlugin = DebugPlugin.getDefault();

			debugPlugin.addDebugEventFilter(this);

			try {
				IThread[] threads = _debugTarget.getThreads();

				for (IThread thread : threads) {
					if (thread.isSuspended()) {
						thread.resume();
					}
				}
			}
			catch (DebugException de) {
				ProjectUI.logError(de);
			}
		}
	}

	private boolean _classPathHasChanged() {
		String[] classpath = _getClassPath(getJavaProject());

		if ((_launchedClassPath != null) && !_classPathsEqual(_launchedClassPath, classpath)) {
			MessageDialog.openWarning(_getShell(), "Warning", "Classpath has changed");

			return true;
		}

		return false;
	}

	private boolean _classPathsEqual(String[] path1, String[] path2) {
		if (path1.length != path2.length) {
			return false;
		}

		for (int i = 0; i < path1.length; i++) {
			if (!path1[i].equals(path2[i])) {
				return false;
			}
		}

		return true;
	}

	private void _connectVM() {
		DebugPlugin debugPlugin = DebugPlugin.getDefault();

		debugPlugin.addDebugEventFilter(this);

		_launchedClassPath = _getClassPath(getJavaProject());

		_launchedWorkingDir = _getWorkingDirectoryAttribute();

		ReplLauncher replLauncher = _serviceTracker.getService();

		BusyIndicator.showWhile(_getShell().getDisplay(), () -> replLauncher.launch(getFile()));
	}

	private void _eval(String snippet) {
		if (_getJavaThread() == null) {
			WaitThread waitThread = new WaitThread(Display.getCurrent(), this);

			waitThread.start();
			waitThread.block();
		}

		if (_getJavaThread() == null) {
			_showError(ProjectUI.createErrorStatus("Error evaluating"));

			return;
		}

		IPreferencesService preferencesService = Platform.getPreferencesService();

		boolean hitBreakpoints = preferencesService.getBoolean(
			JDIDebugPlugin.getUniqueIdentifier(), JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, true,
			null);

		try {
			IClassFileEvaluationEngine evaluationEngine = _getEvaluationEngine();

			evaluationEngine.evaluate(snippet, _getJavaThread(), this, hitBreakpoints);
		}
		catch (DebugException de) {
			IStatus status = ProjectUI.createErrorStatus("Error evaluating.", de);

			ProjectUI.log(status);

			_showError(status);
		}
	}

	private void _evalEnd() {
		UIUtil.async(
			() -> {
				_evaluating = false;
				_setTitleImage();
				fireEvalStateChanged();
				_showStatusMessage("");

				ISourceViewer sourceViewer = getSourceViewer();

				sourceViewer.setEditable(true);
			});
	}

	private synchronized void _evalStart() {
		if (_javaThread != null) {
			try {
				IThread thread = _javaThread;
				_javaThread = null;

				thread.resume();
			}
			catch (DebugException de) {
				ProjectUI.logError(de);
				_showException(de);

				return;
			}
		}

		_evaluating = true;
		_setTitleImage();
		fireEvalStateChanged();
		_showStatusMessage("Evaluating...");
		getSourceViewer().setEditable(false);
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

	private String[] _getClassPath(IJavaProject project) {
		try {
			return JavaRuntime.computeDefaultRuntimeClassPath(project);
		}
		catch (CoreException ce) {
			ProjectUI.logError(ce);

			return new String[0];
		}
	}

	private IClassFileEvaluationEngine _getEvaluationEngine() {
		if (_evaluationEngine == null) {
			IJavaProject javaProject = getJavaProject();

			IProject project = javaProject.getProject();

			IPath outputLocation = project.getWorkingLocation(ProjectUI.PLUGIN_ID);

			File file = new File(outputLocation.toOSString());

			IJavaThread javaThread = _getJavaThread();

			_evaluationEngine = EvaluationManager.newClassFileEvaluationEngine(
				javaProject, (IJavaDebugTarget)javaThread.getDebugTarget(), file);
		}

		if (getImports() != null) {
			_evaluationEngine.setImports(getImports());
		}
		else {
			_evaluationEngine.setImports(new String[0]);
		}

		return _evaluationEngine;
	}

	private IJavaThread _getJavaThread() {
		return _javaThread;
	}

	private Shell _getShell() {
		return getSite().getShell();
	}

	private IVMInstall _getVMInstall() {
		IFile file = getFile();

		if (file != null) {
			try {
				ReplLauncher replLauncher = _serviceTracker.getService();

				return replLauncher.getVMInstall(file);
			}
			catch (CoreException ce) {
				ProjectUI.logError(ce);
			}
		}

		return null;
	}

	private String _getWorkingDirectoryAttribute() {
		IFile file = getFile();

		if (file != null) {
			try {
				ReplLauncher replLauncher = _serviceTracker.getService();

				return replLauncher.getWorkingDirectoryAttribute(file);
			}
			catch (CoreException ce) {
				ProjectUI.logError(ce);
			}
		}

		return null;
	}

	private boolean _performIncrementalBuild(final IProject project) {
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {
				try {
					project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, progressMonitor);
				}
				catch (CoreException ce) {
					throw new InvocationTargetException(ce);
				}
			}

		};

		try {
			IWorkbench workbench = PlatformUI.getWorkbench();

			IProgressService progressService = workbench.getProgressService();

			progressService.run(true, false, runnableWithProgress);
		}
		catch (InterruptedException ie) {
			ProjectUI.logError(ie);

			_evalEnd();

			return false;
		}
		catch (InvocationTargetException ite) {
			ProjectUI.logError(ite);

			_evalEnd();

			return false;
		}

		return true;
	}

	private void _setTitleImage() {
		Image image = null;

		if (isEvaluating()) {
			_oldTitleImage = getTitleImage();
			image = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_SNIPPET_EVALUATING);
		}
		else {
			image = _oldTitleImage;
			_oldTitleImage = null;
		}

		if (image != null) {
			setTitleImage(image);
		}
	}

	private void _showError(IStatus status) {
		_evalEnd();

		if (!status.isOK()) {
			ErrorDialog.openError(_getShell(), "Error evaluating", null, status);
		}
	}

	private void _showError(String message) {
		_showError(ProjectUI.createErrorStatus(message));
	}

	private void _showException(Throwable throwable) {
		if (throwable instanceof DebugException) {
			DebugException de = (DebugException)throwable;

			IStatus status = de.getStatus();

			Throwable exception = status.getException();

			if (throwable != null) {
				_showUnderlyingException(exception);

				return;
			}
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		PrintStream printStream = new PrintStream(outputStream, true);

		throwable.printStackTrace(printStream);

		final String message = outputStream.toString();

		UIUtil.async(
			() -> {
				try {
					ISourceViewer sourceViewer = getSourceViewer();

					IDocument document = sourceViewer.getDocument();

					document.replace(_snippetEnd, 0, message);

					selectAndReveal(_snippetEnd, message.length());
				}
				catch (BadLocationException ble) {
				}
			});
	}

	private void _showStatusMessage(String message) {
		IEditorSite editorSite = (IEditorSite)getSite();

		EditorActionBarContributor editorActionBarContributor =
			(EditorActionBarContributor)editorSite.getActionBarContributor();

		IActionBars actionBars = editorActionBarContributor.getActionBars();

		IStatusLineManager statusLineManager = actionBars.getStatusLineManager();

		statusLineManager.setMessage(message);
	}

	private void _showUnderlyingException(Throwable throwable) {
		if (throwable instanceof InvocationException) {
			InvocationException invocationException = (InvocationException)throwable;

			ObjectReference objectReference = invocationException.exception();

			ReferenceType referenceType = objectReference.referenceType();

			String eName = referenceType.name();

			final String message = "An exception occurred during evaluation: " + eName;

			UIUtil.async(
				() -> {
					ISourceViewer sourceViewer = getSourceViewer();

					IDocument document = sourceViewer.getDocument();

					try {
						document.replace(_snippetEnd, 0, message);

						selectAndReveal(_snippetEnd, message.length());
					}
					catch (BadLocationException ble) {
					}
				});
		}
		else {
			_showException(throwable);
		}
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

	private boolean _workingDirHasChanged() {
		String workingDir = _getWorkingDirectoryAttribute();

		boolean changed = false;

		if ((workingDir == null) || (_launchedWorkingDir == null)) {
			if (workingDir != _launchedWorkingDir) {
				changed = true;
			}
		}
		else {
			if (!workingDir.equals(_launchedWorkingDir)) {
				changed = true;
			}
		}

		if (changed && (_debugTarget != null)) {
			MessageDialog.openWarning(
				_getShell(), "Warning", "The working directory has changed. Restarting the evaluation context.");
		}

		return changed;
	}

	private IAnnotationAccess _annotationAccess;
	private IClassFileEvaluationEngine _classFileEvaluationEngine;
	private IDebugModelPresentation _debugModelPresentation = DebugUITools.newDebugModelPresentation(
		ProjectUI.PLUGIN_ID);
	private IDebugTarget _debugTarget;
	private boolean _evaluating = false;
	private IEvaluationContext _evaluationContext;
	private IClassFileEvaluationEngine _evaluationEngine;
	private IJavaProject _javaProject;
	private IJavaThread _javaThread;
	private String[] _launchedClasspath;
	private String[] _launchedClassPath;
	private String _launchedWorkingDir;
	private Image _oldTitleImage;
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
	private int _resultMode;
	private String[] _serviceImports;
	private final ServiceTracker<ReplLauncher, ReplLauncher> _serviceTracker;
	private int _snippetEnd;
	private int _snippetStart;
	private String[] _userImports;

	private static class WaitThread extends Thread {

		@Override
		public void run() {
			try {
				synchronized (_lock) {
					_lock.wait(10000);
				}
			}
			catch (InterruptedException ie) {
			}
			finally {
				_display.syncExec(
					() -> {
					});

				_continueEventDispatching = false;

				_display.asyncExec(null);
			}
		}

		protected void block() {
			if (_display == Display.getCurrent()) {
				while (_continueEventDispatching) {
					if (!_display.readAndDispatch()) {
						_display.sleep();
					}
				}
			}
		}

		private WaitThread(Display display, Object lock) {
			super("Repl Wait Thread");

			setDaemon(true);
			_display = display;
			_lock = lock;
		}

		private volatile boolean _continueEventDispatching = true;
		private final Display _display;
		private final Object _lock;

	}

}