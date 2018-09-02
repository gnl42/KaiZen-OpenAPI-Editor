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

import com.google.common.base.Strings;

public class ProposalBuilder {

    private final String displayString;
    private String replacementString;
    private String type;
    private String description;
    private String selection;

    public ProposalBuilder(String displayString) {
        this.displayString = displayString;
    }

    public ProposalBuilder replacementString(String replacementString) {
        this.replacementString = replacementString;
        return this;
    }

    public ProposalBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ProposalBuilder selection(String selection) {
        this.selection = selection;
        return this;
    }

    public ProposalBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns a {@link CompletionProposal}.
     * 
     * The {@link CompletionProposal} will be returned only if the prefix is null, or if the replacement string starts
     * with or contains the prefix. Otherwise this method returns null.
     * 
     */
    public StyledCompletionProposal build(String prefix, int offset, int preSelectedRegionLength) {
        prefix = Strings.emptyToNull(prefix);
        StyledCompletionProposal proposal = null;
        if (prefix == null || replacementString.toLowerCase().contains(prefix.toLowerCase())) {
            proposal = new StyledCompletionProposal(this, prefix, offset, preSelectedRegionLength);
        }
        return proposal;
    }

    public String getDisplayString() {
        return displayString;
    }

    public String getReplacementString() {
        return replacementString;
    }

    public String getDescription() {
        return description;
    }

    public String getSelection() {
        return selection;
    }

    public String getType() {
        return type;
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
        if (!(obj instanceof ProposalBuilder)) {
            return false;
        }

        ProposalBuilder other = (ProposalBuilder) obj;

        return Objects.equals(other.displayString, displayString);
    }

}
