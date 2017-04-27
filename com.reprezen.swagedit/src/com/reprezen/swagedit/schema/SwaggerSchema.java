package com.reprezen.swagedit.schema;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reprezen.swagedit.preferences.SwaggerPreferenceConstants;

public class SwaggerSchema extends CompositeSchema {

    private JsonSchema coreType;
    private final Map<String, JsonNode> jsonRefContexts = Maps.newHashMap();

    private final JsonNode refToJsonReferenceNode = mapper.createObjectNode().put("$ref",
            "#/definitions/jsonReference");

    public SwaggerSchema() {

        JsonNode core;
        try {
            core = mapper.readTree(getClass().getResourceAsStream("core.json"));
        } catch (IOException e) {
            return;
        }

        JsonNode content;
        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema.json"));
        } catch (IOException e) {
            return;
        }

        coreType = new JsonSchema(core, this);
        coreType.setType(new ObjectTypeDefinition(coreType, JsonPointer.compile(""), core));

        swaggerType = new JsonSchema(content, this);
        swaggerType.setType(new ObjectTypeDefinition(swaggerType, JsonPointer.compile(""), content));
        try {
            JsonNode definitionsNode = asJson().get("definitions");

            jsonRefContexts.put(SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT,
                    definitionsNode.get("securityDefinitions"));

            jsonRefContexts.put(SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_SCHEME_OBJECT,
                    (ArrayNode) definitionsNode.get("securityDefinitions").get("additionalProperties").get("oneOf"));

            jsonRefContexts.put(SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY,
                    (ArrayNode) definitionsNode.get("security").get("oneOf"));

            jsonRefContexts.put(SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT,
                    (ArrayNode) definitionsNode.get("security").get("oneOf").get(0).get("items").get("oneOf"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void allowJsonRefInContext(String jsonReferenceContext, boolean allow) {
        if (jsonRefContexts.get(jsonReferenceContext) == null) {
            throw new IllegalArgumentException("Invalid JSON Reference Context: " + jsonReferenceContext);
        }
        // special case
        if (SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT.equals(jsonReferenceContext)) {
            allowJsonRefInSecurityDefinitionsObject(allow);
            return;
        }
        ArrayNode definition = (ArrayNode) jsonRefContexts.get(jsonReferenceContext);
        // should preserve order of the original ArrayNode._children
        List<JsonNode> children = Lists.newArrayList(definition.elements());
        int indexOfJsonReference = children.indexOf(refToJsonReferenceNode);
        boolean alreadyHasJsonReference = indexOfJsonReference > -1;
        if (allow) {
            if (!alreadyHasJsonReference) {
                definition.add(refToJsonReferenceNode.deepCopy());
            }
        } else { // disallow
            if (alreadyHasJsonReference) {
                definition.remove(indexOfJsonReference);
            }
        }
    }

    protected void allowJsonRefInSecurityDefinitionsObject(boolean allow) {
        ObjectNode definition = (ObjectNode) jsonRefContexts
                .get(SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT);
        if (allow) {
            ObjectNode propertiesNode = definition.putObject("properties");
            propertiesNode.putObject("$ref").put("type", "string");
        } else {
            definition.remove("properties");
        }
    }

}
