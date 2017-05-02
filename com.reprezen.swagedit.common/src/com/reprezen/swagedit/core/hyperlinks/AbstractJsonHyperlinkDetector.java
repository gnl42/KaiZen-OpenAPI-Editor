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

import static com.google.common.base.Strings.emptyToNull;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.common.editor.JsonDocument;

public abstract class AbstractJsonHyperlinkDetector extends AbstractHyperlinkDetector {

    /**
     * Contains details about potential hyperlinks inside a JSON document.
     */
    protected static class HyperlinkInfo {

        public final IRegion region;
        public final String text;
        public final int column;

        HyperlinkInfo(IRegion region, String text, int column) {
            this.region = region;
            this.text = text;
            this.column = column;
        }

        public int getOffset() {
            return region.getOffset();
        }

        public int getLength() {
            return region.getLength();
        }

        public String getText() {
            return text;
        }

        public String getUnquotedText() {
            return text.replaceAll("'|\"", "");
        }
    }

    @Override
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        JsonDocument document = (JsonDocument) textViewer.getDocument();
        JsonPointer basePath = document.getPath(region);

        if (!canDetect(basePath)) {
            return null;
        }

        HyperlinkInfo info = getHyperlinkInfo(textViewer, region);
        if (info == null) {
            return null;
        }

        return doDetect(document, textViewer, info, basePath);
    }

    protected abstract boolean canDetect(JsonPointer pointer);

    protected abstract IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info,
            JsonPointer pointer);

    protected HyperlinkInfo getHyperlinkInfo(ITextViewer viewer, IRegion region) {
        final JsonDocument document = (JsonDocument) viewer.getDocument();
        IRegion line;
        try {
            line = document.getLineInformationOfOffset(region.getOffset());
        } catch (BadLocationException e) {
            return null;
        }

        String lineContent;
        try {
            lineContent = document.get(line.getOffset(), line.getLength());
        } catch (BadLocationException e) {
            return null;
        }

        if (lineContent == null || emptyToNull(lineContent) == null) {
            return null;
        }

        final int column = region.getOffset() - line.getOffset();
        final IRegion selected = getSelectedRegion(line, lineContent, column);
        String text;
        try {
            text = document.get(selected.getOffset(), selected.getLength());
        } catch (BadLocationException e) {
            return null;
        }

        if (emptyToNull(text) == null || text.trim().equals(":") || text.trim().equals("$ref:")) {
            return null;
        }

        return new HyperlinkInfo(selected, text, column);
    }

    /**
     * Returns the region containing the word selected or under the user's cursor.
     * 
     * @param region
     * @param content
     * @param column
     * @return Region
     */
    protected Region getSelectedRegion(IRegion region, String content, int column) {
        // find next space or if not
        // after position is end of content.
        int end = content.indexOf(" ", column);
        if (end == -1) {
            end = content.length();
        }

        // find previous space
        // if not, start is 0.
        int start = 0;
        int idx = 0;
        do {
            idx = content.indexOf(" ", idx);
            if (idx != -1) {
                idx++;
                if (column >= idx) {
                    start = idx;
                }
            }
        } while (start < column && idx != -1);

        int offset = region.getOffset() + start;
        int length = end - start;

        return new Region(offset, length);
    }

}
