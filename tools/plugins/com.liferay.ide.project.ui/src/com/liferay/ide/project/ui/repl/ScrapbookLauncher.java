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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.ILineNumberAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.launching.JavaMigrationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.liferay.ide.project.ui.ProjectUI;

/**
 * @author Terry Jia
 */
@SuppressWarnings("restriction")
public class ScrapbookLauncher implements IDebugEventSetListener {

	public static final String SCRAPBOOK_LAUNCH = IJavaDebugUIConstants.PLUGIN_ID + ".scrapbook_launch";

	public static final String SCRAPBOOK_FILE_PATH = IJavaDebugUIConstants.PLUGIN_ID + ".scrapbook_file_path";

	public static final QualifiedName SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO = new QualifiedName(
			IJavaDebugUIConstants.PLUGIN_ID, "snippet_editor_launch_config");

	private IJavaLineBreakpoint _magicBreakpoint;

	private HashMap<IFile, IDebugTarget> _scrapbookToVMs = new HashMap<>(10);
	private HashMap<IDebugTarget, IBreakpoint> _vmsToBreakpoints = new HashMap<>(10);
	private HashMap<IDebugTarget, IFile> _vmsToScrapbooks = new HashMap<>(10);

	private static ScrapbookLauncher fgDefault = null;

	private ScrapbookLauncher() {
	}

	public static ScrapbookLauncher getDefault() {
		if (fgDefault == null) {
			fgDefault = new ScrapbookLauncher();
		}
		return fgDefault;
	}

	protected ILaunch launch(IFile page) {
		cleanupLaunchConfigurations();

		if (!page.getFileExtension().equals("lrrepl")) {
			showNoPageDialog();

			return null;
		}

		IDebugTarget vm = getDebugTarget(page);

		if (vm != null) {
			return vm.getLaunch();
		}

		IJavaProject javaProject = JavaCore.create(page.getProject());

		URL jarURL = null;

		try {
			jarURL = JDIDebugUIPlugin.getDefault().getBundle().getEntry("snippetsupport.jar");
			jarURL = FileLocator.toFileURL(jarURL);
		}
		catch (MalformedURLException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);

			return null;
		}
		catch (IOException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);

