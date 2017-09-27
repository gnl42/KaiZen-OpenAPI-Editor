package com.reprezen.swagedit.openapi3.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Point;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.json.references.JsonDocumentManager;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document;

public class Mocks {

    public static JsonReferenceFactory mockJsonReferenceFactory(final Map<URI, JsonNode> entries) {
        final IFile file = mock(IFile.class);
        when(file.exists()).thenReturn(true);

        return new JsonReferenceFactory() {
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
        ITextViewer viewer = mock(ITextViewer.class);
        ISelectionProvider selectionProvider = mockSelectionProvider();
        ITextSelection selection = mockSelection();

        when(viewer.getDocument()).thenReturn(document);
        when(viewer.getSelectedRange()).thenReturn(new Point(0, 0));
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(selection);
        when(selection.getOffset()).thenReturn(offset);

        return viewer;
    }

    public static ISelectionProvider mockSelectionProvider() {
        return mock(ISelectionProvider.class);
    }

    public static ITextSelection mockSelection() {
        return mock(ITextSelection.class);
    }
}
