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
package com.reprezen.swagedit.core.schema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a type defined inside a JSON Schema.
 *
 */
public class TypeDefinition {

    protected final JsonSchema schema;
    public final JsonNode content;
    protected final JsonPointer pointer;
    protected final JsonType type;

    public TypeDefinition(JsonSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        this.schema = schema;
        this.content = definition;
        this.pointer = pointer;
        this.type = type;
    }

    public JsonType getType() {
        return type;
    }

    public JsonNode asJson() {
        return content;
    }

    public JsonSchema getSchema() {
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
    
    public JsonNode getContent() {
        return content;
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
        return "( " + type + " " + content.toString() + " )";
    }

    protected static String getProperty(JsonPointer pointer) {
        String s = pointer.toString();
        return s.substring(s.lastIndexOf("/") + 1).replaceAll("~1", "/");
    }

}
