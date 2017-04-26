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

import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.io.CharStreams;
import com.reprezen.swagedit.common.editor.JsonDocument;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.utils.DocumentUtils;

public class SwaggerFileHyperlink implements IHyperlink {

    private final IRegion linkRegion;
    private final String label;
    private final IFile file;
    private final JsonPointer pointer;

    public SwaggerFileHyperlink(IRegion linkRegion, String label, IFile file, JsonPointer pointer) {
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
            DocumentUtils.openAndReveal(file, getTarget());
        } catch (Exception e) {
            // TODO
        }
    }

    private IRegion getTarget() throws CoreException {
        JsonDocument doc = null;
        try {
            String content = DocumentUtils.getDocumentContent(file.getLocation());
            doc = new SwaggerDocument();
            doc.set(content);
        } catch (IOException e) {
            return null;
        }
        if (doc == null) {
            return null;
        }

        return doc.getRegion(pointer);
    }

}
