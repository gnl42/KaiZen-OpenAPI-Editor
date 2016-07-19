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
package com.reprezen.swagedit.editor.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;

public class SwaggerFileHyperlink implements IHyperlink {

    private final IRegion linkRegion;
    private final String label;
    private final IFile file;
    private final String pointer;

    public SwaggerFileHyperlink(IRegion linkRegion, String label, IFile file, String pointer) {
        this.linkRegion = linkRegion;
        this.label = label;
        this.file = file;
        this.pointer = pointer;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return linkRegion;
    }

    @Override
    public String getTypeLabel() {
        return label;
    }

    @Override
    public String getHyperlinkText() {
        return label;
    }

    @Override
    public void open() {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            IEditorPart editor;
            try {
                editor = IDE.openEditor(page, file);
            } catch (PartInitException e) {
                return;
            }

            if (editor instanceof ITextEditor) {
                IRegion region = getTarget();
                if (region != null) {
                    ((ITextEditor) editor).selectAndReveal(region.getOffset(), region.getLength());
                }
            }
        } catch (ClassCastException | CoreException e) {
            // TODO
        }
    }

    private IRegion getTarget() throws CoreException {
        SwaggerDocument doc = DocumentUtils.getDocument(file.getLocation());
        if (doc == null) {
            return null;
        }

        return doc.getRegion(pointer);
    }

}
