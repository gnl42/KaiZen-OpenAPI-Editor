package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerSchema;

/**
 * This class provides basic content assist based on keywords used by 
 * the swagger schema.
 */
public class SwaggerContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

	private final SwaggerProposalProvider proposalProvider = new SwaggerProposalProvider();

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		// should be done when we add templates
		//ICompletionProposal[] completionProposals = super.computeCompletionProposals(viewer, documentOffset);
		//if (completionProposals.length != 0) {
		//	return completionProposals;
		//}

		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		final SwaggerSchema schema = Activator.getDefault().getSchema();
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

		boolean startOfLine = true;
		int line = 0, lineOffset = 0, column = 0;
		try {
			line = document.getLineOfOffset(documentOffset);
			lineOffset = document.getLineOffset(line);
			column = selection.getOffset() - lineOffset;

			startOfLine = documentOffset == lineOffset;
		} catch (BadLocationException e) {}

		final String prefix = document.getWordBeforeOffset(documentOffset);
		final List<ICompletionProposal> proposals = new ArrayList<>();

		if (!prefix.isEmpty()) {
			for (String keyword : schema.getKeywords(startOfLine)) {				
				if (keyword.startsWith(prefix)) {
					final String replacement = keyword.substring(prefix.length(), keyword.length());
					proposals.add(new CompletionProposal(replacement, documentOffset, 0, replacement.length(), null,keyword, null, null));
				}
			}
		} else {
			JsonNode sp = null;
			try {
				String path = document.getPath(line, column);
				sp = schema.getProposals(path, document.asJson());
			} catch (Exception e) {
				e.printStackTrace();
				sp = null;
			}
			
			if (sp != null) {
				proposals.addAll(proposalProvider.getProposals(sp, documentOffset));
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