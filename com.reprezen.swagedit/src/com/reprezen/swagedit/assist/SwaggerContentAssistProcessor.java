package com.reprezen.swagedit.assist;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.reprezen.swagedit.assist.SwaggerProposal.ObjectProposal;
import com.reprezen.swagedit.validation.SwaggerSchema;

/**
 * This class provides basic content assist based on keywords used by 
 * the swagger schema.
 * 
 */
public class SwaggerContentAssistProcessor implements IContentAssistProcessor {

	private final SwaggerSchema schema = new SwaggerSchema();
	private final ObjectProposal swaggerProposal = new SwaggerCompletionProposal().get();

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		final Set<String> keywords = schema.getKeywords();
		final IDocument document = viewer.getDocument();

		boolean startOfLine = true;
		int lineOfOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(documentOffset);
			int lineOffset = document.getLineOffset(lineOfOffset);			

			startOfLine = documentOffset == lineOffset;
		} catch (BadLocationException e) {}

		final List<ICompletionProposal> proposals = new LinkedList<>();
	
		// look that the cursor is after a :
		int delemiterPos = isAfterDelimiter(document, documentOffset);
		if (delemiterPos > -1) {
			// find the keyword before :
			final String word = getWord(document, delemiterPos);
			// get proposals for that keyword 
			if (!word.isEmpty()) {
				SwaggerProposal proposal = swaggerProposal.getProperties().get(word);
				if (proposal != null) {
					proposals.addAll(proposal.asCompletionProposal(documentOffset));						
				}
			}
		} else {
			// user started a word, find that input
			final String word = getWord(document, documentOffset);
			if (!word.isEmpty()) {
				// look for keywords that match the input
				for (String keyword: keywords) {
					if (keyword.startsWith(word)) {
						final String replacement = keyword.substring(word.length(), keyword.length());
						proposals.add(new CompletionProposal(replacement, 
								documentOffset, 
								0, 
								replacement.length(), 
								null,
								keyword,
								null,
								null));
					}	
				}
			}
		}

		// if nothing has been found, add list of keywords
		if (proposals.isEmpty()) {
			for (String current: keywords) {
				if (!(viewer.getDocument().get().contains(current))) {
					proposals.add(new CompletionProposal(current, documentOffset, 0, current.length()));
				}
			}
		}

		return proposals.toArray(new CompletionProposal[proposals.size()]);
	}

	private int isAfterDelimiter(IDocument document, int offset) {
		while(true) {
			try {
				char c = document.getChar(--offset);
				if (Character.isLetterOrDigit(c))
					return -1;
				if (c == ':')
					return offset;			
			} catch (BadLocationException e) {
				return -1;
			}
		}
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