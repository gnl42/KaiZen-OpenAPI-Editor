/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.validation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.ArrayNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.validation.Messages;
import com.reprezen.swagedit.core.validation.SwaggerError;
import com.reprezen.swagedit.core.validation.Validator;

public class OpenApi3Validator extends Validator {

    private final JsonPointer operationPointer = JsonPointer.compile("/definitions/operation");
    private final JsonPointer securityPointer = JsonPointer.compile("/components/securitySchemes");

    public OpenApi3Validator(JsonReferenceValidator referenceValidator, Map<String, JsonNode> preloadedSchemas) {
        super(referenceValidator, preloadedSchemas);
    }

    @Override
    protected void executeModelValidation(Model model, AbstractNode node, Set<SwaggerError> errors) {
        super.executeModelValidation(model, node, errors);
        validateOperationIdReferences(model, node, errors);
        validateOperationRefReferences(model, node, errors);
        validateSecuritySchemeReferences(model, node, errors);
        validateParameters(model, node, errors);
    }

    private void validateSecuritySchemeReferences(Model model, AbstractNode node, Set<SwaggerError> errors) {
        if (node.getPointerString().matches(".*/security/\\d+")) {
            AbstractNode securitySchemes = model.find(securityPointer);

            if (node.isObject()) {
                for (String field : node.asObject().fieldNames()) {
                    AbstractNode securityScheme = securitySchemes.get(field);

                    if (securityScheme == null) {
                        String message = Messages.error_invalid_reference_type
                                + " It should be a valid security scheme.";

                        errors.add(error(node.get(field), IMarker.SEVERITY_ERROR, message));
                    } else {
                        validateSecuritySchemeScopes(node, field, securityScheme, errors);
                    }
                }
            }
        }
    }

    private List<String> oauthScopes = Lists.newArrayList("oauth2", "openIdConnect");

    private void validateSecuritySchemeScopes(AbstractNode node, String name, AbstractNode securityScheme,
            Set<SwaggerError> errors) {
        String type = getType(securityScheme);
        if (type == null) {
            return;
        }

        boolean shouldHaveScopes = oauthScopes.contains(type);
        List<String> scopes = getSecurityScopes(securityScheme);

        AbstractNode values = node.get(name);
        if (values.isArray()) {
            ArrayNode scopeValues = values.asArray();

            if (scopeValues.size() > 0 && !shouldHaveScopes) {
                String message = String.format(Messages.error_scope_should_be_empty, name, type, name);

                errors.add(error(node.get(name), IMarker.SEVERITY_ERROR, message));
            } else if (scopeValues.size() == 0 && shouldHaveScopes) {
                String message = String.format(Messages.error_scope_should_not_be_empty, name, type);

                errors.add(error(node.get(name), IMarker.SEVERITY_ERROR, message));
            } else {
                for (AbstractNode scope : scopeValues.elements()) {
                    try {
                        String scopeName = (String) scope.asValue().getValue();
                        if (!scopes.contains(scopeName)) {
                            String message = String.format(Messages.error_invalid_scope_reference, scopeName, name);

                            errors.add(error(scope, IMarker.SEVERITY_ERROR, message));
                        }
                    } catch (Exception e) {
                        // Invalid scope name type.
                        // No need to create an error, it will be handle by the schema validation.
                    }
                }
            }
        }
    }

    private String getType(AbstractNode securityScheme) {
        AbstractNode type = securityScheme.get("type");
        if (type == null) {
            return null;
        }

        return (String) type.asValue().getValue();
    }

    private List<String> getSecurityScopes(AbstractNode securityScheme) {
        List<String> scopes = Lists.newArrayList();

        try {
            AbstractNode flows = securityScheme.get("flows");
            for (AbstractNode flow : flows.elements()) {
                AbstractNode values = flow.get("scopes");
                if (values != null && values.isObject()) {
                    scopes.addAll(values.asObject().fieldNames());
                }
            }
        } catch (Exception e) {
            // could be a NPE, let's just return the scopes we have so far.
        }
        return scopes;
    }

    private void validateOperationRefReferences(Model model, AbstractNode node, Set<SwaggerError> errors) {
        JsonPointer schemaPointer = JsonPointer.compile("/definitions/link/properties/operationRef");

        if (node != null && node.getType() != null && schemaPointer.equals(node.getType().getPointer())) {
            String operationRefPointer = (String) node.asValue().getValue();
            AbstractNode operation = model.find(operationRefPointer);

            if (operation == null) {
                errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference));
            } else if (operation.getType() == null
                    || !Objects.equals(operationPointer, operation.getType().getPointer())) {
                errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference_type));
            }
        }
    }

    protected void validateOperationIdReferences(Model model, AbstractNode node, Set<SwaggerError> errors) {
        JsonPointer schemaPointer = JsonPointer.compile("/definitions/link/properties/operationId");

        if (node != null && node.getType() != null && schemaPointer.equals(node.getType().getPointer())) {
            List<AbstractNode> nodes = model.findByType(operationPointer);
            Iterator<AbstractNode> it = nodes.iterator();

            boolean found = false;
            while (it.hasNext() && !found) {
                AbstractNode current = it.next();
                AbstractNode value = current.get("operationId");

                found = value != null && Objects.equals(node.asValue().getValue(), value.asValue().getValue());
            }

            if (!found) {
                errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference_type));
            }
        }
    }

    protected void validateParameters(Model model, AbstractNode node, Set<SwaggerError> errors) {
        final JsonPointer pointer = JsonPointer.compile("/definitions/parameterOrReference");

        if (node != null && node.getType() != null && pointer.equals(node.getType().getPointer())) {
            // validation parameter location value
            if (node.isObject() && node.asObject().get("in") != null) {
                AbstractNode valueNode = node.asObject().get("in");
                try {
                    Object value = valueNode.asValue().getValue();

                    if (!Lists.newArrayList("query", "header", "path", "cookie").contains(value)) {
                        errors.add(error(valueNode, IMarker.SEVERITY_ERROR, Messages.error_invalid_parameter_location));
                    }
                } catch (Exception e) {
                    errors.add(error(valueNode, IMarker.SEVERITY_ERROR, Messages.error_invalid_parameter_location));
                }
            }
        }
    }
}
