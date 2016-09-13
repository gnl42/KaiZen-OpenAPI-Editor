package com.reprezen.swagedit.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;

public class SwaggerSchema {

    private ObjectMapper mapper = new ObjectMapper();
    protected JsonNode content;
    protected TypeDefinition rootType;

    public SwaggerSchema() {
        init();
    }

    protected void init() {
        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema.json"));
        } catch (IOException e) {
            return;
        }

        JsonNode schema = content;
        rootType = TypeDefinition.create(schema, JsonPointer.compile(""));
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
            for (String path : paths) {

                TypeDefinition next = null;
                if (current instanceof ArrayTypeDefinition) {
                    next = ((ArrayTypeDefinition) current).itemsType;
                } else if (current instanceof ObjectTypeDefinition) {
                    next = current.properties.get(path);

                    if (next == null) {
                        next = ((ObjectTypeDefinition) current).findMatchingPattern(path);
                    }
                }

                // not found, we stop here
                if (next == null) {
                    break;
                }
                current = next;
            }
        }

        return current;
    }

}
