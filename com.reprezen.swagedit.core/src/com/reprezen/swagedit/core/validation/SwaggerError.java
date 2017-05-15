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

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class SwaggerError {

    private final String message;

    private final int level;
    private final int line;
    private final int indent;

    public SwaggerError(int line, int level, String message) {
        this(line, level, 0, message);
    }
    
    public SwaggerError(int line, int level, int indent, String message) {
        this.line = line;
        this.level = level;
        this.indent = indent;
        this.message = message;
    }

    public SwaggerError(int level, String message) {
        this(1, level, message);
    }

    public SwaggerError(YAMLException exception) {
        this.level = IMarker.SEVERITY_ERROR;
        this.message = exception.getMessage();
        this.indent = 0;

        if (exception instanceof MarkedYAMLException) {
            this.line = ((MarkedYAMLException) exception).getProblemMark().getLine() + 1;
        } else {
            this.line = 1;
        }
    }

    public SwaggerError(JsonProcessingException exception) {
        this.level = IMarker.SEVERITY_ERROR;
        this.indent = 0;
        this.message = exception.getMessage();

        if (exception.getLocation() != null) {
            this.line = exception.getLocation().getLineNr();
        } else {
            this.line = 1;
        }
    }

    public String getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }

    public int getLine() {
        return line;
    }

    String getMessage(boolean withIndent) {
        if (withIndent) {
            final StringBuilder builder = new StringBuilder();
            builder.append(Strings.repeat("\t", indent));
            builder.append(" - ");
            builder.append(message);
            builder.append("\n");

            return builder.toString();
        }

        return message;
    }
    
    protected int getIndent() {
        return indent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + line;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SwaggerError other = (SwaggerError) obj;
        if (level != other.level)
            return false;
        if (line != other.line)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

    public static class MultipleSwaggerError extends SwaggerError {

        private final Map<String, Set<SwaggerError>> errors = new HashMap<>();
        private final JsonNode jsonSchema;

        public MultipleSwaggerError(int line, int level, int indent, JsonNode JsonSchema) {
            super(line, level, indent, null);
            jsonSchema = JsonSchema;
        }

        public void put(String path, Set<SwaggerError> errors) {
            this.errors.put(path, errors);
        }

        public Map<String, Set<SwaggerError>> getErrors() {
            return errors;
        }

        @Override
        String getMessage(boolean withIndent) {
            return getMessage();
        }

        @Override
        public String getMessage() {
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
            final String tabs = Strings.repeat("\t", getIndent());

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
                    builder.append(e.getMessage(true));
                }
            }

            return builder.toString();
        }

        public String getHumanFriendlyText(String location) {
            JsonNode swaggerSchemaNode = ValidationUtil.findNode(location, jsonSchema);
            if (swaggerSchemaNode == null) {
                return location;
            }
            return getHumanFriendlyText(swaggerSchemaNode, location);
        }
        
        public String getHumanFriendlyText(JsonNode swaggerSchemaNode, final String defaultValue) {
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

        /* package */String getLabelForRef(String refValue) {
            return refValue.substring(refValue.lastIndexOf("/") + 1);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((errors == null) ? 0 : errors.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultipleSwaggerError other = (MultipleSwaggerError) obj;
            if (errors == null) {
                if (other.errors != null)
                    return false;
            } else if (!errors.equals(other.errors))
                return false;
            return true;
        }

    }

}
