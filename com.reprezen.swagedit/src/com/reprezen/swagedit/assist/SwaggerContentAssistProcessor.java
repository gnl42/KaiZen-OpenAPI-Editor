package com.reprezen.swagedit.assist;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.reprezen.swagedit.validation.SwaggerSchema;

/**
 * This class provides basic content assist based on keywords used by 
 * the swagger schema.
 * 
 */
public class SwaggerContentAssistProcessor implements IContentAssistProcessor {

	private final SwaggerSchema schema = new SwaggerSchema();

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		final String[] keywords = schema.getKeywords();		
		final IDocument document = viewer.getDocument();

		boolean startOfLine = true;
		try {
			int lineOfOffset = document.getLineOfOffset(documentOffset);
			int lineOffset = document.getLineOffset(lineOfOffset);			

			startOfLine = documentOffset == lineOffset;
		} catch (BadLocationException e) {}

		if (startOfLine) {
			final List<CompletionProposal> proposals = new LinkedList<>();
			for (int i = 0; i < keywords.length; i++) {
				final String current = keywords[i];
	
				if (!(viewer.getDocument().get().contains(current))) {
					proposals.add(new CompletionProposal(current, 
							documentOffset, 
							0, 
							current.length()));
				}
			}

			return proposals.toArray(new CompletionProposal[proposals.size()]);
		} else {
			return new ICompletionProposal[0];
		}	
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

}