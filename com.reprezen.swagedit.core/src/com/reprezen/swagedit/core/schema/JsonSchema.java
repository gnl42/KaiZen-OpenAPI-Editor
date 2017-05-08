package com.reprezen.swagedit.core.schema;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.reprezen.swagedit.core.json.references.JsonReference;

public class JsonSchema {

    private final Map<JsonPointer, TypeDefinition> types = new HashMap<>();
    private final String id;
    private final JsonNode content;
    private final CompositeSchema manager;

    private ObjectTypeDefinition type;

    public JsonSchema(JsonNode content, CompositeSchema manager) {
        this.id = content.get("id").asText().replaceAll("#", "");
        this.content = content;
        this.manager = manager;
        this.manager.schemas.put(id, this);
    }

    public String getId() {
        return id;
    }

    public CompositeSchema getManager() {
        return manager;
    }

    public void setType(ObjectTypeDefinition type) {
        this.type = type;
    }

    public ObjectTypeDefinition getType() {
        return type;
    }

    /**
     * Creates a new type after resolving it from the pointer.
     * 
     * @param pointer
     * @return type
     */
    protected TypeDefinition createType(JsonPointer pointer) {
        final JsonNode definition = content.at(pointer);
        if (definition == null || definition.isMissingNode()) {
            return null;
        }

        TypeDefinition typeDef;
        if (JsonReference.isReference(definition)) {
            typeDef = new ReferenceTypeDefinition(this, pointer, definition);
        } else {
            final JsonType type = JsonType.valueOf(definition);
            switch (type) {
            case OBJECT:
                typeDef = new ObjectTypeDefinition(this, pointer, definition);
                break;
            case ARRAY:
                typeDef = new ArrayTypeDefinition(this, pointer, definition);
                break;
            case ALL_OF:
            case ANY_OF:
            case ONE_OF:
                typeDef = new ComplexTypeDefinition(this, pointer, definition, type);
                break;
            default:
                typeDef = new TypeDefinition(this, pointer, definition, type);
            }
        }
        types.put(pointer, typeDef);
        return typeDef;
    }

    protected TypeDefinition createType(TypeDefinition parent, String property, JsonNode definition) {
        return createType(JsonPointer.compile(parent.getPointer() + "/" + property));
    }

    public JsonNode resolve(JsonPointer pointer) {
        return content.at(pointer);
    }

    public TypeDefinition get(JsonPointer pointer) {
        if (pointer == null || Strings.emptyToNull(pointer.toString()) == null) {
            return type;
        }
        return types.get(pointer);
    }

}