package com.reprezen.swagedit.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;

public class TypeDefinition {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final Map<String, TypeDefinition> properties = new HashMap<>();
    protected final JsonNode schema;
    protected final JsonNode definition;
    protected final JsonPointer pointer;
    protected final JsonType type;

    public TypeDefinition(JsonNode schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        this.schema = schema;
        this.definition = definition;
        this.pointer = pointer;
        this.type = type;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public JsonPointer getPointer() {
        return pointer;
    }

    public String getContainingProperty() {
        return getProperty(getPointer());
    }

    @Override
    public String toString() {
        return definition.toString();
    }

    protected JsonNode createPropertyProposal(String key, JsonNode value) {
        final JsonNode resolved = resolve(schema, value);
        final JsonType type = JsonType.valueOf(resolved);

        String description = resolved.has("description") ? resolved.get("description").asText() : "";

        return mapper.createObjectNode() //
                .put("value", key + ":") //
                .put("label", key) //
                .put("description", description) //
                .put("type", type.getValue());
    }

    protected Set<JsonNode> createObjectProposal(JsonNode definition, AbstractNode element) {
        Set<JsonNode> proposals = new LinkedHashSet<>();

        if (definition.has("properties")) {
            final JsonNode properties = definition.get("properties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                final String key = it.next();

                if (element.get(key) == null) {
                    proposals.add(createPropertyProposal(key, properties.get(key)));
                }
            }
        }

        if (definition.has("patternProperties")) {
            final JsonNode properties = definition.get("patternProperties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                String key = it.next();
                final JsonNode value = properties.get(key);

                if (key.startsWith("^")) {
                    key = key.substring(1);
                }

                proposals.add(createPropertyProposal(key, value));
            }
        }

        if (proposals.isEmpty()) {
            proposals.add(mapper.createObjectNode() //
                    .put("value", "_key_" + ":") //
                    .put("label", "_key_"));
        }

        return proposals;
    }

    public Set<JsonNode> getProposals(AbstractNode node) {
        Set<JsonNode> proposals = new LinkedHashSet<>();
        JsonType type = JsonType.valueOf(definition);
        if (type == JsonType.STRING || type == JsonType.INTEGER) {

            proposals.add(mapper.createObjectNode().put("value", "") //
                    .put("label", "") //
                    .put("type", "string"));

        } else if (type == JsonType.BOOLEAN) {
            proposals.add(mapper.createObjectNode().put("value", "true").put("label", "true"));
            proposals.add(mapper.createObjectNode().put("value", "false").put("label", "false"));

        } else if (type == JsonType.ENUM) {
            final String subType = definition.has("type") ? definition.get("type").asText() : null;
            for (JsonNode literal : definition.get("enum")) {
                String value = literal.asText();

                // if the type of array is string and
                // current value is a number, it should be put
                // into quotes to avoid validation issues
                if (NumberUtils.isNumber(value) && "string".equals(subType)) {
                    value = "\"" + value + "\"";
                }

                proposals.add(mapper.createObjectNode() //
                        .put("value", value) //
                        .put("label", literal.asText()));
            }
        }

        return proposals;
    }

    public static TypeDefinition create(JsonNode schema, JsonPointer pointer) {
        final JsonNode definition = schema.at(pointer);
        final JsonType type = JsonType.valueOf(definition);

        switch (type) {
        case OBJECT:
            return new ObjectTypeDefinition(schema, pointer, definition, type);
        case ARRAY:
            return new ArrayTypeDefinition(schema, pointer, definition, type);
        case ALL_OF:
        case ANY_OF:
        case ONE_OF:
            return new ComplexTypeDefinition(schema, pointer, definition, type);
        default:
            return new TypeDefinition(schema, pointer, definition, type);
        }
    }

    protected static String getProperty(JsonPointer pointer) {
        String s = pointer.toString();
        return s.substring(s.lastIndexOf("/") + 1).replaceAll("~1", "/");
    }

    protected static JsonNode resolve(JsonNode schema, JsonNode node) {
        if (node.isObject() && node.has("$ref")) {
            JsonPointer p = JsonPointer.compile(node.get("$ref").asText().substring(1));
            return schema.at(p);
        }

        return node;
    }

}
