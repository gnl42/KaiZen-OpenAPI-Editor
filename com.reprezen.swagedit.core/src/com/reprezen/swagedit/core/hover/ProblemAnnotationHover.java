package com.reprezen.swagedit.core.hover;

import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationHoverExtension2;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;

public class ProblemAnnotationHover extends AbstractProblemHover
        implements IAnnotationHover, IAnnotationHoverExtension, IAnnotationHoverExtension2 {

    public ProblemAnnotationHover(ISourceViewer sourceViewer) {
        super(sourceViewer);
    }

    @Override
    public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
        return null;
    }

    @Override
    public boolean canHandleMouseWheel() {
        return false;
    }

    @Override
    public boolean canHandleMouseCursor() {
        return false;
    }

    @Override
    public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines) {
        List<Annotation> annotations = getAnnotations(lineRange.getStartLine(), -1);
        // JsonQuickAssistProcessor processor = new JsonQuickAssistProcessor();

        AnnotationInfo result = annotations.stream() //
                .filter(ann -> ann.getText() != null) //
                .map(ann -> {
                    Position position = getAnnotationModel().getPosition(ann);
                    // ICompletionProposal[] proposals = processor
                    // .computeQuickAssistProposals(new IQuickAssistInvocationContext() {
                    // @Override
                    // public ISourceViewer getSourceViewer() {
                    // return sourceViewer;
                    // }
                    //
                    // @Override
                    // public int getOffset() {
                    // return lineRange.;
                    // }
                    //
                    // @Override
                    // public int getLength() {
                    // return hoverRegion.getLength();
                    // }
                    // });
                    return new AnnotationInfo(ann, position, sourceViewer, new ICompletionProposal[] {});
                }) //
                .findFirst() //
                .orElse(null);

        return result;
    }

    @Override
    public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
        return new LineRange(lineNumber, 1);
    }

}
