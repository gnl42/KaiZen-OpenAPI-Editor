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
package com.reprezen.swagedit.openapi3.validation;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.validation.SwaggerError;

public class OpenApi3ReferenceValidator extends JsonReferenceValidator {

    private final JsonPointer linkTypePointer = JsonPointer.compile("/definitions/linkOrReference");
    private final JsonPointer operationTypePointer = JsonPointer.compile("/definitions/operation");

    public OpenApi3ReferenceValidator() {
        super(new OpenApi3ReferenceFactory());
    }

    OpenApi3ReferenceValidator(OpenApi3ReferenceFactory factory) {
        super(factory);
    }

    @Override
    protected void validateType(JsonDocument doc, URI baseURI, JsonNode node, JsonReference reference,
            Set<SwaggerError> errors) {

        // if (linkTypePointer.equals(node.getType().getPointer())) {
        // JsonNode target = findTarget(doc, baseURI, reference);
        //
        // if (factory != null) {
        // JsonSchema jsonSchema;
        // try {
        // jsonSchema = factory.getJsonSchema(doc.getSchema().asJson(), operationTypePointer.toString());
        // ProcessingReport report = jsonSchema.validate(target);
        // if (!report.isSuccess()) {
        // errors.add(createReferenceError(IMarker.SEVERITY_WARNING, Messages.error_invalid_operation_ref,
        // reference));
        // }
        // } catch (ProcessingException e) {
        // e.printStackTrace();
        // }
        // }
        // } else {
            super.validateType(doc, baseURI, node, reference, errors);
        // }
    }

    // protected boolean isValidOperation(JsonNode operation) {
    // TypeDefinition type = operation != null ? operation.getType() : null;
    //
    // return type != null && Objects.equals(operationTypePointer, type.getPointer());
    // }

    public static class OpenApi3ReferenceFactory extends JsonReferenceFactory {

        private static final String OPERATION_REF = "operationRef";

        @Override
        protected Boolean isReference(JsonNode node) {
            return super.isReference(node) || node.get(OPERATION_REF) != null;
        }

        // @Override
        // protected JsonNode getReferenceValue(JsonNode node) {
        // JsonNode valueNode = super.getReferenceValue(node);
        // if (valueNode == null) {
        // AbstractNode other = node.get(OPERATION_REF);
        // if (other != null && other.isValue()) {
        // valueNode = other.asValue();
        // }
        // }
        // return valueNode;
        // }
    }
}