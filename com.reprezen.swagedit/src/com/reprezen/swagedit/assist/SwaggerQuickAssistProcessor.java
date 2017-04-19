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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import com.google.common.collect.Lists;

public class SwaggerQuickAssistProcessor implements IQuickAssistProcessor {
    private final IMarkerResolutionGenerator2 quickFixer;
    private String errorMessage;

    public SwaggerQuickAssistProcessor(IMarkerResolutionGenerator2 quickFixer) {
        this.quickFixer = quickFixer;
    }

    @Override
    public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
        return false;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean canFix(Annotation annotation) {
        if (annotation.isMarkedDeleted()) {
            return false;
        }
        if (annotation instanceof MarkerAnnotation) {
            MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
            if (!markerAnnotation.isQuickFixableStateSet()) {
                markerAnnotation.setQuickFixable(quickFixer.hasResolutions(markerAnnotation.getMarker()));
            }
            return markerAnnotation.isQuickFixable();
        }
        return false;
    }

    @Override
    public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
        List<IMarker> markers;
        try {
            markers = getMarkersFor(invocationContext.getSourceViewer(), invocationContext.getOffset());
        } catch (BadLocationException e) {
            errorMessage = e.getMessage();
            return new ICompletionProposal[0];
        }
        List<ICompletionProposal> result = Lists.newArrayList();
        for (IMarker marker : markers) {
            for (IMarkerResolution markerResolution : quickFixer.getResolutions(marker)) {
                result.add(new MarkerResolutionProposal(marker, markerResolution));
            }
        }
        return result.toArray(new ICompletionProposal[0]);
    }

    protected List<IMarker> getMarkersFor(ISourceViewer sourceViewer, int offset) throws BadLocationException {
        final IDocument document = sourceViewer.getDocument();

        int line = document.getLineOfOffset(offset);
        int lineOffset = document.getLineOffset(line);

        String delim = document.getLineDelimiter(line);
        int delimLength = delim != null ? delim.length() : 0;
        int lineLength = document.getLineLength(line) - delimLength;

        return getMarkersFor(sourceViewer, lineOffset, lineLength);
    }

    protected List<IMarker> getMarkersFor(ISourceViewer sourceViewer, int lineOffset, int lineLength) {
        List<IMarker> result = Lists.newArrayList();
        IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
        Iterator annotationIter = annotationModel.getAnnotationIterator();
        while (annotationIter.hasNext()) {
            Object annotation = annotationIter.next();
            if (annotation instanceof MarkerAnnotation) {
                MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
                IMarker marker = markerAnnotation.getMarker();
                Position markerPosition = annotationModel.getPosition(markerAnnotation);
                if (markerPosition != null && markerPosition.overlapsWith(lineOffset, lineLength)) {
                    result.add(marker);
                }
            }
        }
        return result;
    }

    public static class MarkerResolutionProposal implements ICompletionProposal {

        private final IMarker marker;
        private final IMarkerResolution markerResolution;

        public MarkerResolutionProposal(IMarker marker, IMarkerResolution markerResolution) {
            this.marker = marker;
            this.markerResolution = markerResolution;
        }

        @Override
        public void apply(IDocument document) {
            markerResolution.run(marker);
        }

        @Override
        public Point getSelection(IDocument document) {
            return null;
        }

        @Override
        public String getAdditionalProposalInfo() {
            return null;
        }

        @Override
        public String getDisplayString() {
            return markerResolution.getLabel();
        }

        @Override
        public Image getImage() {
            return null;
        }

        @Override
        public IContextInformation getContextInformation() {
            return null;
        }

    }

}