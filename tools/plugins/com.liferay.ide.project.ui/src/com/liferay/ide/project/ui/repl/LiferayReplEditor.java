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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IClassFileEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.ui.JDIContentAssistPreference;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.jdt.internal.debug.ui.JavaDebugOptionsManager;
import org.eclipse.jdt.internal.debug.ui.actions.DisplayAction;
import org.eclipse.jdt.internal.debug.ui.actions.EvaluateAction;
import org.eclipse.jdt.internal.debug.ui.actions.PopupInspectAction;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

import com.liferay.ide.project.ui.ProjectUI;
import com.liferay.ide.ui.util.UIUtil;
import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

/**
 * @author Terry Jia
 */
@SuppressWarnings({"restriction", "unchecked"})
public class LiferayReplEditor extends AbstractDecoratedTextEditor
		implements IDebugEventFilter, IEvaluationListener, IValueDetailListener {
	public static final String IMPORTS_CONTEXT = "SnippetEditor.imports";

	public final static int RESULT_DISPLAY = 1;
	public final static int RESULT_RUN = 2;
	public final static int RESULT_INSPECT = 3;

	private int _resultMode;
	private IJavaProject _javaProject;
	private IEvaluationContext _evaluationContext;
	private IDebugTarget _targetVM;
	private String[] _launchedClassPath;
	private String _launchedWorkingDir;
	private String _launchedVMArgs;
	private IVMInstall _launchedVM;
	private List<ILiferayReplStateChangedListener> _liferayReplStateListeners;
	private boolean _evaluating;
	private IJavaThread _javaThread;
	private boolean stepFiltersSetting;
	private int _start;
	private int _end;
	private String[] _imports = null;
	private Image _oldTitleImage = null;
	private IClassFileEvaluationEngine _fileEvaluationEngine = null;
	private IDebugModelPresentation _presentation = DebugUITools.newDebugModelPresentation(JDIDebugModel.getPluginIdentifier());
	private String _result;

	private static class WaitThread extends Thread {
		private Display _display;

		private volatile boolean _continueEventDispatching = true;

		private Object _lock;

		private WaitThread(Display display, Object lock) {
			super("Snippet Wait Thread");

			setDaemon(true);

			_display = display;

			_lock = lock;
		}

		@Override
		public void run() {
			try {
				synchronized (_lock) {
					_lock.wait(10000);
				}
			}
			catch (InterruptedException e) {
			}
			finally {
				_display.syncExec(new Runnable() {
					@Override
					public void run() {
					}
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
	}

	private IPartListener2 _activationListener = new IPartListener2() {

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if ("org.eclipse.jdt.debug.ui.SnippetEditor".equals(partRef.getId())) {
				System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "true");
			}
			else {
				System.setProperty(JDIDebugUIPlugin.getUniqueIdentifier() + ".scrapbookActive", "false");
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

	public LiferayReplEditor() {
		super();

		setDocumentProvider(JDIDebugUIPlugin.getDefault().getSnippetDocumentProvider());
		IPreferenceStore store = new ChainedPreferenceStore(
				new IPreferenceStore[] { PreferenceConstants.getPreferenceStore(), EditorsUI.getPreferenceStore() });
		setSourceViewerConfiguration(
				new JavaSnippetViewerConfiguration(JDIDebugUIPlugin.getDefault().getJavaTextTools(), store, this));
		_liferayReplStateListeners = new ArrayList<>(4);
		setPreferenceStore(store);
		setEditorContextMenuId("#LiferayReplEditorContext");
		setRulerContextMenuId("#JavaSnippetRulerContext");
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		IFile file = getFile();

		if (file != null) {
			String property = file.getPersistentProperty(new QualifiedName(JDIDebugUIPlugin.getUniqueIdentifier(), IMPORTS_CONTEXT));

			if (property != null) {
				_imports = JavaDebugOptionsManager.parseList(property);
			}
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);

		site.getWorkbenchWindow().getPartService().addPartListener(_activationListener);
	}

	@Override
	public void dispose() {
		shutDownVM();

		_presentation.dispose();

		_liferayReplStateListeners = null;

		ISourceViewer viewer = getSourceViewer();

		if (viewer != null) {
			((JDISourceViewer) viewer).dispose();
		}

		getSite().getWorkbenchWindow().getPartService().removePartListener(_activationListener);

		super.dispose();
	}

	@Override
	protected void createActions() {
		super.createActions();

		if (getFile() != null) {
			Action action = new TextOperationAction(
				SnippetMessages.getBundle(), "SnippetEditor.ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS);

			action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

			setAction("ContentAssistProposal", action);
			setAction("ShowInPackageView", new ShowInPackageViewAction(this));
			setAction("Stop", new StopAction(this));
			setAction("SelectImports", new SelectImportsAction(this));
		}
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);

		addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_GENERATE);
		addGroup(menu, ITextEditorActionConstants.GROUP_FIND, IContextMenuConstants.GROUP_SEARCH);
		addGroup(menu, IContextMenuConstants.GROUP_SEARCH, IContextMenuConstants.GROUP_SHOW);

		if (getFile() != null) {
			addAction(menu, IContextMenuConstants.GROUP_SHOW, "ShowInPackageView"); 
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "Run"); 
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "Stop"); 
			addAction(menu, IContextMenuConstants.GROUP_ADDITIONS, "SelectImports"); 
		}
	}

	protected boolean isVMLaunched() {
		return _targetVM != null;
	}

	public boolean isEvaluating() {
		return _evaluating;
	}

	public void evalSelection(int resultMode) {
		if (!isInJavaProject()) {
			reportNotInJavaProjectError();

			return;
		}

		if (isEvaluating()) {
			return;
		}

		checkCurrentProject();

		evaluationStarts();

		_resultMode = resultMode;

		buildAndLaunch();

		if (_targetVM == null) {
			evaluationEnds();

			return;
		}

		fireEvalStateChanged();

		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();

		String snippet = selection.getText();

		_start = selection.getOffset();

		_end = _start + selection.getLength();

		evaluate(snippet);
	}

	protected void checkCurrentProject() {
		IFile file = getFile();

		if (file == null) {
			return;
		}

		try {
			ILaunchConfiguration config = ScrapbookLauncher.getLaunchConfigurationTemplate(file);

			if (config != null) {
				String projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);

				IJavaProject javaProject = JavaCore.create(file.getProject());

				if (!javaProject.getElementName().equals(projectName)) {
					ScrapbookLauncher.setLaunchConfigMemento(file, null);
				}
			}
		}
		catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);

			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, ce.getStatus());

			evaluationEnds();
		}
	}

	protected void buildAndLaunch() {
		IJavaProject javaProject = getJavaProject();

		if (javaProject == null) {
			return;
		}

		boolean build = !javaProject.getProject().getWorkspace().isAutoBuilding() || !javaProject.hasBuildState();

		if (build) {
			if (!performIncrementalBuild()) {
				return;
			}
		}

		boolean changed = classPathHasChanged();

		if (!changed) {
			changed = workingDirHasChanged();
		}

		if (!changed) {
			changed = vmHasChanged();
		}

		if (!changed) {
			changed = vmArgsChanged();
		}

		boolean launch = _targetVM == null || changed;

		if (changed) {
			shutDownVM();
		}

		if (_targetVM == null) {
			checkMultipleEditors();
		}

		if (launch && _targetVM == null) {
			launchVM();

			_targetVM = ScrapbookLauncher.getDefault().getDebugTarget(getFile());
		}
	}

	protected boolean performIncrementalBuild() {
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor progressMonitor) throws InvocationTargetException {
				try {
					getJavaProject().getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, progressMonitor);
				}
				catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};

		try {
			PlatformUI.getWorkbench().getProgressService().run(true, false, runnableWithProgress);
		}
		catch (InterruptedException e) {
			ProjectUI.logError(e);

			evaluationEnds();

			return false;
		}
		catch (InvocationTargetException e) {
			ProjectUI.logError(e);

			evaluationEnds();

			return false;
		}

		return true;
	}

	protected void checkMultipleEditors() {
		_targetVM = ScrapbookLauncher.getDefault().getDebugTarget(getFile());

		if (_targetVM != null) {
			DebugPlugin.getDefault().addDebugEventFilter(this);

			try {
				IThread[] threads = _targetVM.getThreads();
				for (int i = 0; i < threads.length; i++) {
					IThread iThread = threads[i];
					if (iThread.isSuspended()) {
						iThread.resume();
					}
				}
			}
			catch (DebugException de) {
				JDIDebugUIPlugin.log(de);
			}
		}
	}

	protected void setImports(String[] imports) {
		_imports = imports;

		IFile file = getFile();

		if (file == null) {
			return;
		}

		String serialized = null;

		if (imports != null) {
			serialized = JavaDebugOptionsManager.serializeList(imports);
		}

		try {
			file.setPersistentProperty(new QualifiedName(JDIDebugUIPlugin.getUniqueIdentifier(), IMPORTS_CONTEXT),
					serialized);
		}
		catch (CoreException e) {
			ProjectUI.logError(e);

			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.imports"), null, e.getStatus());
		}
	}

	protected String[] getImports() {
		return _imports;
	}

	protected IEvaluationContext getEvaluationContext() {
		if (_evaluationContext == null) {
			IJavaProject project = getJavaProject();

			if (project != null) {
				_evaluationContext = project.newEvaluationContext();
			}
		}
		if (_evaluationContext != null) {
			if (getImports() != null) {
				_evaluationContext.setImports(getImports());
			}
			else {
				_evaluationContext.setImports(new String[] {});
			}
		}

		return _evaluationContext;
	}

	protected IJavaProject getJavaProject() {
		if (_javaProject == null) {
			try {
				_javaProject = findJavaProject();
			}
			catch (CoreException e) {
				ProjectUI.logError(e);

				showError(e.getStatus());
			}
		}

		return _javaProject;
	}

	protected void shutDownVM() {
		DebugPlugin.getDefault().removeDebugEventFilter(this);

		IDebugTarget target = _targetVM;

		if (_targetVM != null) {
			try {
				IBreakpoint breakpoint = ScrapbookLauncher.getDefault().getMagicBreakpoint(_targetVM);

				if (breakpoint != null) {
					_targetVM.breakpointRemoved(breakpoint, null);
				}

				if (getThread() != null) {
					getThread().resume();
				}

				_targetVM.terminate();
			}
			catch (DebugException e) {
				ProjectUI.logError(e);

				ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.shutdown"), null, e.getStatus());

				return;
			}

			vmTerminated();

			ScrapbookLauncher.getDefault().cleanup(target);
		}
	}

	protected void vmTerminated() {
		_targetVM = null;
		_javaThread = null;
		_evaluationContext = null;
		_launchedClassPath = null;

		if (_fileEvaluationEngine != null) {
			_fileEvaluationEngine.dispose();
		}

		_fileEvaluationEngine = null;

		fireEvalStateChanged();
	}

	public void addSnippetStateChangedListener(ILiferayReplStateChangedListener listener) {
		if (_liferayReplStateListeners != null && !_liferayReplStateListeners.contains(listener)) {
			_liferayReplStateListeners.add(listener);
		}
	}

	public void removeSnippetStateChangedListener(ILiferayReplStateChangedListener listener) {
		if (_liferayReplStateListeners != null) {
			_liferayReplStateListeners.remove(listener);
		}
	}

	protected void fireEvalStateChanged() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Shell shell = getShell();

				if (_liferayReplStateListeners != null && shell != null && !shell.isDisposed()) {
					List<ILiferayReplStateChangedListener> v = new ArrayList<>(_liferayReplStateListeners);

					for (int i = 0; i < v.size(); i++) {
						ILiferayReplStateChangedListener liferayReplStateChangedListener = v.get(i);

						liferayReplStateChangedListener.snippetStateChanged(LiferayReplEditor.this);
					}
				}
			}
		};

		Shell shell = getShell();

		if (shell != null) {
			getShell().getDisplay().asyncExec(runnable);
		}
	}

	protected void evaluate(String snippet) {
		if (getThread() == null) {
			WaitThread eThread = new WaitThread(Display.getCurrent(), this);

			eThread.start();

			eThread.block();
		}

		if (getThread() == null) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(),
					IJavaDebugUIConstants.INTERNAL_ERROR,
					"Evaluation failed: internal error - unable to obtain an execution context.", null); 

			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, 
					status);

			evaluationEnds();

			return;
		}

		boolean hitBreakpoints = Platform.getPreferencesService().getBoolean(JDIDebugPlugin.getUniqueIdentifier(),
				JDIDebugModel.PREF_SUSPEND_FOR_BREAKPOINTS_DURING_EVALUATION, true, null);

		try {
			getEvaluationEngine().evaluate(snippet, getThread(), this, hitBreakpoints);
		}
		catch (DebugException e) {
			ProjectUI.logError(e);

			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating"), null, e.getStatus());

			evaluationEnds();
		}
	}

	@Override
	public void evaluationComplete(IEvaluationResult result) {
		boolean severeErrors = false;

		if (result.hasErrors()) {
			String[] errors = result.getErrorMessages();

			severeErrors = errors.length > 0;

			if (result.getException() != null) {
				showException(result.getException());
			}

			showAllErrors(errors);
		}

		IJavaValue value = result.getValue();

		if (value != null && !severeErrors) {
			switch (_resultMode) {
				case RESULT_DISPLAY:
					displayResult(value);
	
					break;
				case RESULT_INSPECT:
					JavaInspectExpression exp = new JavaInspectExpression(result.getSnippet().trim(), value);
	
					showExpression(exp);
	
					break;
				case RESULT_RUN:
					break;
			}
		}

		evaluationEnds();
	}

	protected void showExpressionView() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = UIUtil.getActivePage();

				if (page != null) {
					IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);

					if (part == null) {
						try {
							page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
						}
						catch (PartInitException e) {
							ProjectUI.logError(e);

							showError(e.getStatus());
						}
					}
					else {
						page.bringToTop(part);
					}
				}
			}
		};

		async(runnable);
	}

	protected void codeComplete(CompletionRequestor requestor) throws JavaModelException {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();

		int start = selection.getOffset();

		String snippet = getSourceViewer().getDocument().get();

		IEvaluationContext evaluationContext = getEvaluationContext();

		if (evaluationContext != null) {
			evaluationContext.codeComplete(snippet, start, requestor);
		}
	}

	protected IJavaElement[] codeResolve() throws JavaModelException {
		ISourceViewer viewer = getSourceViewer();

		if (viewer == null) {
			return null;
		}

		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();

		int start = selection.getOffset();
		int len = selection.getLength();

		String snippet = viewer.getDocument().get();

		IEvaluationContext evaluationContext = getEvaluationContext();

		if (evaluationContext != null) {
			return evaluationContext.codeSelect(snippet, start, len);
		}

		return null;
	}

	protected void showError(IStatus status) {
		evaluationEnds();

		if (!status.isOK()) {
			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.evaluating2"), null, status);
		}
	}

	protected void showError(String message) {
		Status status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, message, null);

		showError(status);
	}

	protected void displayResult(IJavaValue result) {
		StringBuilder resultString = new StringBuilder();

		try {
			IJavaType type = result.getJavaType();

			if (type != null) {
				String sig = type.getSignature();

				if ("V".equals(sig)) { 
					resultString.append(SnippetMessages.getString("SnippetEditor.noreturnvalue")); 
				}
				else {
					if (sig != null) {
						resultString.append(SnippetMessages.getFormattedString("SnippetEditor.typename", 
								result.getReferenceTypeName()));
					}
					else {
						resultString.append(" "); 
					}

					resultString.append(DisplayAction.trimDisplayResult(evaluateToString(result)));
				}
			}
			else {
				resultString.append(DisplayAction.trimDisplayResult(result.getValueString()));
			}
		}
		catch (DebugException e) {
			ProjectUI.logError(e);

			ErrorDialog.openError(getShell(), SnippetMessages.getString("SnippetEditor.error.toString"), null, 
					e.getStatus());
		}

		String message = resultString.toString();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					getSourceViewer().getDocument().replace(_end, 0, message);

					selectAndReveal(_end, message.length());
				}
				catch (BadLocationException e) {
				}
			}
		};
		async(runnable);
	}

	protected synchronized String evaluateToString(IJavaValue value) {
		_result = null;
		_presentation.computeDetail(value, this);

		if (_result == null) {
			try {
				wait(10000);
			}
			catch (InterruptedException e) {
				return SnippetMessages.getString("SnippetEditor.error.interrupted"); 
			}
		}

		return _result;
	}

	@Override
	public synchronized void detailComputed(IValue value, final String result) {
		_result = result;

		this.notifyAll();
	}

	protected void showAllErrors(final String[] errors) {
		IDocument document = getSourceViewer().getDocument();
		String delimiter = document.getLegalLineDelimiters()[0];

		StringBuilder errorString = new StringBuilder();

		for (int i = 0; i < errors.length; i++) {
			errorString.append(errors[i] + delimiter);
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					getSourceViewer().getDocument().replace(_start, 0, errorString.toString());
					selectAndReveal(_start, errorString.length());
				}
				catch (BadLocationException e) {
				}
			}
		};

		async(runnable);
	}

	private void showExpression(final JavaInspectExpression expression) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				new InspectPopupDialog(getShell(), EvaluateAction.getPopupAnchor(getSourceViewer().getTextWidget()),
						PopupInspectAction.ACTION_DEFININITION_ID, expression).open();
			}
		};

		async(runnable);
	}

	protected void showException(Throwable exception) {
		if (exception instanceof DebugException) {
			DebugException de = (DebugException) exception;

			Throwable throwable = de.getStatus().getException();

			if (throwable != null) {
				showUnderlyingException(throwable);

				return;
			}
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		PrintStream ps = new PrintStream(bos, true);

		exception.printStackTrace(ps);

		final String message = bos.toString();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					getSourceViewer().getDocument().replace(_end, 0, message);

					selectAndReveal(_end, message.length());
				}
				catch (BadLocationException e) {
				}
			}
		};

		async(runnable);
	}

	protected void showUnderlyingException(Throwable t) {
		if (t instanceof InvocationException) {
			InvocationException ie = (InvocationException) t;
			ObjectReference ref = ie.exception();
			String eName = ref.referenceType().name();
			final String message = SnippetMessages.getFormattedString("SnippetEditor.exception", eName); 
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						getSourceViewer().getDocument().replace(_end, 0, message);
						selectAndReveal(_end, message.length());
					} catch (BadLocationException e) {
					}
				}
			};
			async(r);
		} else {
			showException(t);
		}
	}

	protected IJavaProject findJavaProject() throws CoreException {
		IFile file = getFile();
		if (file != null) {
			IProject p = file.getProject();
			if (p.getNature(JavaCore.NATURE_ID) != null) {
				return JavaCore.create(p);
			}
		}
		return null;
	}

	protected boolean classPathHasChanged() {
		String[] classpath = getClassPath(getJavaProject());
		if (_launchedClassPath != null && !classPathsEqual(_launchedClassPath, classpath)) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.warning"), 
					SnippetMessages.getString("SnippetEditor.warning.cpchange")); 
			return true;
		}
		return false;
	}

	protected boolean workingDirHasChanged() {
		String wd = getWorkingDirectoryAttribute();
		boolean changed = false;
		if (wd == null || _launchedWorkingDir == null) {
			if (wd != _launchedWorkingDir) {
				changed = true;
			}
		} else {
			if (!wd.equals(_launchedWorkingDir)) {
				changed = true;
			}
		}
		if (changed && _targetVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), 
					SnippetMessages.getString(
							"SnippetEditor.The_working_directory_has_changed._Restarting_the_evaluation_context._2")); 
		}
		return changed;
	}

	protected boolean vmArgsChanged() {
		String args = getVMArgsAttribute();
		boolean changed = false;
		if (args == null || _launchedVMArgs == null) {
			if (args != _launchedVMArgs) {
				changed = true;
			}
		} else {
			if (!args.equals(_launchedVMArgs)) {
				changed = true;
			}
		}
		if (changed && _targetVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), 
					SnippetMessages.getString("SnippetEditor.1")); 
		}
		return changed;
	}

	protected boolean vmHasChanged() {
		IVMInstall vm = getVMInstall();
		boolean changed = false;
		if (vm == null || _launchedVM == null) {
			if (vm != _launchedVM) {
				changed = true;
			}
		} else {
			if (!vm.equals(_launchedVM)) {
				changed = true;
			}
		}
		if (changed && _targetVM != null) {
			MessageDialog.openWarning(getShell(), SnippetMessages.getString("SnippetEditor.Warning_1"), SnippetMessages 
					.getString("SnippetEditor.The_JRE_has_changed._Restarting_the_evaluation_context._2")); 
		}
		return changed;
	}

	protected boolean classPathsEqual(String[] path1, String[] path2) {
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

	protected synchronized void evaluationStarts() {
		if (_javaThread != null) {
			try {
				IThread thread = _javaThread;
				_javaThread = null;
				thread.resume();
			} catch (DebugException e) {
				ProjectUI.logError(e);
				showException(e);
				return;
			}
		}
		_evaluating = true;
		setTitleImage();
		fireEvalStateChanged();
		showStatus(SnippetMessages.getString("SnippetEditor.evaluating")); 
		getSourceViewer().setEditable(false);
	}

	protected void setTitleImage() {
		Image image = null;
		if (_evaluating) {
			_oldTitleImage = getTitleImage();
			image = JavaDebugImages.get(JavaDebugImages.IMG_OBJS_SNIPPET_EVALUATING);
		} else {
			image = _oldTitleImage;
			_oldTitleImage = null;
		}
		if (image != null) {
			setTitleImage(image);
		}
	}

	protected void evaluationEnds() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				_evaluating = false;
				setTitleImage();
				fireEvalStateChanged();
				showStatus(""); 
				getSourceViewer().setEditable(true);
			}
		};
		async(r);
	}

	protected void showStatus(String message) {
		IEditorSite site = (IEditorSite) getSite();
		EditorActionBarContributor contributor = (EditorActionBarContributor) site.getActionBarContributor();
		contributor.getActionBars().getStatusLineManager().setMessage(message);
	}

	protected String[] getClassPath(IJavaProject project) {
		try {
			return JavaRuntime.computeDefaultRuntimeClassPath(project);
		} catch (CoreException e) {
			ProjectUI.logError(e);
			return new String[0];
		}
	}

	protected Shell getShell() {
		return UIUtil.getActiveShell();
	}

	@Override
	public DebugEvent[] filterDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent e = events[i];
			Object source = e.getSource();
			if (source instanceof IDebugElement) {
				IDebugElement de = (IDebugElement) source;
				if (de instanceof IDebugTarget) {
					if (de.getDebugTarget().equals(_targetVM)) {
						if (e.getKind() == DebugEvent.TERMINATE) {
							setThread(null);
							Runnable r = new Runnable() {
								@Override
								public void run() {
									vmTerminated();
								}
							};
							getShell().getDisplay().asyncExec(r);
						}
					}
				} else if (de instanceof IJavaThread) {
					if (e.getKind() == DebugEvent.SUSPEND) {
						IJavaThread jt = (IJavaThread) de;
						try {
							if (jt.equals(getThread()) && e.getDetail() == DebugEvent.EVALUATION) {
								return null;
							}
							IJavaStackFrame f = (IJavaStackFrame) jt.getTopStackFrame();
							if (f != null) {
								IJavaDebugTarget target = (IJavaDebugTarget) f.getDebugTarget();
								IBreakpoint[] bps = jt.getBreakpoints();

								int lineNumber = f.getLineNumber();
								if (e.getDetail() == DebugEvent.STEP_END && (lineNumber == 28)
										&& f.getDeclaringTypeName().equals(
												"org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain1") 
										&& jt.getDebugTarget() == _targetVM) {

									target.setStepFiltersEnabled(stepFiltersSetting);
									setThread(jt);
									return null;
								} else if (e.getDetail() == DebugEvent.BREAKPOINT && bps.length > 0 && bps[0].equals(
										ScrapbookLauncher.getDefault().getMagicBreakpoint(jt.getDebugTarget()))) {

									IStackFrame[] frames = jt.getStackFrames();
									for (int j = 0; j < frames.length; j++) {
										IJavaStackFrame frame = (IJavaStackFrame) frames[j];
										if (frame.getReceivingTypeName().equals(
												"org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain1") 
												&& frame.getName().equals("eval")) { 

											stepFiltersSetting = target.isStepFiltersEnabled();
											target.setStepFiltersEnabled(false);
											frame.stepOver();
											return null;
										}
									}
								}
							}
						} catch (DebugException ex) {
							JDIDebugUIPlugin.log(ex);
						}
					}
				}
			}
		}
		return events;
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		JavaSourceViewerConfiguration sourceViewerConfiguration = (JavaSourceViewerConfiguration) getSourceViewerConfiguration();
		return sourceViewerConfiguration.affectsTextPresentation(event);
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		JDISourceViewer isv = (JDISourceViewer) getSourceViewer();
		if (isv != null) {
			IContentAssistant assistant = isv.getContentAssistant();
			if (assistant instanceof ContentAssistant) {
				JDIContentAssistPreference.changeConfiguration((ContentAssistant) assistant, event);
			}
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			if (configuration instanceof JavaSourceViewerConfiguration) {
				JavaSourceViewerConfiguration jsv = (JavaSourceViewerConfiguration) configuration;
				if (jsv.affectsTextPresentation(event)) {
					jsv.handlePropertyChangeEvent(event);
					isv.invalidateTextPresentation();
				}
			}
			super.handlePreferenceStoreChanged(event);
		}
	}

	protected IJavaThread getThread() {
		return _javaThread;
	}

	protected synchronized void setThread(IJavaThread thread) {
		_javaThread = thread;
		notifyAll();
	}

	protected void launchVM() {
		DebugPlugin.getDefault().addDebugEventFilter(this);
		_launchedClassPath = getClassPath(getJavaProject());
		_launchedWorkingDir = getWorkingDirectoryAttribute();
		_launchedVMArgs = getVMArgsAttribute();
		_launchedVM = getVMInstall();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				ScrapbookLauncher.getDefault().launch(getFile());
			}
		};
		BusyIndicator.showWhile(getShell().getDisplay(), r);
	}

	public IFile getFile() {
		IEditorInput input = getEditorInput();
		return input.getAdapter(IFile.class);
	}

	@Override
	protected void updateSelectionDependentActions() {
		super.updateSelectionDependentActions();
		fireEvalStateChanged();
	}

	@Override
	protected void setPartName(String title) {
		cleanupOnRenameOrMove();
		super.setPartName(title);
	}

	protected void cleanupOnRenameOrMove() {
		if (isVMLaunched()) {
			shutDownVM();
		} else {
			_javaThread = null;
			_evaluationContext = null;
			_launchedClassPath = null;

			if (_fileEvaluationEngine != null) {
				_fileEvaluationEngine.dispose();
				_fileEvaluationEngine = null;
			}
		}
		_javaProject = null;
	}

	protected boolean isInJavaProject() {
		try {
			return findJavaProject() != null;
		} catch (CoreException ce) {
			JDIDebugUIPlugin.log(ce);
		}
		return false;
	}

	protected void reportNotInJavaProjectError() {
		String projectName = null;
		IFile file = getFile();
		if (file != null) {
			IProject p = file.getProject();
			projectName = p.getName();
		}
		String message = ""; 
		if (projectName != null) {
			message = projectName + SnippetMessages.getString("JavaSnippetEditor._is_not_a_Java_Project._n_1"); 
		}
		showError(message + SnippetMessages
				.getString("JavaSnippetEditor.Unable_to_perform_evaluation_outside_of_a_Java_Project_2")); 
	}

	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell = getSite().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.open();
		IPath path = dialog.getResult();

		if (path == null) {
			if (progressMonitor != null) {
				progressMonitor.setCanceled(true);
			}
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile file = workspace.getRoot().getFile(path);
		final IEditorInput newInput = new FileEditorInput(file);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			public void execute(final IProgressMonitor monitor) throws CoreException {
				IDocumentProvider dp = getDocumentProvider();
				dp.saveDocument(monitor, newInput, dp.getDocument(getEditorInput()), true);
			}
		};

		boolean success = false;
		try {
			getDocumentProvider().aboutToChange(newInput);
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
			success = true;
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			JDIDebugUIPlugin.log(x);
			String title = SnippetMessages.getString("JavaSnippetEditor.Problems_During_Save_As..._3"); 
			String msg = SnippetMessages.getString("JavaSnippetEditor.Save_could_not_be_completed.__4") 
					+ x.getTargetException().getMessage();
			MessageDialog.openError(shell, title, msg);
		} finally {
			getDocumentProvider().changed(newInput);
			if (success) {
				setInput(newInput);
			}
		}

		if (progressMonitor != null) {
			progressMonitor.setCanceled(!success);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	protected IClassFileEvaluationEngine getEvaluationEngine() {
		if (_fileEvaluationEngine == null) {
			IPath outputLocation = getJavaProject().getProject()
					.getWorkingLocation(JDIDebugUIPlugin.getUniqueIdentifier());
			java.io.File f = new java.io.File(outputLocation.toOSString());
			_fileEvaluationEngine = EvaluationManager.newClassFileEvaluationEngine(getJavaProject(),
					(IJavaDebugTarget) getThread().getDebugTarget(), f);
		}
		if (getImports() != null) {
			_fileEvaluationEngine.setImports(getImports());
		} else {
			_fileEvaluationEngine.setImports(new String[] {});
		}
		return _fileEvaluationEngine;
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new JDISourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles | SWT.LEFT_TO_RIGHT);

		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	protected String getWorkingDirectoryAttribute() {
		IFile file = getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getWorkingDirectoryAttribute(file);
			} catch (CoreException e) {
				ProjectUI.logError(e);
			}
		}
		return null;
	}

	protected String getVMArgsAttribute() {
		IFile file = getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getVMArgsAttribute(file);
			} catch (CoreException e) {
				ProjectUI.logError(e);
			}
		}
		return null;
	}

	protected IVMInstall getVMInstall() {
		IFile file = getFile();
		if (file != null) {
			try {
				return ScrapbookLauncher.getVMInstall(file);
			} catch (CoreException e) {
				ProjectUI.logError(e);
			}
		}
		return null;
	}

	protected void async(Runnable r) {
		Control control = getVerticalRuler().getControl();
		if (!control.isDisposed()) {
			control.getDisplay().asyncExec(r);
		}
	}

	protected void showAndSelect(final String text, final int offset) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					getSourceViewer().getDocument().replace(offset, 0, text);
				} catch (BadLocationException e) {
					ProjectUI.logError(e);
				}
				selectAndReveal(offset, text.length());
			}
		};
		async(r);
	}

	@Override
	public <T> T getAdapter(Class<T> required) {
		if (required == IShowInTargetList.class) {
			return (T) new IShowInTargetList() {
				@SuppressWarnings("deprecation")
				@Override
				public String[] getShowInTargetIds() {
					return new String[] { JavaUI.ID_PACKAGES, IPageLayout.ID_RES_NAV };
				}

			};
		}
		return super.getAdapter(required);
	}

}
