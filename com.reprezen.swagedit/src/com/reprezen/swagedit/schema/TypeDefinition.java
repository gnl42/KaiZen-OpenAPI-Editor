/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.schema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a type defined inside a JSON Schema.
 *
 */
public class TypeDefinition {

    protected final SwaggerSchema schema;
    protected final JsonNode content;
    protected final JsonPointer pointer;
    protected final JsonType type;

    public TypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        this.schema = schema;
        this.content = definition;
        this.pointer = pointer;
        this.type = type;

        schema.add(this);
    }

    public JsonType getType() {
        return type;
    }

    public JsonNode asJson() {
        return content;
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

    public TypeDefinition getPropertyType(String property) {
        return null;
    }

    /**
     * 
     * @return
     */
    public String getDescription() {
        if (content == null) {
            return null;
        }

        if (!content.has("description")) {
            return null;
        }

        return content.get("description").asText();
    }

    @Override
    public String toString() {
        return content.toString();
    }

    /**
     * Returns the type reachable by the given pointer inside the given schema.
     * 
     * @param schema
     * @param pointer
     * @return type
     */
    public static TypeDefinition create(SwaggerSchema schema, JsonPointer pointer) {
        if (schema.getType(pointer) != null) {
            return schema.getType(pointer);
        }

        final JsonNode definition = schema.asJson().at(pointer);
        if (definition == null || definition.isMissingNode()) {
            return null;
        }

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

}
