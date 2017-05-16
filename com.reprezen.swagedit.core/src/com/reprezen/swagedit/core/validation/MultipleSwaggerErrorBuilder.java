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
package com.reprezen.swagedit.core.validation;

import static com.google.common.collect.Iterators.transform;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.reprezen.swagedit.core.validation.SwaggerError.MultipleSwaggerError;

public class MultipleSwaggerErrorBuilder {

    private int line;
    private int severity;
    private int indent;
    private JsonNode jsonSchema;
    private final Map<String, Set<SwaggerError>> errors = new HashMap<>();

    public MultipleSwaggerErrorBuilder locatedOn(int line) {
        this.line = line;
        return this;
    }

    public MultipleSwaggerErrorBuilder withSeverity(int severity) {
        this.severity = severity;
        return this;
    }

    public MultipleSwaggerErrorBuilder indented(int indent) {
        this.indent = indent;
        return this;
    }

    public MultipleSwaggerErrorBuilder basedOnSchema(JsonNode jsonSchema) {
        this.jsonSchema = jsonSchema;
        return this;
    }

    public MultipleSwaggerErrorBuilder withErrorsOnPath(String path, Set<SwaggerError> errors) {
        this.errors.put(path, errors);
        return this;
    }

    public MultipleSwaggerError build() {
        return new MultipleSwaggerError(line, severity, indent, getMessage(), errors);
    }

    protected String getMessage() {
        Set<String> orderedErrorLocations = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (errors.get(o1).size() != errors.get(o2).size()) {
                    return errors.get(o1).size() - errors.get(o2).size();
                }
                return o1.compareTo(o2);
            }
        });
        orderedErrorLocations.addAll(errors.keySet());

        final StringBuilder builder = new StringBuilder();
        final String tabs = Strings.repeat("\t", indent);

        builder.append(tabs);
        builder.append("Failed to match exactly one schema:");
        builder.append("\n");

        for (String location : orderedErrorLocations) {
            builder.append(tabs);
            builder.append(" - ");
            builder.append(getHumanFriendlyText(location));
            builder.append(":");
            builder.append("\n");

            for (SwaggerError e : errors.get(location)) {
                builder.append(e.getIndentedMessage());
            }
        }

        return builder.toString();
    }

    protected String getHumanFriendlyText(String location) {
        JsonNode swaggerSchemaNode = ValidationUtil.findNode(location, jsonSchema);
        if (swaggerSchemaNode == null) {
            return location;
        }
        return getHumanFriendlyText(swaggerSchemaNode, location);
    }

    protected String getHumanFriendlyText(JsonNode swaggerSchemaNode, final String defaultValue) {
        JsonNode title = swaggerSchemaNode.get("title");
        if (title != null) {
            return title.asText();
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

    private String getLabelForRef(String refValue) {
        return refValue.substring(refValue.lastIndexOf("/") + 1);
    }

}
