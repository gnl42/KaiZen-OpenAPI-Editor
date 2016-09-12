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
package com.reprezen.swagedit.validation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class SwaggerError {

    public String schema;
    public String schemaPointer;
    public String instancePointer;
    public String keyword;
    public String message;

    public int level;
    public int line;
    public int indent = 0;

    public SwaggerError(int line, int level, String message) {
        this.line = line;
        this.level = level;
        this.message = message;
    }

    public SwaggerError(int level, String message) {
        this(1, level, message);
    }

    public SwaggerError(YAMLException exception) {
        this.level = IMarker.SEVERITY_ERROR;
        this.message = exception.getMessage();

        if (exception instanceof MarkedYAMLException) {
            this.line = ((MarkedYAMLException) exception).getProblemMark().getLine() + 1;
        } else {
            this.line = 1;
        }
    }

    public SwaggerError(JsonProcessingException exception) {
        this.level = IMarker.SEVERITY_ERROR;
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

        public MultipleSwaggerError(int line, int level) {
            super(line, level, null);
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
                    builder.append(e.getMessage(true));
                }
            }

            return builder.toString();
        }

        /* package */String getHumanFriendlyText(String location) {
            JsonNode swaggerSchemaNode = findNode(location);
            if (swaggerSchemaNode == null) {
                return location;
            }
            return getHumanFriendlyText(swaggerSchemaNode, location);
        }

        /* package */String getHumanFriendlyText(JsonNode swaggerSchemaNode, String defaultValue) {
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
            return defaultValue;
        }

        /* package */String getLabelForRef(String refValue) {
            return refValue.substring(refValue.lastIndexOf("/") + 1);
        }

        protected JsonNode findNode(String path) {
            // TODO
            // JsonNode result = findNode(Lists.newLinkedList(Arrays.asList(path.split("/"))), swaggerSchema);
            JsonNode result = findNode(Lists.newLinkedList(Arrays.asList(path.split("/"))), null);
            return result;
        }

        protected JsonNode findNode(LinkedList<String> path, JsonNode root) {
            if (root == null) {
                return null;
            }
            // retrieves the first element, and also *removes* it
            String firstSegment = path.pop();
            if (Strings.isNullOrEmpty(firstSegment)) {
                return findNode(path, root);
            }
            int firstSegmentAsNumber = -1;
            try {
                firstSegmentAsNumber = Integer.parseInt(firstSegment);
            } catch (NumberFormatException e) {
                // ignore
            }
            JsonNode nodeForSegment = firstSegmentAsNumber == -1 ? root.get(firstSegment) : root
                    .get(firstSegmentAsNumber);
            if (path.isEmpty()) {
                return nodeForSegment;
            }
            return findNode(path, nodeForSegment);
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
