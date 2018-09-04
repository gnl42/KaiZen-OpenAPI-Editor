/*******************************************************************************
 * Copyright (c) 2017 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonSchemaUtils {
    public static String getHumanFriendlyText(JsonNode swaggerSchemaNode, final String defaultValue) {
        String schemaTitle = getSchemaTitle(swaggerSchemaNode);
        if (schemaTitle != null) {
            return schemaTitle;
        }
        // nested array
        if (swaggerSchemaNode.get("items") != null) {
            return getHumanFriendlyText(swaggerSchemaNode.get("items"), defaultValue);
        }
        // "$ref":"#/definitions/headerParameterSubSchema"
        JsonNode ref = swaggerSchemaNode.get("$ref");
        if (ref != null) {
            return getLabelForRef(ref.asText());
        }
        // Auxiliary oneOf in "oneOf": [ { "$ref": "#/definitions/securityRequirement" }]
        JsonNode oneOf = swaggerSchemaNode.get("oneOf");
        if (oneOf != null) {
            if (oneOf instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) oneOf;
                if (arrayNode.size() > 0) {
                    List<String> labels = new ArrayList<>();
                    arrayNode.elements().forEachRemaining(el -> labels.add(getHumanFriendlyText(el, defaultValue)));
                    StringJoiner joiner = new StringJoiner(", ", "[", "]");
                    labels.forEach(joiner::add);
                    return joiner.toString();
                }
            }
        }
        return defaultValue;
    }

    public static String getSchemaTitle(JsonNode swaggerSchemaNode) {
        JsonNode title = swaggerSchemaNode.get("title");
        if (title != null) {
            return title.asText();
        }
        return null;
    }

    public static String getLabelForRef(String refValue) {
        return refValue.substring(refValue.lastIndexOf("/") + 1);
    }
}
