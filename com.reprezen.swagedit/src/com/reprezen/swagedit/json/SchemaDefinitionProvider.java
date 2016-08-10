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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.JsonSchemaManager.JSONSchema;

//
// From a path, returns set of definitions
//
public class SchemaDefinitionProvider {

    private final JsonSchemaManager schemaManager = new JsonSchemaManager();

    public Set<SchemaDefinition> getDefinitions(String path) {
        if (path.startsWith(":")) {
            path = path.substring(1);
        }

        final String[] segments = path.split(":");
        Set<SchemaDefinition> definitions = Collections.emptySet();
        final JSONSchema swagger = schemaManager.getSchema("swagger");
        Set<SchemaDefinition> toTraverse = Collections.singleton(new SchemaDefinition(swagger.asJson(), swagger
                .asJson()));

        // we iterate over all segments that form the path,
        // each segment is use to traverse the schema, until
        // we find the definitions that correspond to the latest segment
        for (String segment : segments) {
            // we reset the definitions we found until now
            // only the latest are of interest.
            definitions = new HashSet<>();
            for (SchemaDefinition traverse : toTraverse) {
                definitions.addAll(traverse(traverse, segment));
            }
            // we keep the definitions we will
            // need to process in the next iteration
            toTraverse = definitions;
        }

        return definitions;
    }

    /*
     * This method takes for parameter the current JSON object, and the segment of a path that should be use to traverse
     * the current JSON object.
     * 
     * Returns the set of nodes that matches the current segment, after applying the segment to the current node.
     */
    private Set<SchemaDefinition> traverse(SchemaDefinition current, String segment) {
        final Set<SchemaDefinition> definitions = new HashSet<>();

        // make sure the current node is not a ref.
        current = JsonUtil.getReference(current.schema, current.definition);

        if (segment.isEmpty()) {
            return Collections.singleton(current);
        }

        // if it's an array, collect definitions
        // from the property items.
        if (isArray(current.definition, segment)) {
            definitions.add(JsonUtil.getReference(current.schema, current.definition.get("items")));
        }

        // if the node has properties, lookup for properties that
        // matches the segment
        if (current.definition.has("properties") && current.definition.get("properties").has(segment)) {
            definitions.add(JsonUtil.getReference(current.schema, current.definition.get("properties").get(segment)));
        }

        // if nothing found yet, collect pattern properties
        // that match with the segment
        if (definitions.isEmpty() && current.definition.has("patternProperties")) {
            definitions.addAll(traversePatternProperties(current, segment));
        }

        // same with additional properties
        if (definitions.isEmpty() && current.definition.has("additionalProperties")) {
            definitions.addAll(traverseAdditionalProperties(current, segment));
        }

        // if the node is of type oneOf, traverse and collect
        // all definitions from the oneOf
        if (definitions.isEmpty() && current.definition.has("oneOf")) {
            definitions.addAll(traverseOneOf(current, segment));
        }

        // if the node is of type anyOf, traverse and collect
        // all definitions from the anyOf
        if (definitions.isEmpty() && current.definition.has("anyOf")) {
            definitions.addAll(traverseAnyOf(current, segment));
        }

        return definitions;
    }

    /*
     * Returns true if the segment matches a position in an array and the current node is itself an array.
     */
    private boolean isArray(JsonNode current, String segment) {
        return JsonType.ARRAY == JsonType.valueOf(current);
    }

    private Set<SchemaDefinition> traversePatternProperties(SchemaDefinition current, String segment) {
        final Set<SchemaDefinition> definitions = new HashSet<>();
        final JsonNode properties = current.definition.get("patternProperties");
        final Iterator<String> it = properties.fieldNames();

        while (it.hasNext()) {
            String key = it.next();
            if (key.startsWith("^")) {
                if (segment.startsWith(key.substring(1))) {
                    if (properties.has(key)) {
                        definitions.add(JsonUtil.getReference(current.schema, properties.get(key)));
                    }
                }
            }

            if (segment.matches(key) && properties.has(key)) {
                definitions.add(JsonUtil.getReference(current.schema, properties.get(key)));
            }
        }

        return definitions;
    }

    private Set<SchemaDefinition> traverseAdditionalProperties(SchemaDefinition current, String segment) {
        final JsonNode properties = current.definition.get("additionalProperties");

        if (properties.isObject()) {
            return Collections.singleton(JsonUtil.getReference(current.schema, properties));
        }

        return Collections.emptySet();
    }

    private Set<SchemaDefinition> traverseOneOf(SchemaDefinition current, String segment) {
        final Iterator<JsonNode> it = current.definition.get("oneOf").elements();
        final Set<SchemaDefinition> definitions = new HashSet<>();

        while (it.hasNext()) {
            JsonNode next = it.next();
            Set<SchemaDefinition> found = traverse(JsonUtil.getReference(current.schema, next), segment);
            definitions.addAll(found);
        }

        return definitions;
    }

    private Set<SchemaDefinition> traverseAnyOf(SchemaDefinition current, String segment) {
        final Iterator<JsonNode> it = current.definition.get("anyOf").elements();
        final Set<SchemaDefinition> definitions = new HashSet<>();

        while (it.hasNext()) {
            JsonNode next = it.next();
            Set<SchemaDefinition> found = traverse(JsonUtil.getReference(current.schema, next), segment);
            definitions.addAll(found);
        }

        return definitions;
    }

}
