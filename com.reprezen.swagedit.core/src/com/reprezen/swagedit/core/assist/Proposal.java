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

import java.util.Objects;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.reprezen.swagedit.core.utils.StringUtils;
import com.reprezen.swagedit.core.utils.StringUtils.QuoteStyle;

public class Proposal {

    public final String replacementString;
    public final String displayString;
    public final String type;
    public final String description;
    private final String selection;

    protected final Styler typeStyler = new StyledString.Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
        }
    };

    public Proposal(String replacementString, String displayString, String description, String type, String selection) {
        this.replacementString = replacementString;
        this.displayString = displayString;
        this.type = type;
        this.description = description;
        this.selection = selection;
    }

    public Proposal(String replacementString, String displayString, String description, String type) {
        this(replacementString, displayString, description, type, "");
    }

    /**
     * Returns a {@link CompletionProposal}.
     * 
     * The {@link CompletionProposal} will be returned only if the prefix is null, or if the replacement string starts
     * with or contains the prefix. Otherwise this method returns null.
     * 
     * @param prefix
     * @param offset
     * @return proposal
     */
    public StyledCompletionProposal asStyledCompletionProposal(String prefix, int offset) {
        final StyledString styledString = new StyledString(displayString);
        if (type != null) {
            styledString.append(": ", typeStyler).append(type, typeStyler);
        }

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

        StyledCompletionProposal proposal = null;
        if (StringUtils.emptyToNull(prefix) == null) {
            proposal = new StyledCompletionProposal(replacementString, styledString, null, description, offset,
                    selection);
        } else if (rString.toLowerCase().contains(prefix.toLowerCase())) {
            proposal = new StyledCompletionProposal(rString, styledString, prefix, description, offset, selection);
        }

        return proposal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayString == null) ? 0 : displayString.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Proposal)) {
            return false;
        }

        Proposal other = (Proposal) obj;

        return Objects.equals(other.displayString, displayString);
    }

}