			return null;
		}

		List<IRuntimeClasspathEntry> runtimeClasspathEntries = new ArrayList<>(3);

		String jarFile = jarURL.getFile();

		IRuntimeClasspathEntry supportEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(jarFile));

		runtimeClasspathEntries.add(supportEntry);

		try {
			IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(javaProject);
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].getClasspathProperty() != IRuntimeClasspathEntry.USER_CLASSES) {
					runtimeClasspathEntries.add(entries[i]);
				}
			}
			IRuntimeClasspathEntry[] classPath = runtimeClasspathEntries.toArray(new IRuntimeClasspathEntry[runtimeClasspathEntries.size()]);

			return doLaunch(javaProject, page, classPath, jarFile);
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);
		}
		return null;
	}

	private ILaunch doLaunch(IJavaProject p, IFile page, IRuntimeClasspathEntry[] classPath, String jarFile) {
		try {
			if (_vmsToScrapbooks.isEmpty()) {
				DebugPlugin.getDefault().addDebugEventListener(this);
			}
			ILaunchConfiguration config = null;
			ILaunchConfigurationWorkingCopy wc = null;
			try {
				config = getLaunchConfigurationTemplate(page);
				if (config != null) {
					wc = config.getWorkingCopy();
				}
			} catch (CoreException e) {
				config = null;
				JDIDebugUIPlugin.errorDialog("Unable to retrieve scrapbook settings", e);
			}

			if (config == null) {
				config = createLaunchConfigurationTemplate(page);
				wc = config.getWorkingCopy();
			}

			IPath outputLocation = p.getProject().getWorkingLocation(JDIDebugUIPlugin.getUniqueIdentifier());
			File f = outputLocation.toFile();
			URL u = null;
			try {
				u = getEncodedURL(f);
			} catch (MalformedURLException e) {
				JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);
				return null;
			} catch (UnsupportedEncodingException usee) {
				JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", usee);
				return null;
			}
			String[] defaultClasspath = JavaRuntime.computeDefaultRuntimeClassPath(p);
			String[] urls = new String[defaultClasspath.length + 1];
			urls[0] = u.toExternalForm();
			for (int i = 0; i < defaultClasspath.length; i++) {
				f = new File(defaultClasspath[i]);
				try {
					urls[i + 1] = getEncodedURL(f).toExternalForm();
				} catch (MalformedURLException e) {
					JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);
					return null;
				} catch (UnsupportedEncodingException usee) {
					JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", usee);
					return null;
				}
			}

			List<String> classpathList = new ArrayList<>(classPath.length);
			for (int i = 0; i < classPath.length; i++) {
				classpathList.add(classPath[i].getMemento());
			}
			if (wc == null) {
				wc = config.getWorkingCopy();
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathList);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, p.getElementName());
			if (wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, (String) null) == null) {
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
						"org.eclipse.jdt.debug.ui.scrapbookSourcepathProvider");
			}

			StringBuilder urlsString = new StringBuilder();
			for (int i = 0; i < urls.length; i++) {
				urlsString.append(' ');
				urlsString.append(urls[i]);
			}
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, urlsString.toString());
			wc.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);

			config = wc.doSave();

			ILaunch launch = config.launch(ILaunchManager.DEBUG_MODE, null);
			if (launch != null) {
				IDebugTarget dt = launch.getDebugTarget();
				IBreakpoint magicBreakpoint = createMagicBreakpoint(jarFile);
				_scrapbookToVMs.put(page, dt);
				_vmsToScrapbooks.put(dt, page);
				_vmsToBreakpoints.put(dt, magicBreakpoint);
				dt.breakpointAdded(magicBreakpoint);
				launch.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
				return launch;
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.errorDialog("Unable to launch scrapbook VM", e);
		}
		return null;
	}

	IBreakpoint createMagicBreakpoint(String jarFile) throws CoreException {
		String typeName = "org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain";

		IClassFileReader reader = ToolFactory.createDefaultClassFileReader(jarFile,
				typeName.replace('.', '/') + ".class", IClassFileReader.METHOD_INFOS | IClassFileReader.METHOD_BODIES); //$NON-NLS-1$
		IMethodInfo[] methodInfos = reader.getMethodInfos();
		for (IMethodInfo methodInfo : methodInfos) {
			if (!CharOperation.equals("nop".toCharArray(), methodInfo.getName())) {
				continue;
			}
			ICodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			ILineNumberAttribute lineNumberAttribute = codeAttribute.getLineNumberAttribute();
			int[][] lineNumberTable = lineNumberAttribute.getLineNumberTable();
			int lineNumber = lineNumberTable[0][1];

			_magicBreakpoint = JDIDebugModel.createLineBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), typeName,
					lineNumber, -1, -1, 0, false, null);
			_magicBreakpoint.setPersisted(false);
			return _magicBreakpoint;
		}
		throw new CoreException(
				new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
						"An error occurred creating the evaluation breakpoint location.", null));
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource() instanceof IDebugTarget && event.getKind() == DebugEvent.TERMINATE) {
				cleanup((IDebugTarget) event.getSource());
			}
		}
	}

	public IDebugTarget getDebugTarget(IFile page) {
		return _scrapbookToVMs.get(page);
	}

	public IBreakpoint getMagicBreakpoint(IDebugTarget target) {
		return _vmsToBreakpoints.get(target);
	}

	protected void showNoPageDialog() {
		String title = SnippetMessages.getString("ScrapbookLauncher.error.title");
		String msg = SnippetMessages.getString("ScrapbookLauncher.error.pagenotfound");
		MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(), title, msg);
	}

	protected void cleanup(IDebugTarget target) {
		Object page = _vmsToScrapbooks.get(target);
		if (page != null) {
			_vmsToScrapbooks.remove(target);
			_scrapbookToVMs.remove(page);
			_vmsToBreakpoints.remove(target);
			ILaunch launch = target.getLaunch();
			if (launch != null) {
				getLaunchManager().removeLaunch(launch);
			}
			if (_vmsToScrapbooks.isEmpty()) {
				DebugPlugin.getDefault().removeDebugEventListener(this);
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected URL getEncodedURL(File file) throws MalformedURLException, UnsupportedEncodingException {
		String urlDelimiter = "/";
		String unencoded = file.toURL().toExternalForm();
		StringBuilder encoded = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(unencoded, urlDelimiter);

		encoded.append(tokenizer.nextToken());
		encoded.append(urlDelimiter);
		encoded.append(tokenizer.nextToken());
		while (tokenizer.hasMoreElements()) {
			encoded.append(urlDelimiter);
			String token = tokenizer.nextToken();
			try {
				encoded.append(URLEncoder.encode(token, ResourcesPlugin.getEncoding()));
			} catch (UnsupportedEncodingException e) {
				encoded.append(URLEncoder.encode(token, "UTF-8"));
			}
		}
		if (file.isDirectory()) {
			encoded.append(urlDelimiter);
		}
		return new URL(encoded.toString());
	}

	public static ILaunchConfiguration getLaunchConfigurationTemplate(IFile file) throws CoreException {
		String memento = getLaunchConfigMemento(file);
		if (memento != null) {
			return getLaunchManager().getLaunchConfiguration(memento);
		}
		return null;
	}

	public static ILaunchConfiguration createLaunchConfigurationTemplate(IFile page) throws CoreException {
		ILaunchConfigurationType lcType = getLaunchManager()
				.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		String name = NLS.bind(SnippetMessages.getString("ScrapbookLauncher.17"), new String[] { page.getName() });
		ILaunchConfigurationWorkingCopy wc = lcType.newInstance(null, name);
		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
				"org.eclipse.jdt.internal.debug.ui.snippeteditor.ScrapbookMain");
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, page.getProject().getName());
		wc.setAttribute(SCRAPBOOK_LAUNCH, SCRAPBOOK_LAUNCH);
		wc.setAttribute(SCRAPBOOK_FILE_PATH, page.getFullPath().toString());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
				"org.eclipse.jdt.debug.ui.scrapbookSourcepathProvider");
		JavaMigrationDelegate.updateResourceMapping(wc);
		ILaunchConfiguration config = wc.doSave();
		setLaunchConfigMemento(page, config.getMemento());
		return config;
	}

	private static String getLaunchConfigMemento(IFile file) {
		try {
			return file.getPersistentProperty(SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO);
		} catch (CoreException e) {
			ProjectUI.logError(e);
		}
		return null;
	}

	protected static void setLaunchConfigMemento(IFile file, String memento) {
		try {
			file.setPersistentProperty(SNIPPET_EDITOR_LAUNCH_CONFIG_HANDLE_MEMENTO, memento);
		} catch (CoreException e) {
			ProjectUI.logError(e);
		}
	}

	protected static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static String getWorkingDirectoryAttribute(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config != null) {
			return config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
		}
		return null;
	}

	public static String getVMArgsAttribute(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config != null) {
			return config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String) null);
		}
		return null;
	}

	public static IVMInstall getVMInstall(IFile file) throws CoreException {
		ILaunchConfiguration config = getLaunchConfigurationTemplate(file);
		if (config == null) {
			IJavaProject pro = JavaCore.create(file.getProject());
			return JavaRuntime.getVMInstall(pro);
		}
		return JavaRuntime.computeVMInstall(config);
	}

	public void cleanupLaunchConfigurations() {
		try {
			ILaunchConfigurationType lcType = getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(lcType);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (int i = 0; i < configs.length; i++) {
				String path = configs[i].getAttribute(SCRAPBOOK_FILE_PATH, (String) null);
				if (path != null) {
					IPath pagePath = new Path(path);
					IResource res = root.findMember(pagePath);
					if (res == null) {
						configs[i].delete();
					}
				}
			}
		}
		catch (CoreException e) {
			ProjectUI.logError(e);
		}
	}
}
