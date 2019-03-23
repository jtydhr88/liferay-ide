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

import java.util.Arrays;

import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings({"deprecation", "restriction"})
public class LiferayReplCompletionProcessor implements IContentAssistProcessor {

	public LiferayReplCompletionProcessor(LiferayReplEditor liferayReplEditor) {
		_liferayReplEditor = liferayReplEditor;

		JavaPlugin javaPlugin = JavaPlugin.getDefault();

		ContextTypeRegistry contextTypeRegistry = javaPlugin.getTemplateContextRegistry();

		TemplateContextType contextType = contextTypeRegistry.getContextType("java");

		if (contextType != null) {
			_templateEngine = new TemplateEngine(contextType);
		}

		_completionProposalComparator = new CompletionProposalComparator();
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int position) {
		try {
			setErrorMessage(null);

			try {
				_completionProposalCollector = new CompletionProposalCollector(_liferayReplEditor.getJavaProject());

				_liferayReplEditor.codeComplete(_completionProposalCollector);
			}
			catch (JavaModelException jme) {
				StyledText styledText = textViewer.getTextWidget();

				Shell shell = styledText.getShell();

				ErrorDialog.openError(
					shell, "Liferay Repl Completions", "Error collecting completion proposals.", jme.getStatus());

				ProjectUI.logError(jme);
			}

			IJavaCompletionProposal[] completionProposals = _completionProposalCollector.getJavaCompletionProposals();

			if (_templateEngine != null) {
				_templateEngine.reset();
				_templateEngine.complete(textViewer, position, null);

				TemplateProposal[] templateProposals = _templateEngine.getResults();

				IJavaCompletionProposal[] totalCompletionProposals =
					new IJavaCompletionProposal[completionProposals.length + templateProposals.length];

				System.arraycopy(templateProposals, 0, totalCompletionProposals, 0, templateProposals.length);

				System.arraycopy(
					completionProposals, 0, totalCompletionProposals, templateProposals.length,
					completionProposals.length);

				completionProposals = totalCompletionProposals;
			}

			return _sortProposals(completionProposals);
		}
		finally {
			setErrorMessage(_completionProposalCollector.getErrorMessage());
			_completionProposalCollector = null;
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer textViewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return _proposalAutoActivationSet;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		if (_contextInformationValidator == null) {
			_contextInformationValidator = new JavaParameterListValidator();
		}

		return _contextInformationValidator;
	}

	@Override
	public String getErrorMessage() {
		return _errorMessage;
	}

	public void orderProposalsAlphabetically(boolean order) {
		_completionProposalComparator.setOrderAlphabetically(order);
	}

	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		_proposalAutoActivationSet = activationSet;
	}

	public void setContentAssistant(ContentAssistant assistant) {
		_contentAssistant = assistant;
	}

	protected void setErrorMessage(String message) {
		if ((message != null) && (message.length() == 0)) {
			message = null;
		}

		_errorMessage = message;
	}

	private ICompletionProposal[] _sortProposals(IJavaCompletionProposal[] completionProposals) {
		if (_contentAssistant == null) {
			Arrays.sort(completionProposals, _completionProposalComparator);

			return completionProposals;
		}

		_contentAssistant.setSorter(
			new AbstractProposalSorter() {

				@Override
				public int compare(ICompletionProposal p1, ICompletionProposal p2) {
					return _completionProposalComparator.compare(p1, p2);
				}

			});

		return completionProposals;
	}

	private CompletionProposalCollector _completionProposalCollector;
	private CompletionProposalComparator _completionProposalComparator;
	private ContentAssistant _contentAssistant;
	private IContextInformationValidator _contextInformationValidator;
	private String _errorMessage;
	private LiferayReplEditor _liferayReplEditor;
	private char[] _proposalAutoActivationSet;
	private TemplateEngine _templateEngine;

}