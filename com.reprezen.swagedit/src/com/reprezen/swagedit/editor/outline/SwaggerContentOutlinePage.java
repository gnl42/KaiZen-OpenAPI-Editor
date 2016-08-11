package com.reprezen.swagedit.editor.outline;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.model.Model;

public class SwaggerContentOutlinePage extends ContentOutlinePage {

    private final IDocumentProvider documentProvider;
    private final IShowInTarget showInTarget;

    private Object currentInput;

    public SwaggerContentOutlinePage(IDocumentProvider documentProvider, IShowInTarget showInTarget) {
        super();
        this.documentProvider = documentProvider;
        this.showInTarget = showInTarget;
    }

    public void setInput(Object input) {
        this.currentInput = input;
        update();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new OutlineContentProvider());
        viewer.setLabelProvider(new OutlineStyledLabelProvider());
        viewer.addSelectionChangedListener(this);
        viewer.setAutoExpandLevel(2);

        if (currentInput != null) {
            setInput(currentInput);
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        super.selectionChanged(event);

        showInTarget.show(new ShowInContext(null, event.getSelection()));
    }

    protected void update() {
        IDocument document = documentProvider.getDocument(currentInput);
        if (document instanceof SwaggerDocument) {
            Model model = ((SwaggerDocument) document).getModel();

            TreeViewer viewer = getTreeViewer();
            if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
                viewer.setInput(model);
            }
        }
    }
}
