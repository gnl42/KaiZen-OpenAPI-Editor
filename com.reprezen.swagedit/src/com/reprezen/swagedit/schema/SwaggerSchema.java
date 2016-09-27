package com.reprezen.swagedit.schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;

/**
 * Represents the Swagger Schema.
 * 
 */
public class SwaggerSchema {

    private final ObjectMapper mapper = new ObjectMapper();

    // private JsonNode core;
    private JsonNode content;

    private TypeDefinition rootType;
    private final Map<JsonPointer, TypeDefinition> types = new HashMap<>();

    public SwaggerSchema() {
        init();
    }

    protected void init() {
        // try {
        // core = mapper.readTree(getClass().getResourceAsStream("core.json"));
        // } catch (IOException e) {
        // return;
        // }

        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema.json"));
        } catch (IOException e) {
            return;
        }

        rootType = TypeDefinition.create(this, JsonPointer.compile(""));
    }

    /**
     * Returns the content of the schema as JSON.
     * 
     * @return schema content
     */
    public JsonNode asJson() {
        return content;
    }

    /**
     * Returns the type of a node.
     * 
     * <br/>
     * 
     * Note: this method should be used only during initialization of a model.
     * 
     * @param node
     * @return node's type
     */
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

    /**
     * Returns the schema root type.
     * 
     * @return root type
     */
    public TypeDefinition getRootType() {
        return rootType;
    }

    /**
     * Returns the type that is reachable by the given pointer inside the JSON schema. <br/>
     * 
     * Examples of pointers: <br/>
     * - /properties/swagger <br/>
     * - /definitions/paths
     * 
     * @param pointer
     * @return
     */
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
