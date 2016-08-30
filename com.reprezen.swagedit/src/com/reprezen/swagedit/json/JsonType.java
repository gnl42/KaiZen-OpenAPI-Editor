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
package com.reprezen.swagedit.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enumeration of JSON types found in a JSON schema.
 */
public enum JsonType {
    OBJECT("object"), //
    ARRAY("array"), //
    STRING("string"), //
    NUMBER("number"), //
    INTEGER("integer"), //
    BOOLEAN("boolean"), //
    ONE_OF("oneOf"), //
    ENUM("enum"), //
    ANY_OF("anyOf"), //
    ALL_OF("allOf"), //
    UNDEFINED("undefined");

    private final String value;

    private JsonType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JsonType valueOf(JsonNode node) {
        if (node == null) {
            return JsonType.UNDEFINED;
        } else if (node.has("oneOf")) {
            return JsonType.ONE_OF;
        } else if (node.has("enum")) {
            return JsonType.ENUM;
        } else if (node.has("type")) {
            String type = node.has("type") ? node.get("type").asText() : null;

            if (type != null) {
                for (JsonType jsonType : JsonType.values()) {
                    if (type.equals(jsonType.getValue())) {
                        return jsonType;
                    }
                }
            }
        } else if (node.has("properties")) {
            return JsonType.OBJECT;
        } else if (node.has("anyOf")) {
            return JsonType.ANY_OF;
        }

        return JsonType.UNDEFINED;
    }

    public boolean isValueType() {
        return this == INTEGER || this == JsonType.STRING || this == JsonType.BOOLEAN || this == JsonType.NUMBER;
    }

}
