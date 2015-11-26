package com.reprezen.swagedit.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.reprezen.swagedit.Activator;

public class StyledCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

	private final int fReplacementOffset;
	private final int fReplacementLength;
	private final String fReplacementString;
	private final StyledString fLabel;
	private final int fCursorPosition;

	public StyledCompletionProposal(String replacement, StyledString label, int offset, int lenght, int position) {
		this.fReplacementString = replacement;
		this.fLabel = label;
		this.fReplacementOffset = offset;
		this.fReplacementLength = lenght;
		this.fCursorPosition = position;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return fLabel;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get("swagger_16");
	}

	@Override
	public IContextInformation getContextInformation() {
		// TODO Auto-generated method stub
		return null;
	}
	
}