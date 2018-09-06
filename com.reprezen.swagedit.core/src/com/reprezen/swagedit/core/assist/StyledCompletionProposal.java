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
package com.reprezen.swagedit.core.assist;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.Activator.Icons;
import com.reprezen.swagedit.core.utils.StringUtils;


public class StyledCompletionProposal
        implements ICompletionProposal, ICompletionProposalExtension5, ICompletionProposalExtension6 {

    private final int replacementOffset;
    private final String replacementString;
    private final StyledString styledDisplayString;
    private final String description;
    /** Lower-cased prefix - content assist typeahead should be case-insensitive */
    private final String prefix;
    /** Non-null text to be selected after code completion is done*/
    private final String selection;
    
    private final int preSelectedRegionLength;
    
    protected final Styler typeStyler = new StyledString.Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
        }
    };

    public StyledCompletionProposal(ProposalBuilder builder, String prefix, int offset, int preSelectedRegionLength) {
        styledDisplayString = new StyledString(builder.getDisplayString());
        if (builder.getType() != null) {
            styledDisplayString.append(": ", typeStyler).append(builder.getType(), typeStyler);
        }
        this.prefix = prefix != null ? prefix.toLowerCase() : null;
        this.replacementString = builder.getReplacementString();
        this.selection = builder.getSelection() == null ? "" : builder.getSelection();
        this.replacementOffset = offset;
        this.description = builder.getDescription();
        this.preSelectedRegionLength = preSelectedRegionLength;
    }

    @Override
    public StyledString getStyledDisplayString() {
        return styledDisplayString;
    }
    
    @Override
    public void apply(IDocument document) {
        int replacedLength = preSelectedRegionLength;
        int offset = replacementOffset;
        String text = replacementString;

        if (StringUtils.emptyToNull(prefix) != null && replacementString.toLowerCase().contains(prefix)) {
            if (replacementString.toLowerCase().startsWith(prefix)) {
                text = text.substring(prefix.length());
            } else {
                offset = replacementOffset - prefix.length();
                replacedLength = prefix.length();              
            }
        }
        
        try {
            document.replace(offset, replacedLength, text);
        } catch (BadLocationException x) {
            // ignore
        }
    }
    
    @Override
    public Point getSelection(IDocument document) {
        int offset = replacementOffset;

        if (StringUtils.emptyToNull(prefix) != null && replacementString.toLowerCase().contains(prefix)) {
            // offset at the start of the prefix
            offset = replacementOffset - prefix.length();
        }
        int replacementIndex = !selection.isEmpty() ? replacementString.indexOf(selection) : -1;
        int selectionStart = offset + (replacementIndex < 0 ? replacementString.length() : replacementIndex);
        return new Point(selectionStart, selection.length());
    }

    @Override
    public String getAdditionalProposalInfo() {
        return description;
    }

    @Override
    public String getDisplayString() {
        return styledDisplayString.getString();
    }

	@Override
	public Image getImage() {
		return Activator.getDefault().getImage(Icons.assist_item);
	}

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    public String getReplacementString() {
        return replacementString;
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return description;
    }
}