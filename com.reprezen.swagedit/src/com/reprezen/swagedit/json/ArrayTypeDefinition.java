package com.reprezen.swagedit.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;

public class ArrayTypeDefinition extends TypeDefinition {

    public final TypeDefinition itemsType;

    public ArrayTypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);

        itemsType = TypeDefinition.create(schema, getItemsPointer());
    }

    private JsonPointer getItemsPointer() {
        String p = getPointer().toString();

        JsonNode node = definition.get("items");

        if (node.isObject() && node.has(JsonReference.PROPERTY)) {
            p = node.get(JsonReference.PROPERTY).asText().substring(1);
        } else {
            p += "/items";
        }

        return JsonPointer.compile(p);
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        return itemsType;
    }
}