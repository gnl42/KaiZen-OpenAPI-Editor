/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
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
import com.reprezen.swagedit.Activator.Icons;

public class StyledCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

	private final int replacementOffset;
	private final int replacementLength;
	private final String replacementString;
	private final StyledString label;
	private final int cursorPosition;

	public StyledCompletionProposal(String replacement, StyledString label, int offset, int lenght, int position) {
		this.replacementString = replacement;
		this.label = label;
		this.replacementOffset = offset;
		this.replacementLength = lenght;
		this.cursorPosition = position;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return label;
	}

	@Override
	public void apply(IDocument document) {
		try {
			document.replace(replacementOffset, replacementLength, replacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(replacementOffset + cursorPosition, 0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return null;
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImage(Icons.assist_item);
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
	
}