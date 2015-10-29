package com.reprezen.swagedit.assist;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import com.reprezen.swagedit.assist.SwaggerProposal.ObjectProposal;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerSchema;

/**
 * This class provides basic content assist based on keywords used by 
 * the swagger schema.
 */
public class SwaggerContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

	private final SwaggerSchema schema = new SwaggerSchema();
	private final ObjectProposal swaggerProposal = new SwaggerCompletionProposal().get();

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		// should be done when we add templates
		//ICompletionProposal[] completionProposals = super.computeCompletionProposals(viewer, documentOffset);
		//if (completionProposals.length != 0) {
		//	return completionProposals;
		//}

		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();

		boolean startOfLine = true;
		int lineOfOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(documentOffset);
			int lineOffset = document.getLineOffset(lineOfOffset);			

			startOfLine = documentOffset == lineOffset;
		} catch (BadLocationException e) {}

		final List<ICompletionProposal> proposals = new LinkedList<>();

		// look that the cursor is after a :
		int delemiterPos = document.getDelimiterPosition(documentOffset);

		if (delemiterPos > -1) {
			// find the keyword before :
			final String word = document.getWordBeforeOffset(delemiterPos);
			// get proposals for that keyword
			if (!word.isEmpty()) {
				SwaggerProposal proposal = swaggerProposal.getProperties().get(word);
				if (proposal != null) {
					proposals.addAll(proposal.asCompletionProposal(documentOffset));						
				}
			}
		} else {
			// user started a word, find that input
			final String word = document.getWordBeforeOffset(documentOffset);
			if (!word.isEmpty()) {
				// look for keywords that match the input
				for (String keyword: schema.getKeywords(false)) {
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
			for (String current: schema.getKeywords(startOfLine)) {
				if (!(viewer.getDocument().get().contains(current))) {
					proposals.add(new CompletionProposal(current, documentOffset, 0, current.length()));
				}
			}
		}

		return proposals.toArray(new CompletionProposal[proposals.size()]);
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
		return null;
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return null;
	}

	@Override
	protected Image getImage(Template template) {
		return null;
	}

}