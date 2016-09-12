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

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.references.JsonReference;
import com.reprezen.swagedit.json.references.JsonReferenceFactory;

/**
 * Hyperlink detector that detects links from JSON references.
 * 
 */
public class JsonReferenceHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

    protected final JsonReferenceFactory factory = new JsonReferenceFactory();

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().endsWith("$ref");
    }

    @Override
    protected IHyperlink[] doDetect(SwaggerDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        URI baseURI = getBaseURI();

        JsonReference reference = getFactory().create(doc.getModel().find(pointer));
        if (reference.isInvalid() || reference.isMissing(getBaseURI())) {
            return null;
        }

        if (reference.isLocal()) {
            IRegion target = doc.getRegion(reference.getPointer());
            if (target == null) {
                return null;
            }
            return new IHyperlink[] { new SwaggerHyperlink(reference.getPointer().toString(), viewer, info.region,
                    target) };
        } else {
            URI resolved;
            try {
                resolved = baseURI.resolve(reference.getUri());
            } catch (IllegalArgumentException e) {
                // the given string violates RFC 2396
                return null;
            }
            IFile file = DocumentUtils.getWorkspaceFile(resolved);
            if (file != null && file.exists()) {
                return new IHyperlink[] { new SwaggerFileHyperlink(info.region, info.text, file,
                        reference.getPointer()) };
            }
        }

        return null;
    }

    protected FileEditorInput getActiveEditor() {
        return DocumentUtils.getActiveEditorInput();
    }

    protected URI getBaseURI() {
        FileEditorInput editor = getActiveEditor();

        return editor != null ? editor.getURI() : null;
    }

    protected JsonReferenceFactory getFactory() {
        return factory;
    }

    protected String pointer(JsonPointer pointer) {
        return pointer.toString().replaceAll("/", ":").replaceAll("~1", "/");
    }

}
