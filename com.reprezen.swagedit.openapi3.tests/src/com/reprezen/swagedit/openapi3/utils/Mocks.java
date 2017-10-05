package com.reprezen.swagedit.openapi3.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Point;

import com.reprezen.swagedit.openapi3.editor.OpenApi3Document;

public class Mocks {

    public static ITextViewer mockTextViewer(OpenApi3Document document) {
        ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(document);
        return viewer;
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
