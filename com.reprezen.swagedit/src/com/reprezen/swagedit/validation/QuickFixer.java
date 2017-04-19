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
package com.reprezen.swagedit.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dadacoalition.yedit.preferences.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.base.Strings;
import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.Messages;

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
                document.replace(endOfCurrLine, 0, TextUtilities.getDefaultLineDelimiter(document) + replacementText);
                return new Region(endOfCurrLine + 1, replacementText.length());
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

    public abstract static class TextDocumentMarkerResolution implements IMarkerResolution {

        /**
         * @return IRegion to be selected in the editor, can be null
         * @throws CoreException
         */
        public abstract IRegion processFix(IDocument document, IMarker marker) throws CoreException;

        public void run(IMarker marker) {
            try {
                IResource resource = marker.getResource();
                if (resource.getType() != IResource.FILE) {
                    throw new CoreException(createStatus(null, "The editor is not a File: " + resource.getName()));
                }
                IFile file = (IFile) resource;
                ITextEditor editor = openTextEditor(file);
                IDocument document = editor.getDocumentProvider().getDocument(new FileEditorInput(file));
                if (document == null) {
                    throw new CoreException(createStatus(null, "The document is null"));
                }
                IRegion region = processFix(document, marker);
                if (region != null) {
                    editor.selectAndReveal(region.getOffset(), region.getLength());
                }
            } catch (CoreException e) {
                Activator.getDefault().getLog().log(e.getStatus());
            }
        }

        protected ITextEditor openTextEditor(IFile file) throws CoreException {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorPart part;
            try {
                part = IDE.openEditor(page, file, true);
            } catch (PartInitException e) {
                throw new CoreException(createStatus(e, "Cannot open editor"));
            }
            if (!(part instanceof ITextEditor)) {
                throw new CoreException(createStatus(null, "The editor is not TextEditor: " + part));
            }
            return (ITextEditor) part;
        }

        protected IStatus createStatus(Exception e, String msg) {
            return new Status(Status.ERROR, Activator.PLUGIN_ID, "Cannot process the quick fix: " + msg, e);
        }
    }

}
