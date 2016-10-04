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

import java.util.Objects;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

public class Proposal {

    public final String replacementString;
    public final String displayString;
    public final String type;
    public final String description;

    protected final Styler typeStyler = new StyledString.Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
        }
    };

    public Proposal(String replacementString, String displayString, String description, String type) {
        this.replacementString = replacementString;
        this.displayString = displayString;
        this.type = type;
        this.description = description;
    }

    public StyledCompletionProposal asStyledCompletionProposal(String prefix, int offset) {
        StyledString styledString = new StyledString(displayString);
        if (type != null) {
            styledString.append(": ", typeStyler).append(type, typeStyler);
        }

        StyledCompletionProposal p = null;

        if (prefix != null) {
            if (replacementString.startsWith(prefix)) {
                String value = replacementString.substring(prefix.length());
                p = new StyledCompletionProposal(value, styledString, offset, 0, value.length());
            }
        } else {
            p = new StyledCompletionProposal(replacementString, styledString, offset, 0, replacementString.length());
        }

        if (p != null && description != null) {
            p.setDescription(description);
        }

        return p;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((displayString == null) ? 0 : displayString.hashCode());
        result = prime * result + ((replacementString == null) ? 0 : replacementString.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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

        return Objects.equals(other.replacementString, replacementString) //
                && Objects.equals(other.displayString, displayString) //
                && Objects.equals(other.description, description) //
                && Objects.equals(other.type, type);
    }

}
