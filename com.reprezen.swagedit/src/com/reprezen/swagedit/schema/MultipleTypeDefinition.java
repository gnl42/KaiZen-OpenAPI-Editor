package com.reprezen.swagedit.schema;

import java.util.List;

/**
 * Represents a JSON schema type definition that is defined in separated types.
 * 
 */
public class MultipleTypeDefinition extends TypeDefinition {

    private final List<TypeDefinition> multipleTypes;

    public MultipleTypeDefinition(SwaggerSchema schema, List<TypeDefinition> multipleTypes) {
        super(schema, null, null, JsonType.UNDEFINED);
        this.multipleTypes = multipleTypes;
    }

    public List<TypeDefinition> getMultipleTypes() {
        return multipleTypes;
    }

    @Override
    public String toString() {
        return multipleTypes.toString();
    }
}
