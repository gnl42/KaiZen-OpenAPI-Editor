package com.reprezen.swagedit.core.validation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.JsonRegion;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ModelValidator {

    private final JsonNode schemaRefTemplate = new ObjectMapper().createObjectNode() //
            .put("$ref", "#/definitions/schema");
    protected final JsonModel model;

    public ModelValidator(JsonModel model) {
        this.model = model;
    }

    /**
     * Validates the model against with different rules that cannot be verified only by JSON schema validation.
     * 
     * @return errors
     */
    public Set<SwaggerError> validateModel() {
        final Set<SwaggerError> errors = new HashSet<>();

        if (model != null) {
            for (JsonPointer ptr : model.getTypes().keySet()) {
                JsonNode node = model.getContent().at(ptr);
                JsonRegion region = model.getRegion(ptr);

                executeModelValidation(node, region, errors);
            }
        }
        return errors;
    }

    protected void executeModelValidation(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        checkArrayTypeDefinition(node, region, errors);
        checkObjectTypeDefinition(node, region, errors);
    }

    /**
     * Validates an object type definition.
     * 
     * @param node
     * @param region
     * @param errors
     */
    private void checkObjectTypeDefinition(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        if (node != null && node.isObject()) {
            if (ValidationUtil.isInDefinition(region.pointer.toString())) {
                checkMissingType(node, region, errors);
                checkMissingRequiredProperties(node, region, errors);
            }
        }
    }

    /**
     * This method checks that the node if an array type definitions includes an items field.
     * 
     * @param node
     * @param region
     * @param errors
     */
    void checkArrayTypeDefinition(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        if (hasArrayType(node)) {
            JsonNode items = node.get("items");
            if (items == null) {
                errors.add(error(region, IMarker.SEVERITY_ERROR, Messages.error_array_missing_items));
            } else {
                if (!items.isObject()) {
                    errors.add(error(getRegion(region, "items"),
                            IMarker.SEVERITY_ERROR, Messages.error_array_items_should_be_object));
                }
            }
        }
    }

    /**
     * Returns true if the node is an array type definition
     * 
     * @param node
     * @return true if array definition
     */
    protected boolean hasArrayType(JsonNode node) {
        if (node.isObject() && node.get("type") != null) {
            String typeValue = node.get("type").asText();
            return "array".equalsIgnoreCase(typeValue);
        }
        return false;
    }

    /**
     * This method checks that the node if an object definition includes a type field.
     * 
     * @param errors
     * @param node
     */
    protected void checkMissingType(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        // object
        if (node.get("properties") != null) {
            // bypass this node, it is a property whose name is `properties`

            // if ("properties".equals(node.getProperty())) {
            // return;
            // }

            if (node.get("type") == null) {
                errors.add(error(region, IMarker.SEVERITY_WARNING, Messages.error_object_type_missing));
            } else {
                JsonNode typeValue = node.get("type");
                if (!(typeValue.isValueNode()) || !Objects.equals("object", typeValue.asText())) {
                    errors.add(error(region, IMarker.SEVERITY_ERROR, Messages.error_wrong_type));
                }
            }
        } else if (isSchemaDefinition(node, null) && node.get("type") == null) {
            errors.add(error(region, IMarker.SEVERITY_WARNING, Messages.error_type_missing));
        }
    }

    private boolean isSchemaDefinition(JsonNode node, TypeDefinition type) {
        // need to use getContent() because asJson() returns resolvedValue is some subclasses
        return type != null && schemaRefTemplate.equals(type.getContent()) //
                && node.get(JsonReference.PROPERTY) == null //
                && node.get("allOf") == null;
    }

    /**
     * This method checks that the required values for the object type definition contains only valid properties.
     * 
     * @param errors
     * @param node
     */
    protected void checkMissingRequiredProperties(JsonNode node, JsonRegion region, Set<SwaggerError> errors) {
        if (node.get("required") != null && node.get("required").isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode required = (com.fasterxml.jackson.databind.node.ArrayNode) node
                    .get("required");

            JsonNode properties = node.get("properties");
            if (properties == null) {
                errors.add(error(region, IMarker.SEVERITY_ERROR, Messages.error_missing_properties));
            } else {
                for (Iterator<JsonNode> it = required.elements(); it.hasNext();) {
                    JsonNode prop = it.next();
                    if (prop.isValueNode()) {
                        String value = prop.asText();

                        if (properties.get(value) == null) {
                            errors.add(error(getRegion(region, value),
                                    IMarker.SEVERITY_ERROR,
                                    String.format(Messages.error_required_properties, value)));
                        }
                    }
                }
            }
        }
    }

    protected SwaggerError error(JsonRegion region, int level, String message) {
        return new SwaggerError(region.getContentLocation().startLine, level, message);
    }

    protected JsonRegion getRegion(JsonRegion parent, String ptr) {
        return model.getRegion(parent.pointer.append(JsonPointer.compile(ptr)));
    }
}
