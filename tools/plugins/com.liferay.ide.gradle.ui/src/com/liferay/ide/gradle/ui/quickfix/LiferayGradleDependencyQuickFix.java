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

package com.liferay.ide.gradle.ui.quickfix;

import com.liferay.ide.core.IWorkspaceProject;
import com.liferay.ide.core.util.ListUtil;
import com.liferay.ide.gradle.core.GradleUtil;
import com.liferay.ide.gradle.core.LiferayGradleCore;
import com.liferay.ide.gradle.core.LiferayGradleWorkspaceProject;
import com.liferay.ide.gradle.core.parser.GradleDependency;
import com.liferay.ide.gradle.core.parser.GradleDependencyUpdater;
import com.liferay.ide.project.core.util.LiferayWorkspaceUtil;
import com.liferay.ide.project.ui.ProjectUI;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.manipulation.TypeNameMatchCollector;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Charles Wu
 */
@SuppressWarnings("restriction")
public class LiferayGradleDependencyQuickFix implements IQuickFixProcessor {

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
		throws CoreException {

		if (ListUtil.isEmpty(locations) ||
			!(LiferayWorkspaceUtil.getLiferayWorkspaceProject() instanceof LiferayGradleWorkspaceProject)) {

			return new IJavaCompletionProposal[0];
		}

		List<IJavaCompletionProposal> resultingCollections = new ArrayList<>();

		for (IProblemLocation curr : locations) {
			try {
				List<IJavaCompletionProposal> newProposals = _process(context, curr);

				resultingCollections.addAll(newProposals);
			}
			catch (JavaModelException jme) {

				// ignore

			}
		}

		return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
	}

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		if ((problemId == IProblem.ImportNotFound) || (problemId == IProblem.UndefinedType)) {
			return true;
		}

		return false;
	}

	private GradleDependency _parseGradleDependency(IClasspathEntry entry) {
		try {
			IPath path = entry.getPath();

			String[] items = path.segments();

			return new GradleDependency(items[items.length - 5], items[items.length - 4], items[items.length - 3]);
		}
		catch (Exception e) {
			return null;
		}
	}

	private List<IJavaCompletionProposal> _process(IInvocationContext context, IProblemLocation problem)
		throws JavaModelException {

		int id = problem.getProblemId();

		if (id == 0) {
			return Collections.emptyList();
		}

		ASTNode selectedNode = problem.getCoveringNode(context.getASTRoot());

		if (selectedNode == null) {
			return Collections.emptyList();
		}

		if (id == IProblem.ImportNotFound) {
			ImportDeclaration importDeclaration = (ImportDeclaration)ASTNodes.getParent(
				selectedNode, ASTNode.IMPORT_DECLARATION);

			if (importDeclaration == null) {
				return Collections.emptyList();
			}

			String name = ASTNodes.asString(importDeclaration.getName());

			if (importDeclaration.isOnDemand()) {
				name = JavaModelUtil.concatenateName(name, "*");
			}

			return _processProposals(name, context);
		}
		else if (id == IProblem.UndefinedType) {
			if (selectedNode instanceof Name) {
				Name node = (Name)selectedNode;

				return _processProposals(node.getFullyQualifiedName(), context);
			}
		}

		return Collections.emptyList();
	}

	private List<IJavaCompletionProposal> _processProposals(String name, IInvocationContext context)
		throws JavaModelException {

		ICompilationUnit compilationUnit = context.getCompilationUnit();

		IJavaProject javaProject = compilationUnit.getJavaProject();

		IProject project = javaProject.getProject();

		IFile gradleFile = project.getFile("build.gradle");

		if (!gradleFile.exists()) {
			return Collections.emptyList();
		}

		int index = name.lastIndexOf('.');

		char[] packageName = null;

		if (index != -1) {
			String substring = name.substring(0, index);

			packageName = substring.toCharArray();
		}

		String substring = name.substring(index + 1);

		char[] typeName = substring.toCharArray();

		if ((typeName.length == 1) && (typeName[0] == '*')) {
			typeName = null;
		}

		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

		ArrayList<TypeNameMatch> result = new ArrayList<>();

		TypeNameMatchCollector requestor = new TypeNameMatchCollector(result);

		int matchMode = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;

		new SearchEngine().searchAllTypeNames(
			packageName, matchMode, typeName, matchMode, IJavaSearchConstants.TYPE, scope, requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);

		if (result.isEmpty()) {
			return Collections.emptyList();
		}

		List<IJavaCompletionProposal> proposals = new ArrayList<>();

		for (TypeNameMatch item : result) {
			IType type = item.getType();

			if (type != null) {
				IPackageFragmentRoot packageFragmentroot = (IPackageFragmentRoot)type.getAncestor(
					IJavaElement.PACKAGE_FRAGMENT_ROOT);

				IClasspathEntry entry = packageFragmentroot.getRawClasspathEntry();

				if (entry == null) {
					continue;
				}

				int entryKind = entry.getEntryKind();

				GradleDependency dependency = null;

				if (entryKind == IClasspathEntry.CPE_CONTAINER) {
					IClasspathContainer classpathContainer = JavaCore.getClasspathContainer(
						entry.getPath(), packageFragmentroot.getJavaProject());

					if (classpathContainer != null) {
						entry = JavaModelUtil.findEntryInContainer(classpathContainer, packageFragmentroot.getPath());

						if (entry != null) {
							dependency = _parseGradleDependency(entry);
						}
					}
				}
				else if (entryKind == IClasspathEntry.CPE_LIBRARY) {
					dependency = _parseGradleDependency(entry);
				}

				if (dependency != null) {
					String displayName =
						"Add Dependency '" + dependency.getGroup() + ":" + dependency.getName() + ":" +
							dependency.getVersion() + "' to Gradle";

					proposals.add(new CorrectionProposal(displayName, compilationUnit, dependency, gradleFile));
				}
			}
		}

		return proposals;
	}

	private class CorrectionProposal extends CUCorrectionProposal {

		public CorrectionProposal(
			String name, ICompilationUnit compiletionUnit, GradleDependency dependency, IFile file) {

			super(name, compiletionUnit, -10, ProjectUI.getPluginImageRegistry().get(ProjectUI.LIFERAY_LOGO_IMAGE_ID));

			_dependency = dependency;
			_gradleFile = file;
		}

		@Override
		public void apply(IDocument document) {
			try {
				GradleDependencyUpdater updater = new GradleDependencyUpdater(_gradleFile);

				StringBuilder sb = new StringBuilder();

				sb.append("compileOnly '");
				sb.append(_dependency.getGroup());
				sb.append(":");
				sb.append(_dependency.getName());

				IWorkspaceProject workspaceProject = LiferayWorkspaceUtil.getLiferayWorkspaceProject();

				if (workspaceProject.getTargetPlatformVersion() == null) {
					sb.append(":");
					sb.append(_dependency.getVersion());
				}

				sb.append("'");

				updater.updateDependency(sb.toString());

				GradleUtil.refreshProject(_gradleFile.getProject());
			}
			catch (IOException ioe) {
				LiferayGradleCore.logError(ioe);
			}

			IRunnableContext context = JavaPlugin.getActiveWorkbenchWindow();

			if (context == null) {
				context = new BusyIndicatorRunnableContext();
			}

			super.apply(document);
		}

		@Override
		public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
			return "Add Gradle Dependency to build.gradle file";
		}

		@Override
		protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
			if (_resultingEdit != null) {
				editRoot.addChild(_resultingEdit);
			}
		}

		private final GradleDependency _dependency;
		private final IFile _gradleFile;
		private TextEdit _resultingEdit;

	}

}