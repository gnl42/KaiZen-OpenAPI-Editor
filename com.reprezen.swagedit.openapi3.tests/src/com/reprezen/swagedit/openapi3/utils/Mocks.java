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
package com.reprezen.swagedit.openapi3.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Point;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.json.references.JsonDocumentManager;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document;
import com.reprezen.swagedit.openapi3.validation.OpenApi3ReferenceValidator;
import com.reprezen.swagedit.openapi3.validation.OpenApi3ReferenceValidator.OpenApi3ReferenceFactory;

public class Mocks {

    public static ITextViewer mockTextViewer(OpenApi3Document document) {
        ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(document);
        return viewer;
    }

    public static OpenApi3ReferenceFactory mockJsonReferenceFactory(final Map<URI, JsonNode> entries) {
        final IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);

        return new OpenApi3ReferenceValidator.OpenApi3ReferenceFactory() {
            public JsonReference create(AbstractNode node) {
                JsonReference ref = super.create(node);
                ref.setDocumentManager(new JsonDocumentManager() {
                    @Override
                    public IFile getFile(URI uri) {
                        return file;
                    }

                    @Override
                    public JsonNode getDocument(URI uri) {
                        return entries.get(uri);
                    }
                });
                return ref;
            };
        };
    }

    public static ITextViewer mockTextViewer(OpenApi3Document document, int offset) {
        return mockTextViewer(document, offset, "");
    }
    
    public static ITextViewer mockTextViewer(OpenApi3Document document, int selectionOffset, String selectionText) {
        ITextViewer viewer = mock(ITextViewer.class);
        ISelectionProvider selectionProvider = mockSelectionProvider();
        ITextSelection selection = mockSelection();

        when(viewer.getDocument()).thenReturn(document);
        when(viewer.getSelectedRange()).thenReturn(new Point(0, 0));
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(selection);
        when(selection.getOffset()).thenReturn(selectionOffset);
        when(selection.getText()).thenReturn(selectionText);
        when(selection.getLength()).thenReturn(selectionText.length());

        return viewer;
    }

    public static ISelectionProvider mockSelectionProvider() {
        return mock(ISelectionProvider.class);
    }

    public static ITextSelection mockSelection() {
        return mock(ITextSelection.class);
    }
    
    public static IFile mockJsonReferenceProposalFile() {
        IFile file = mock(IFile.class);
        IContainer parent = mock(IContainer.class);
        IPath parentPath = mock(IPath.class);

        when(file.getParent()).thenReturn(parent);
        when(parent.getFullPath()).thenReturn(parentPath);

        return file;
    }
}
