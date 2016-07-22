package com.reprezen.swagedit.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenQuickOutlineHandler extends AbstractHandler {

    public static final int QUICK_OUTLINE = 513; // magic number from PDE quick outline see PDEProjectionViewer

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Object activeFocusControl = HandlerUtil.getVariable(event, "activeFocusControl"); //$NON-NLS-1$
        if (activeFocusControl instanceof Control) {
            Control control = (Control) activeFocusControl;
            if (!control.isDisposed()) {
                ISourceViewer viewer = (ISourceViewer) control.getData(ISourceViewer.class.getName());
                if (viewer != null) {
                    ITextOperationTarget operationTarget = viewer.getTextOperationTarget();
                    if (operationTarget.canDoOperation(QUICK_OUTLINE)) {
                        operationTarget.doOperation(QUICK_OUTLINE);
                    }
                }
            }
        }
        return null;
    }

}
