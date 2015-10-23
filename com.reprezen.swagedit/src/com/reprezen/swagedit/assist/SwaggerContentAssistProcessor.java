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
		final List<String> keywords = schema.getKeywords();
		final IDocument document = viewer.getDocument();

		boolean startOfLine = true;
		int lineOfOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(documentOffset);
			int lineOffset = document.getLineOffset(lineOfOffset);			

			startOfLine = documentOffset == lineOffset;
		} catch (BadLocationException e) {}

		final List<CompletionProposal> proposals = new LinkedList<>();

		if (startOfLine) {
			for (String current: keywords) {
				if (!(viewer.getDocument().get().contains(current))) {
					proposals.add(new CompletionProposal(current, documentOffset, 0, current.length()));
				}
			}
		} else {
			final String word = getWord(document, documentOffset);
			if (!word.isEmpty()) {
				for (String current: keywords) {
					if (current.startsWith(word)) {
						final String replacement = current.substring(word.length(), current.length());
						proposals.add(new CompletionProposal(replacement, documentOffset, 0, replacement.length()));
					}			
				}
			}
		}

		return proposals.toArray(new CompletionProposal[proposals.size()]);
	}

	private String getWord(IDocument document, int documentOffset) {
		final StringBuffer buffer = new StringBuffer();
		while (true) {
			try {
				char c = document.getChar(--documentOffset);
				if (!Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
					return buffer.reverse().toString().trim(); 
				} else {
					buffer.append(c);
				}
			} catch (BadLocationException e) {
				return buffer.reverse().toString().trim();
			}
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