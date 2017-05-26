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
package com.reprezen.swagedit.core.quickfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dadacoalition.yedit.preferences.PreferenceConstants;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import com.google.common.base.Strings;
import com.reprezen.swagedit.core.validation.Messages;

public class QuickFixer implements IMarkerResolutionGenerator2 {

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker) {
        if (isMissingObjectType(marker)) {
            return new IMarkerResolution[] { new FixMissingObjectType() };
        }
        return new IMarkerResolution[0];
    }

    @Override
    public boolean hasResolutions(IMarker marker) {
        return isMissingObjectType(marker);
    }

    private boolean isMissingObjectType(IMarker marker) {
        try {
            return Messages.error_object_type_missing.equals(marker.getAttribute(IMarker.MESSAGE));
        } catch (CoreException e) {
            return false;
        }
    }

    public static class FixMissingObjectType extends TextDocumentMarkerResolution {
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)\\S.*", Pattern.DOTALL);

        public String getLabel() {
            return "Set object type to schema definition";
        }

        @Override
        public IRegion processFix(IDocument document, IMarker marker) throws CoreException {
            int line = (int) marker.getAttribute(IMarker.LINE_NUMBER);
            try {
                String indent = getIndent(document, line);
                // getLineOffset() is zero-based, and imarkerLine is one-based.
                int endOfCurrLine = document.getLineInformation(line - 1).getOffset()
                        + document.getLineInformation(line - 1).getLength();
                // should be fine for first and last lines in the doc as well
                String replacementText = indent + "type: object";
                String delim = TextUtilities.getDefaultLineDelimiter(document);
                document.replace(endOfCurrLine, 0, delim + replacementText);
                return new Region(endOfCurrLine + delim.length(), replacementText.length());
            } catch (BadLocationException e) {
                throw new CoreException(createStatus(e, "Cannot process the IMarker"));
            }
        }

        protected String getIndent(IDocument document, int line) throws BadLocationException {
            String definitionLine = document.get(document.getLineOffset(line - 1), document.getLineLength(line - 1));
            Matcher m = WHITESPACE_PATTERN.matcher(definitionLine);
            final String definitionIndent = m.matches() ? m.group(1) : "";
            return definitionIndent + Strings.repeat(" ", getTabWidth());
        }

        private int getTabWidth() {
            IPreferenceStore prefs = org.dadacoalition.yedit.Activator.getDefault().getPreferenceStore();
            return prefs.getInt(PreferenceConstants.SPACES_PER_TAB);
        }
    }

}
