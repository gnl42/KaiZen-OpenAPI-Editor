package com.reprezen.swagedit.openapi3.validation;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.JsonRegion;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import com.reprezen.swagedit.core.validation.Messages;
import com.reprezen.swagedit.core.validation.ModelValidator;
import com.reprezen.swagedit.core.validation.SwaggerError;

public class OpenApi3ModelValidator extends ModelValidator {

    private final JsonPointer operationPointer = JsonPointer.compile("/definitions/operation");
    private final JsonPointer securityPointer = JsonPointer.compile("/components/securitySchemes");
    private final List<String> oauthScopes = Lists.newArrayList("oauth2", "openIdConnect");

    public OpenApi3ModelValidator(JsonModel model) {
        super(model);
    }

    @Override
    protected void executeModelValidation(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        super.executeModelValidation(node, region, errors);
        validateSecuritySchemeReferences(node, region, errors);
        validateOperationIdReferences(node, region, errors);
        validateParameters(node, region, errors);
    }

    void validateOperationIdReferences(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        final JsonPointer schemaPointer = JsonPointer.compile("/definitions/link/properties/operationId");
        final TypeDefinition definition = model.getTypes().get(region.pointer);

        if (node != null && definition != null && schemaPointer.equals(definition.getPointer())) {
            List<JsonNode> nodes = model.findByType(operationPointer);
            Iterator<JsonNode> it = nodes.iterator();

            boolean found = false;
            while (it.hasNext() && !found) {
                JsonNode current = it.next();
                JsonNode value = current.get("operationId");

                found = value != null && Objects.equals(node.asText(), value.asText());
            }

            if (!found) {
                errors.add(error(region, IMarker.SEVERITY_ERROR, Messages.error_invalid_operation_id));
            }
        }
    }

    void validateParameters(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        final JsonPointer pointer = JsonPointer.compile("/definitions/parameterOrReference");
        final TypeDefinition definition = model.getTypes().get(region.pointer);

        if (node != null && definition != null && pointer.equals(definition.getPointer())) {
            // validation parameter location value
            if (node.isObject() && node.has("in")) {
                JsonNode valueNode = node.get("in");
                try {
                    Object value = valueNode.asText();

                    if (!Lists.newArrayList("query", "header", "path", "cookie").contains(value)) {
                        errors.add(error(getRegion(region, "in"), IMarker.SEVERITY_ERROR,
                                Messages.error_invalid_parameter_location));
                    }
                } catch (Exception e) {
                    errors.add(error(getRegion(region, "in"), IMarker.SEVERITY_ERROR,
                            Messages.error_invalid_parameter_location));
                }
            }
        }
    }

    void validateSecuritySchemeReferences(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        if (region.pointer.toString().matches(".*/security/\\d+")) {
            JsonNode securitySchemes = model.getContent().at(securityPointer);

            if (node.isObject()) {
                for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                    String field = it.next();
                    JsonNode securityScheme = securitySchemes.get(field);

                    if (securityScheme == null) {
                        String message = Messages.error_invalid_reference_type
                                + " It should be a valid security scheme.";

                        errors.add(error(getRegion(region, field), IMarker.SEVERITY_ERROR, message));
                    } else {
                        validateSecuritySchemeScopes(node, region, field, securityScheme, errors);
                    }
                }
            }
        }
    }


    private void validateSecuritySchemeScopes(JsonNode node, JsonRegion region, String name, JsonNode securityScheme,
            Set<SwaggerError> errors) {
        String type = getType(securityScheme);
        if (type == null) {
            return;
        }

        boolean shouldHaveScopes = oauthScopes.contains(type);
        List<String> scopes = getSecurityScopes(securityScheme);

        JsonNode values = node.get(name);
        if (values.isArray()) {
            if (values.size() > 0 && !shouldHaveScopes) {
                String message = String.format(Messages.error_scope_should_be_empty, name, type, name);

                errors.add(error(getRegion(region, name), IMarker.SEVERITY_ERROR, message));
            } else if (values.size() == 0 && shouldHaveScopes) {
                String message = String.format(Messages.error_scope_should_not_be_empty, name, type);

                errors.add(error(getRegion(region, name), IMarker.SEVERITY_ERROR, message));
            } else {
                for (int i = 0; i < values.size(); i++) {
                    JsonNode scope = values.get(i);
                    try {
                        String scopeName = scope.asText();
                        if (!scopes.contains(scopeName)) {
                            String message = String.format(Messages.error_invalid_scope_reference, scopeName, name);
                            errors.add(
                                    error(getRegion(region, "/name/" + i + "/scope"), IMarker.SEVERITY_ERROR, message));
                        }
                    } catch (Exception e) {
                        // Invalid scope name type.
                        // No need to create an error, it will be handle by the schema validation.
                    }
                }
            }
        }
    }

    private String getType(JsonNode securityScheme) {
        JsonNode type = securityScheme.get("type");
        if (type == null) {
            return null;
        }

        return type.asText();
    }

    private List<String> getSecurityScopes(JsonNode securityScheme) {
        List<String> scopes = Lists.newArrayList();

        try {
            JsonNode flows = securityScheme.get("flows");
            for (Iterator<JsonNode> it = flows.elements(); it.hasNext();) {
                JsonNode flow = it.next();
                JsonNode values = flow.get("scopes");
                if (values != null && values.isObject()) {
                    scopes.addAll(Lists.newArrayList(values.fieldNames()));
                }
            }
        } catch (Exception e) {
            // could be a NPE, let's just return the scopes we have so far.
        }
        return scopes;
    }

}
