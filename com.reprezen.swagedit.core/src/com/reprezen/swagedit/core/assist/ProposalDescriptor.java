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

public class ProposalDescriptor {

    private final String displayString;
    private String replacementString;
    private String type;
    private String description;
    private String selection;

    public ProposalDescriptor(String displayString) {
        // displayString is used for equality in #equals() and #hashCode()
        this.displayString = displayString;
    }

    public ProposalDescriptor replacementString(String replacementString) {
        this.replacementString = replacementString;
        return this;
    }

    public ProposalDescriptor type(String type) {
        this.type = type;
        return this;
    }

    public ProposalDescriptor selection(String selection) {
        this.selection = selection;
        return this;
    }

    public ProposalDescriptor description(String description) {
        this.description = description;
        return this;
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
        if (!(obj instanceof ProposalDescriptor)) {
            return false;
        }

        ProposalDescriptor other = (ProposalDescriptor) obj;

        return Objects.equals(other.displayString, displayString);
    }

    @Override
    public String toString() {
        return "ProposalDescriptor [displayString=" + displayString + ", replacementString=" + replacementString
                + ", type=" + type + ", description=" + description + ", selection=" + selection + "]";
    }

}
