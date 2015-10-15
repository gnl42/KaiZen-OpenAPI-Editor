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
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;

public class SwaggerCompletionProcessor implements IContentAssistProcessor {

	private SwaggerCompletionProposal proposals = SwaggerCompletionProposal.create();

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		int lineOfOffset = 0;
//		int lineOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(offset);
//			lineOffset = document.getLineOffset(lineOfOffset);

			// do not show any content assist in case the offset is not at the
			// beginning of a line
//			if (offset != lineOffset) {
//				return new ICompletionProposal[0];
//			}
		} catch (BadLocationException e) {
			// ignore here and just continue
		}

		List<Event> events = null;
		if (document instanceof SwaggerDocument) {
			events = ((SwaggerDocument) document).getEvent(lineOfOffset);
		}

		List<ICompletionProposal> completionProposals = new ArrayList<ICompletionProposal>();

		if (!events.isEmpty()) {
			Event event = events.get(0);
			if (event instanceof ScalarEvent) {
				String value = ((ScalarEvent) event).getValue();
				List<SwaggerProposal> list = proposals.getProposals().get(value);
				
				if (list != null) {
					for (SwaggerProposal proposal: list) {
						completionProposals.add(new CompletionProposal(proposal.name, 
								offset + 1, 0, proposal.name.length()));
					}
				}
			}
		} else {
			for (String c: proposals.getProposals().keySet()) {
				// Only add proposal if it is not already present
				if (!(viewer.getDocument().get().contains(c))) {
					completionProposals.add(new CompletionProposal(c, offset, 0, c.length()));
				}
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