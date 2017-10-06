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
package com.reprezen.swagedit.core.hyperlinks;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class SwaggerHyperlink implements IHyperlink {

    private final String text;
    private final IRegion region;
    private final IRegion target;
    private final ITextViewer viewer;

    public SwaggerHyperlink(String text, ITextViewer viewer, IRegion region, IRegion target) {
        this.text = text;
        this.viewer = viewer;
        this.region = region;
        this.target = target;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    @Override
    public String getTypeLabel() {
        return text;
    }

    @Override
    public String getHyperlinkText() {
        return text;
    }

    public IRegion getTarget() {
        return target;
    }

    @Override
    public void open() {
        if (viewer != null) {
            viewer.setSelectedRange(target.getOffset(), target.getLength());
            viewer.revealRange(target.getOffset(), target.getLength());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SwaggerHyperlink [text=");
        builder.append(text);
        builder.append(", region=");
        builder.append(region);
        builder.append(", target=");
        builder.append(target);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((region == null) ? 0 : region.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        SwaggerHyperlink other = (SwaggerHyperlink) obj;

        if (region == null) {
            if (other.region != null)
                return false;
        } else if (!region.equals(other.region))
            return false;

        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;

        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;

        return true;
    }

}
