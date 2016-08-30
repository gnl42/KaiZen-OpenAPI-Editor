package com.reprezen.swagedit.json;

import static com.google.common.collect.Iterators.find;

import java.util.Iterator;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Predicate;
import com.reprezen.swagedit.json.JsonSchemaManager.JSONSchema;
import com.reprezen.swagedit.model.AbstractNode;

public class JsonType2 {

    public JsonNode definition;
    public JsonType2 parent;
    public String containingProperty;
    public JsonSchemaManager.JSONSchema schema;

    public static class ObjectType extends JsonType2 {

    }

    public static class ArrayType extends JsonType2 {
        public JsonNode items;
        public JsonType itemsType;
    }

    @Override
    public String toString() {
        return definition.toString();
    }

    /**
     * Returns the type of the current node by resolving it from the swagger schema.
     * 
     * @param node
     * @param pointer
     * @param baseType
     * @return the node's type
     */
    public static JsonType2 findType(AbstractNode node, JsonPointer pointer, JsonType2 baseType) {
        if (node == null)
            return null;

        AbstractNode parent = node.getParent();
        if (parent == null) {
            return baseType;
        }

        JsonType2 parentType = parent.type2;
        String property = getProperty(pointer);

        if (parentType instanceof ArrayType) {
            JsonType itemsType = ((ArrayType) parentType).itemsType;
            JsonNode items = ((ArrayType) parentType).items;

            if (itemsType.isValueType() || itemsType == JsonType.ENUM) {
                JsonType2 type = new JsonType2();
                type.schema = parentType.schema;
                type.containingProperty = property;
                type.definition = items;
                type.parent = parentType;

                return type;
            } else {
                JsonNode correctType = oneOf(parentType.schema, items, node);
                if (correctType != null) {
                    JsonType2 type = new JsonType2();
                    type.schema = parentType.schema;
                    type.containingProperty = property;
                    type.parent = parentType;
                    type.definition = correctType;

                    return type;
                }
            }
        }

        JsonNode definition = findDefinition(parentType, property);

        if (JsonType.valueOf(definition) == JsonType.ARRAY) {
            JsonNode items = resolve(parentType.schema, definition.get("items"));

            ArrayType type = new ArrayType();
            type.schema = parentType.schema;
            type.containingProperty = property;
            type.definition = definition;
            type.parent = parentType;
            type.itemsType = JsonType.valueOf(items);
            type.items = items;

            return type;
        } else if (JsonType.valueOf(definition) == JsonType.ONE_OF || JsonType.valueOf(definition) == JsonType.ALL_OF
                || JsonType.valueOf(definition) == JsonType.ANY_OF) {

            // TODO
            return null;
        } else if (JsonType.valueOf(definition) == JsonType.OBJECT) {

            ObjectType type = new ObjectType();
            type.schema = parentType.schema;
            type.containingProperty = property;
            type.definition = definition;
            type.parent = parentType;

            return type;
        } else {

            JsonType2 type = new JsonType2();
            type.schema = parentType.schema;
            type.containingProperty = property;
            type.definition = definition;
            type.parent = parentType;

            return type;
        }
    }

    private static JsonNode findDefinition(JsonType2 parentType, final String property) {
        JsonNode parentDefinition = parentType.definition;
        JsonNode definition = null;
        if (parentDefinition.has("properties")) {
            definition = parentType.definition.get("properties").get(property);
        }

        if (definition == null && parentDefinition.has("patternProperties")) {
            Iterator<String> patterns = parentDefinition.get("patternProperties").fieldNames();

            definition = parentType.definition.get("patternProperties").get(find(patterns, //
                    new Predicate<String>() {
                        @Override
                        public boolean apply(String s) {
                            return property.matches(s);
                        }
                    }, null));
        }

        if (definition != null) {
            definition = resolve(parentType.schema, definition);
        }

        return definition;
    }

    protected static JsonNode oneOf(JSONSchema schema, JsonNode node, AbstractNode actual) {
        Iterator<JsonNode> it = node.get("oneOf").elements();
        JsonNode found = null;
        while (it.hasNext() && found == null) {
            JsonNode current = resolve(schema, it.next());
            JsonType tt = JsonType.valueOf(current);

            if (tt == JsonType.ONE_OF) {
                found = oneOf(schema, current, actual);
            } else {
                found = objectOf(schema, current, actual);
            }
        }

        return found;
    }

    private static JsonNode objectOf(JSONSchema schema, JsonNode current, AbstractNode actual) {
        // TODO
        // better resolution of type, it cannot be done by looking only
        // on first property present, see parameters for example where multiple
        // types have same properties
        if (current.has("properties")) {
            for (Iterator<String> it = current.get("properties").fieldNames(); it.hasNext();) {
                String property = it.next();
                if (actual.isObject()) {
                    if (actual.asObject().get(property) != null) {
                        return current;
                    }
                }
            }
        }
        return null;
    }

    protected static String getProperty(JsonPointer pointer) {
        String s = pointer.toString();
        return s.substring(s.lastIndexOf("/") + 1).replaceAll("~1", "/");
    }

    protected static JsonNode resolve(JSONSchema schema, JsonNode node) {
        if (node.isObject() && node.has("$ref")) {
            JsonPointer p = JsonPointer.compile(node.get("$ref").asText().substring(1));
            return schema.asJson().at(p);
        }
        return node;
    }

}
