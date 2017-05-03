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
package com.reprezen.swagedit.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reprezen.swagedit.core.editor.JsonEditor;

public class OpenQuickOutlineHandler extends AbstractHandler {

    public static final int QUICK_OUTLINE = 513; // magic number from PDE quick outline see PDEProjectionViewer

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISourceViewer viewer = null;

        Object activeFocusControl = HandlerUtil.getVariable(event, "activeFocusControl"); //$NON-NLS-1$
        if (activeFocusControl instanceof Control) {
            Control control = (Control) activeFocusControl;
            if (!control.isDisposed()) {
                viewer = (ISourceViewer) control.getData(ISourceViewer.class.getName());
            }
        }

        if (viewer == null) {
            IEditorPart editor = HandlerUtil.getActiveEditor(event);
            if (editor instanceof JsonEditor) {
                viewer = ((JsonEditor) editor).getProjectionViewer();
            }
        }

        if (viewer != null) {
            ITextOperationTarget operationTarget = viewer.getTextOperationTarget();
            if (operationTarget.canDoOperation(QUICK_OUTLINE)) {
                operationTarget.doOperation(QUICK_OUTLINE);
            }
        }

        return null;
    }

}
