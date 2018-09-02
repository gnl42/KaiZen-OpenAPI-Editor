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

import com.google.common.base.Strings;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.Activator.Icons;
import com.reprezen.swagedit.core.utils.StringUtils;
import com.reprezen.swagedit.core.utils.StringUtils.QuoteStyle;

public class StyledCompletionProposal
        implements ICompletionProposal, ICompletionProposalExtension5, ICompletionProposalExtension6 {

    private final int replacementOffset;
    private final String replacementString;
    private final StyledString styledDisplayString;
    private final String description;
    /** Lower-cased prefix - content assist typeahead should be case-insensitive */
    private final String prefix;
    private final String selection;
    
    private final int preSelectedRegionLength;
    
    protected final Styler typeStyler = new StyledString.Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
        }
    };

    public StyledCompletionProposal(ProposalBuilder builder, String prefix, int offset, int preSelectedRegionLength) {
        final StyledString styledString = new StyledString(builder.getDisplayString());
        if (builder.getType() != null) {
            styledString.append(": ", typeStyler).append(builder.getType(), typeStyler);
        }
        this.styledDisplayString = styledString;
        this.prefix = prefix != null ? prefix.toLowerCase() : null;
        if (prefix != null && builder.getReplacementString().toLowerCase().contains(prefix)) {
            this.replacementString = handleQuotes(builder.getReplacementString(), prefix);
        } else {
            this.replacementString = builder.getReplacementString();
        }
        this.selection = builder.getSelection() == null ? "" : builder.getSelection();
        this.replacementOffset = offset;
        this.description = builder.getDescription();
        this.preSelectedRegionLength = preSelectedRegionLength;
    }

    @Override
    public StyledString getStyledDisplayString() {
        return styledDisplayString;
    }
    
    private String handleQuotes(String replacementString, String prefix) {
        // If the replacement string has quotes
        // we should know which kind is it
        QuoteStyle quote = QuoteStyle.INVALID;
        if (StringUtils.isQuoted(replacementString)) {
            quote = StringUtils.QuoteStyle.parse(replacementString.charAt(0));
        }

        // If prefix is a quote, which kind is it
        QuoteStyle prefixQuote = QuoteStyle.INVALID;
        if (StringUtils.isQuoted(prefix)) {
            prefixQuote = StringUtils.QuoteStyle.parse(prefix.charAt(0));
        }

        // Handle quotes
        String rString = replacementString;
        if (quote != QuoteStyle.INVALID && prefixQuote != QuoteStyle.INVALID) {
            if (quote != prefixQuote) {
                // If quotes are not same, replace quotes from replacement
                // string with one from prefix 
                rString = rString.substring(1);
                if (rString.endsWith(quote.getValue())) {
                    rString = rString.substring(0, rString.length() - 1);
                }
                rString = prefixQuote.getValue() + rString;
            } else {
                // remove last quote to avoid duplicates
                rString = rString.substring(0, rString.length() - 1);
            }
        }
        return rString;
    }

    @Override
    public void apply(IDocument document) {
        int offset = replacementOffset;
        String text = replacementString;

        if (Strings.emptyToNull(prefix) != null) {
            if (replacementString.toLowerCase().contains(prefix)) {
                text = text.substring(prefix.length());
            }
        }
      
        try {
            document.replace(offset, preSelectedRegionLength, text);
        } catch (BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        int offset = replacementOffset;

        if (Strings.emptyToNull(prefix) != null) {
            if (replacementString.toLowerCase().startsWith(prefix)) {
                offset = replacementOffset - prefix.length();
            } else if (replacementString.toLowerCase().contains(prefix)) {
                offset = replacementOffset - prefix.length();
            }
        }
        int replacementIndex = !"".equals(selection) ? replacementString.indexOf(selection) : -1;
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