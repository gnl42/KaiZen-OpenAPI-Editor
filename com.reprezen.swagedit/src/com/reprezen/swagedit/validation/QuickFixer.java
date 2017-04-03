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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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

    public static class FixMissingObjectType implements IMarkerResolution {

        public String getLabel() {
            return "Set object type to schema definition";
        }

        public void run(IMarker marker) {
            IDocument document = getDocument(marker);
            if (document == null) {
                return;
            }
            int line;
            try {
                line = (int) marker.getAttribute(IMarker.LINE_NUMBER);
            } catch (CoreException e) {
                // TODO log
                return;
            }
            // TODO add a new line if it's at the end of the document
            int nextLineOffset;
            try {
                nextLineOffset = document.getLineOffset(line);
                String definitionLine = document.get(document.getLineOffset(line - 1),
                        document.getLineLength(line - 1));
                // TODO use definitionLine to calculate indent
                String indent = "    ";
                document.replace(nextLineOffset, 0,
                        indent + "type: object" + TextUtilities.getDefaultLineDelimiter(document));
            } catch (BadLocationException e1) {
                // TODO log
                return;
            }
        }

        protected IDocument getDocument(IMarker marker) {
            IResource resource = marker.getResource();
            if (resource.getType() != IResource.FILE) {
                // TODO log
                return null;
            }
            IFile file = (IFile) resource;
            ITextEditor editor = openTextEditor(file);
            if (editor == null) {
                return null;
            }
            return editor.getDocumentProvider().getDocument(new FileEditorInput(file));
        }

        protected ITextEditor openTextEditor(IFile file) {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorPart part;
            try {
                part = IDE.openEditor(page, file, true);
            } catch (PartInitException e1) {
                // log error
                return null;
            }
            return part instanceof ITextEditor ? (ITextEditor) part : null;
        }
    }

}
