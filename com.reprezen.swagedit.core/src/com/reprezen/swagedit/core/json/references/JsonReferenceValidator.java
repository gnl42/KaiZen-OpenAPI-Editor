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
package com.reprezen.swagedit.core.json.references;

import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IMarker.SEVERITY_WARNING;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import com.reprezen.swagedit.core.validation.Messages;
import com.reprezen.swagedit.core.validation.SwaggerError;

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
            Map<AbstractNode, JsonReference> references) {
        Set<SwaggerError> errors = Sets.newHashSet();
        for (AbstractNode node : references.keySet()) {
            JsonReference reference = references.get(node);

            if (reference instanceof JsonReference.SimpleReference) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.warning_simple_reference, reference));
            } else if (reference.isInvalid()) {
                errors.add(createReferenceError(SEVERITY_ERROR, Messages.error_invalid_reference, reference));
            } else if (reference.isMissing(doc, baseURI)) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.error_missing_reference, reference));
            } else if (reference.containsWarning()) {
                errors.add(createReferenceError(SEVERITY_WARNING, Messages.error_invalid_reference, reference));
            } else {
                validateType(doc, baseURI, node, reference, errors);
            }
        }
        return errors;
    }

    /**
     * This method checks that referenced objects are of expected type as defined in the schema.
     * 
     * @param doc
     *            current document
     * @param node
     *            node holding the reference
     * @param reference
     *            actual reference
     * @param errors
     *            current set of errors
     */
    private void validateType(JsonDocument doc, URI baseURI, AbstractNode node, JsonReference reference,
            Set<SwaggerError> errors) {
        if (node == null) {
            return;
        }

        Model model = doc.getModel();
        TypeDefinition type = node.getType();

        AbstractNode nodeValue = node.get(JsonReference.PROPERTY);
        String pointer = (String) nodeValue.asValue().getValue();
        AbstractNode valueNode = model.find(pointer);

        if (valueNode == null) {
            // Try to load the referenced node from an external document
            JsonNode externalDoc = reference.getDocument(doc, baseURI);
            if (externalDoc != null) {
                try {
                    Model externalModel = Model.parse(model.getSchema(), externalDoc);

                    int pos = pointer.indexOf("#");
                    if (pos == -1) {
                        valueNode = externalModel.getRoot();
                    } else {
                        valueNode = externalModel.find(pointer.substring(pos, pointer.length()));
                    }

                    if (valueNode == null) {
                        return;
                    }
                } catch (Exception e) {
                    // fail to parse the model or the pointer
                    return;
                }
            }
        }

        if (!type.validate(valueNode)) {
            errors.add(
                    createReferenceError(IMarker.SEVERITY_WARNING, Messages.error_invalid_reference_type, reference));
        }
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
