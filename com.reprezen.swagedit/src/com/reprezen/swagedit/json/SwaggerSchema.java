package com.reprezen.swagedit.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;

public class SwaggerSchema {

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode content;
    private TypeDefinition rootType;
    private final Map<JsonPointer, TypeDefinition> types = new HashMap<>();

    public SwaggerSchema() {
        init();
    }

    protected void init() {
        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema.json"));
        } catch (IOException e) {
            return;
        }

        rootType = TypeDefinition.create(this, JsonPointer.compile(""));
    }

    public JsonNode asJson() {
        return content;
    }

    public TypeDefinition getType(AbstractNode node) {
        JsonPointer pointer = node.getPointer();

        if (JsonPointer.compile("").equals(pointer)) {
            return rootType;
        }

        String[] paths = pointer.toString().substring(1).split("/");
        TypeDefinition current = rootType;

        if (current != null) {
            for (String property : paths) {
                TypeDefinition next = current.getPropertyType(property);

                // not found, we stop here
                if (next == null) {
                    break;
                }
                current = next;
            }
        }

        return current;
    }

    public TypeDefinition getRootType() {
        return rootType;
    }

    public TypeDefinition getType(JsonPointer pointer) {
        return types.get(pointer);
    }

    public void add(TypeDefinition typeDef) {
        types.put(typeDef.getPointer(), typeDef);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}
