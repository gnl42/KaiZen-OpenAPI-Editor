package com.reprezen.swagedit.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;

public class ComplexTypeDefinition extends TypeDefinition {

    private Collection<TypeDefinition> allTypes = new HashSet<>();

    public ComplexTypeDefinition(final SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);

        for (Iterator<JsonNode> it = definition.get(type.getValue()).elements(); it.hasNext();) {
            JsonNode current = it.next();
            if (JsonReference.isReference(current)) {
                JsonPointer p = JsonPointer.compile(current.get(JsonReference.PROPERTY).asText().substring(1));
                TypeDefinition def = schema.getType(p);
                if (def == null) {
                    def = TypeDefinition.create(getSchema(), p);
                }
                allTypes.add(def);
            }
        }
    }

    public Collection<TypeDefinition> getAllTypes() {
        return allTypes;
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        TypeDefinition found = null;

        for (TypeDefinition type : allTypes) {
            found = type.getPropertyType(property);
            if (found != null) {
                return found;
            }
        }

        return found;
    }
}