/*******************************************************************************
 * Copyright (c) 2019 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.validation;

import static com.reprezen.swagedit.core.json.references.JsonReference.isReference;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.model.AbstractNode;

public class ExampleValidator {

    private final JsonDocument document;
    private final JsonReferenceFactory factory = new JsonReferenceFactory();
    private final URI baseURI;

    public ExampleValidator(URI baseURI, JsonDocument document) {
        this.baseURI = baseURI;
        this.document = document;
    }

    public static class ExampleSchemaValidator extends JsonSchemaValidator {

        public ExampleSchemaValidator(JsonNode schema) {
            super(schema, Collections.emptyMap());
        }

    }

    /**
     * Validates the current node if it is an example. A node is an example if it's pointer ends with /example(s). The
     * example node is validated against the schema that is referenced in it's parent node. This validation process will
     * only work if the example is valid JSON.
     * 
     * @param node
     *            - node to validate
     */
    public Set<SwaggerError> validate(AbstractNode node) {
        if (node.getPointerString().matches(".*/example")) {
            if (node.getParent().get("schema") != null) {
                JsonNode example = document.asJson().at(node.getPointer());
                return doValidate(node, node.getParent(), example);
            }
        }

        if (node.getPointerString().matches(".*/examples")) {
            final Set<SwaggerError> errors = new HashSet<>();
            final JsonNode examples = document.asJson().at(node.getPointer());

            examples.fields().forEachRemaining(entry -> {
                JsonNode example = entry.getValue();
                // OpenAPI3 example is under value
                if (example.has("value")) {
                    example = example.get("value");
                }

                errors.addAll(doValidate(node.get(entry.getKey()), node.getParent(), example));
            });

            return errors;
        }

        return Collections.emptySet();
    }

    private Set<SwaggerError> doValidate(AbstractNode node, AbstractNode parent, JsonNode example) {
        final Set<SwaggerError> errors = new HashSet<>();

        JsonNode schema = document.asJson().at(parent.getPointer()).get("schema");
        if (isReference(schema)) {
            schema = factory.create(schema).resolve(document, baseURI);
        }

        normalize(schema);

        new ExampleSchemaValidator(schema).validateSubSchema(example, "").forEach(message -> {
            int line = node.getStart().getLine() + 1;

            errors.add(new SwaggerError(line, getLevel(message.asJson()), message.getMessage()));
        });

        return errors;
    }

    private int getLevel(JsonNode message) {
        if (message == null || !message.has("level")) {
            return IMarker.SEVERITY_INFO;
        }

        switch (message.get("level").asText()) {
        case "error":
        case "fatal":
            return IMarker.SEVERITY_ERROR;
        case "warning":
            return IMarker.SEVERITY_WARNING;
        default:
            return IMarker.SEVERITY_INFO;
        }
    }

    /**
     * This method traverses the root schema object and resolves all references it encounters. This result in a single
     * object in which all references have been normalized.
     * 
     * @param node
     *            - object to normalize
     */
    private void normalize(JsonNode node) {
        node.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();

            if (isReference(value)) {
                JsonReference reference = factory.create(value);
                value = reference.resolve(document, baseURI);

                if (value != null && !value.isMissingNode()) {
                    ((ObjectNode) node).set(entry.getKey(), value);
                }
            }

            normalize(value);
        });
    }

}
