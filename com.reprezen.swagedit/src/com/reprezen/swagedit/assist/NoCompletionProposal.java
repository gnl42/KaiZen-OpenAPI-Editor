package com.reprezen.swagedit.assist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.reprezen.swagedit.Messages;

public class NoCompletionProposal implements ICompletionProposal {

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return Messages.no_default_proposals;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {			
		return null;
	}

	@Override
	public void apply(IDocument document) {}

}
