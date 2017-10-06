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

import static com.google.common.collect.Iterators.transform;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

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
                    Iterator<String> labels = transform(arrayNode.elements(), new Function<JsonNode, String>() {

                        @Override
                        public String apply(JsonNode el) {
                            return getHumanFriendlyText(el, defaultValue);
                        }
                    });
                    return "[" + Joiner.on(", ").join(labels) + "]";
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
