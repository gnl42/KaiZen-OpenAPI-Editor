package com.reprezen.swagedit.completions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.completions.SwaggerProposal.ObjectProposal;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.Schema;

public class SwaggerCompletionProcessor implements IContentAssistProcessor {

	public SwaggerCompletionProcessor() {}

	private SwaggerProposal proposal;

	public SwaggerProposal get() {
		if (proposal == null) {
			final Schema schema = new Schema();
			final JsonNode tree = schema.getTree();
			
			proposal = new SwaggerProposal.Builder(tree).build();
		}

		return proposal;
	}

	public List<SwaggerProposal> matchPosition(int line, SwaggerDocument document) {
		final List<SwaggerProposal> proposals = new ArrayList<>();
		final List<Event> events = document.getEvent(line);

		for (Event event: events) {
			System.out.println("events start");
			if (event instanceof ScalarEvent) {
				String value = ((ScalarEvent) event).getValue();
				System.out.println(value);
				
				SwaggerProposal swagger = get();
				if (swagger instanceof ObjectProposal) {
					SwaggerProposal found = ((ObjectProposal) swagger).getProperties().get(value);

					if (found != null) {
						proposals.add(found);
					}
				}
			}
			System.out.println("events end");
		}
		
		return proposals;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		final List<ICompletionProposal> proposals = new ArrayList<>(); 
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		int lineOfOffset = 0;
		int lineOffset = 0;
		try {
			lineOfOffset = document.getLineOfOffset(offset);
			lineOffset = document.getLineOffset(lineOfOffset);
//			if (offset != lineOffset) {
//				return new ICompletionProposal[0];
//			}
		} catch (BadLocationException e) {}

		final List<SwaggerProposal> swaggerProposal = matchPosition(lineOfOffset, document);

		for (SwaggerProposal p: swaggerProposal) {
			System.out.println("here " + p);
			for (ICompletionProposal cp: p.asCompletionProposal(offset)) {
				proposals.add(cp);
			}
		}

//				List<SwaggerProposal> list = proposals.getProposals().get(value);
				
//				if (list != null) {
//					for (SwaggerProposal proposal: list) {
//						completionProposals.add(new CompletionProposal(proposal.name, 
//								offset + 1, 0, proposal.name.length()));
//					}
//				}
//			}
//		} else {
//			for (String c: proposals.getProposals().keySet()) {
//				// Only add proposal if it is not already present
//				if (!(viewer.getDocument().get().contains(c))) {
//					completionProposals.add(new CompletionProposal(c, offset, 0, c.length()));
//				}
//			}
//		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
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