package com.reprezen.swagedit.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.reprezen.swagedit.editor.SwaggerDocumentProvider.SwaggerDocument;

public class SwaggerCompletionProcessor implements IContentAssistProcessor {

	private String[] proposals = new String[] { "swagger:", "info:", "description:", "host:", "schemes:", "consumes:",
			"produces:", "paths:", "definitions:" };

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		int lineOfOffset = 0;
		int lineOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(offset);
			lineOffset = document.getLineOffset(lineOfOffset);

			// do not show any content assist in case the offset is not at the
			// beginning of a line
			if (offset != lineOffset) {
				return new ICompletionProposal[0];
			}
		} catch (BadLocationException e) {
			// ignore here and just continue
		}

		if (document instanceof SwaggerDocument) {
			((SwaggerDocument) document).getEvent(lineOfOffset);
		}

		List<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();

		for (String c : proposals) {
			// Only add proposal if it is not already present
			if (!(viewer.getDocument().get().contains(c))) {
				completionProposals.add(new CompletionProposal(c, offset, 0, c.length()));
			}
		}

		return completionProposals.toArray(new ICompletionProposal[completionProposals.size()]);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

}