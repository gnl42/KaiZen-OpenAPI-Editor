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
package com.reprezen.swagedit.core.hyperlinks;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.utils.DocumentUtils;

public abstract class ReferenceHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    protected final JsonReferenceFactory factory = new JsonReferenceFactory();

    protected abstract JsonFileHyperlink createFileHyperlink(IRegion linkRegion, String label, IFile file,
            JsonPointer pointer);

    @Override
    protected abstract boolean canDetect(JsonPointer pointer);

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        URI baseURI = getBaseURI();

        JsonNode node = doc.asJson().at(pointer);
        JsonReference reference = getFactory().createSimpleReference(getBaseURI(), doc.asJson(), node);
        if (reference == null) {
            reference = getFactory().create(node);
        }

        if (reference.isInvalid() || reference.isMissing(doc, getBaseURI())) {
            return null;
        }

        if (reference.isLocal()) {
            IRegion target = doc.getRegion(reference.getPointer());
            if (target == null) {
                return null;
            }
            return new IHyperlink[] {
                    new SwaggerHyperlink(reference.getPointer().toString(), viewer, info.region, target) };
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
                return new IHyperlink[] { createFileHyperlink(info.region, info.text, file, reference.getPointer()) };
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

}
