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
package com.reprezen.swagedit.json.references;

import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IMarker.SEVERITY_WARNING;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.reprezen.swagedit.common.editor.JsonDocument;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.validation.SwaggerError;

/**
 * JSON Reference Validator
 */
public class JsonReferenceValidator {

    private final JsonReferenceCollector collector;

    public JsonReferenceValidator(JsonReferenceFactory factory) {
        this.collector = new JsonReferenceCollector(factory);
    }

    /**
     * Returns a collection containing all errors being invalid JSON references present in the Swagger document.
     * 
     * @param baseURI
     * @param document
     * @return collection of errors
     */
    public Collection<? extends SwaggerError> validate(URI baseURI, JsonDocument doc) {
        return doValidate(baseURI, doc, collector.collect(baseURI, doc.getModel()));
    }

    protected Collection<? extends SwaggerError> doValidate(URI baseURI, JsonDocument doc,
            Iterable<JsonReference> references) {
        Set<SwaggerError> errors = Sets.newHashSet();
        for (JsonReference reference : references) {
            if (reference instanceof JsonReference.SimpleReference) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.warning_simple_reference, reference));
            } else if (reference.isInvalid()) {
                errors.add(createReferenceError(SEVERITY_ERROR, Messages.error_invalid_reference, reference));
            } else if (reference.isMissing(doc, baseURI)) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.error_missing_reference, reference));
            } else if (reference.containsWarning()) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.error_invalid_reference, reference));
            }
        }
        return errors;
    }

    protected SwaggerError createReferenceError(int severity, String message, JsonReference reference) {
        Object source = reference.getSource();
        int line;
        if (source instanceof AbstractNode) {
            line = ((AbstractNode) source).getStart().getLine() + 1;
        } else {
            line = 1;
        }

        return new SwaggerError(line, severity, message);
    }

}
