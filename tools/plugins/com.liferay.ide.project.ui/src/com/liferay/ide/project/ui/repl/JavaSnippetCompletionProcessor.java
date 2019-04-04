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

import java.util.Arrays;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Terry Jia
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class JavaSnippetCompletionProcessor implements IContentAssistProcessor {

	private CompletionProposalCollector _completionProposalCollector;
	private JavaSnippetEditor _JavaSnippetEditor;
	private IContextInformationValidator _validator;
	private TemplateEngine _templateEngine;
	private CompletionProposalComparator _completionProposalComparator;
	private String _errorMessage;
	private char[] _proposalAutoActivationSet;
	private ContentAssistant _ContentAssistant;

	public JavaSnippetCompletionProcessor(JavaSnippetEditor editor) {
		_JavaSnippetEditor = editor;

		JavaPlugin javaPlugin = JavaPlugin.getDefault();

		ContextTypeRegistry templateContextRegistry = javaPlugin.getTemplateContextRegistry();

		TemplateContextType contextType = templateContextRegistry.getContextType("java");

		if (contextType != null) {
			_templateEngine = new TemplateEngine(contextType);
		}

		_completionProposalComparator = new CompletionProposalComparator();
	}

	public void setContentAssistant(ContentAssistant assistant) {
		_ContentAssistant = assistant;
	}

	@Override
	public String getErrorMessage() {
		return _errorMessage;
	}

	protected void setErrorMessage(String message) {
		if (message != null && message.length() == 0) {
			message = null;
		}
		_errorMessage = message;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (_validator == null) {
			_validator = new JavaParameterListValidator();
		}
		return _validator;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
		try {
			setErrorMessage(null);

			try {
				_completionProposalCollector = new CompletionProposalCollector(_JavaSnippetEditor.getJavaProject());
				_JavaSnippetEditor.codeComplete(_completionProposalCollector);
			}
			catch (JavaModelException x) {
				Shell shell = viewer.getTextWidget().getShell();

				ErrorDialog.openError(
					shell, SnippetMessages.getString("CompletionProcessor.errorTitle"),
					SnippetMessages.getString("CompletionProcessor.errorMessage"), x.getStatus());
				JDIDebugUIPlugin.log(x);
			}

			IJavaCompletionProposal[] results = _completionProposalCollector.getJavaCompletionProposals();

			if (_templateEngine != null) {
				_templateEngine.reset();
				_templateEngine.complete(viewer, position, null);

				TemplateProposal[] templateResults = _templateEngine.getResults();

				IJavaCompletionProposal[] total = new IJavaCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results = total;
			}

			return _order(results);
		}
		finally {
			setErrorMessage(_completionProposalCollector.getErrorMessage());
			_completionProposalCollector = null;
		}
	}

	private ICompletionProposal[] _order(IJavaCompletionProposal[] proposals) {
		if (_ContentAssistant == null) {
			Arrays.sort(proposals, _completionProposalComparator);
			return proposals;
		}

		_ContentAssistant.setSorter(new AbstractProposalSorter() {
			@Override
			public int compare(ICompletionProposal p1, ICompletionProposal p2) {
				return _completionProposalComparator.compare(p1, p2);
			}

		});

		return proposals;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return _proposalAutoActivationSet;
	}

	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		_proposalAutoActivationSet = activationSet;
	}

	public void orderProposalsAlphabetically(boolean order) {
		_completionProposalComparator.setOrderAlphabetically(order);
	}
}
