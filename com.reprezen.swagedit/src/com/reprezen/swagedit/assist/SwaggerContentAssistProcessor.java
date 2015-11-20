package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Lists;
import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.templates.SwaggerContextType;

/**
 * This class provides basic content assist based on keywords used by the
 * swagger schema.
 */
public class SwaggerContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

	private final SwaggerProposalProvider proposalProvider = new SwaggerProposalProvider();
	private String currentPath = null;

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		int line = 0, lineOffset = 0, column = 0;
		try {
			line = document.getLineOfOffset(documentOffset);
			lineOffset = document.getLineOffset(line);
			column = selection.getOffset() - lineOffset;
		} catch (BadLocationException e) {}

		final String prefix = extractPrefix(viewer, documentOffset);
		// we have to remove the length of
		// the prefix to obtain the correct
		// column to resolve the path
		if (!prefix.isEmpty()) {
			column -= prefix.length();
		}

		currentPath = document.getPath(line, column);

		// compute template proposals
		final ICompletionProposal[] templateProposals = super.computeCompletionProposals(viewer, documentOffset);
		final List<ICompletionProposal> proposals = new ArrayList<>();

		proposals.addAll(proposalProvider.getCompletionProposals(
				currentPath, 
				document.getNodeForPath(currentPath), 
				prefix, 
				documentOffset));

		if (templateProposals != null && templateProposals.length > 0) {
			proposals.addAll(Lists.newArrayList(templateProposals));
		}

		final ICompletionProposal[] result = new ICompletionProposal[proposals.size()];
		for (int i = 0; i < proposals.size(); i++) {
			result[i] = proposals.get(i);
		}

		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return geTemplateStore().getTemplates();
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return getContextTypeRegistry().getContextType(SwaggerContextType.getContentType(currentPath));
	}

	@Override
	protected Image getImage(Template template) {
		return null;
	}

	protected TemplateStore geTemplateStore() {
		return Activator.getDefault().getTemplateStore();
	}

	protected ContextTypeRegistry getContextTypeRegistry() {
		return Activator.getDefault().getContextTypeRegistry();
	}
}