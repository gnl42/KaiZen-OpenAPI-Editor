package com.reprezen.swagedit.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

public class TypeDefinition {

    protected final Map<String, TypeDefinition> properties = new HashMap<>();
    protected final SwaggerSchema schema;
    protected final JsonNode definition;
    protected final JsonPointer pointer;
    protected final JsonType type;

    public TypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        this.schema = schema;
        this.definition = definition;
        this.pointer = pointer;
        this.type = type;

        schema.add(this);
    }

    public JsonType getType() {
        return type;
    }

    public JsonNode getDefinition() {
        return definition;
    }

    public SwaggerSchema getSchema() {
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

    public static TypeDefinition create(SwaggerSchema schema, JsonPointer pointer) {
        if (schema.getType(pointer) != null) {
            return schema.getType(pointer);
        }

        final JsonNode definition = schema.asJson().at(pointer);
        final JsonType type = JsonType.valueOf(definition);

        TypeDefinition typeDef;
        switch (type) {
        case OBJECT:
            typeDef = new ObjectTypeDefinition(schema, pointer, definition, type);
            break;
        case ARRAY:
            typeDef = new ArrayTypeDefinition(schema, pointer, definition, type);
            break;
        case ALL_OF:
        case ANY_OF:
        case ONE_OF:
            typeDef = new ComplexTypeDefinition(schema, pointer, definition, type);
            break;
        default:
            typeDef = new TypeDefinition(schema, pointer, definition, type);
        }

        return typeDef;
    }

    protected static String getProperty(JsonPointer pointer) {
        String s = pointer.toString();
        return s.substring(s.lastIndexOf("/") + 1).replaceAll("~1", "/");
    }

    protected static JsonNode resolve(SwaggerSchema schema, JsonNode node) {
        if (node.isObject() && node.has("$ref")) {
            JsonPointer p = JsonPointer.compile(node.get("$ref").asText().substring(1));
            return schema.asJson().at(p);
        }

        return node;
    }

    public TypeDefinition getPropertyType(String property) {
        return properties.get(property);
    }

}
