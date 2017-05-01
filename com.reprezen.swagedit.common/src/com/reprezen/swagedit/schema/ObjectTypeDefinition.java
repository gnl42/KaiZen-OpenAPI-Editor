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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.json.references.JsonReference;

/**
 * Represents a JSON schema type definition for objects.
 * 
 * <br/>
 * 
 * Example of an object type definition:
 * 
 * <code>
 * <pre>
 * {
 *   "type": "object",
 *   "required": [
 *     "swagger",
 *     info",
 *     "paths"
 *   ],
 *   "properties": {
 *     "swagger": ...
 *   }
 * }
 * </pre>
 * </code>
 *
 */
public class ObjectTypeDefinition extends TypeDefinition {

    private final List<String> requiredProperties = new ArrayList<>();
    private final Map<String, TypeDefinition> properties = new LinkedHashMap<>();
    private final Map<String, TypeDefinition> patternProperties = new LinkedHashMap<>();
    private TypeDefinition additionalProperties = null;

    public ObjectTypeDefinition(JsonSchema schema, JsonPointer pointer, JsonNode definition) {
        super(schema, pointer, definition, JsonType.OBJECT);
        init();
    }

    protected void init() {
        if (content.has("definitions") && content.get("definitions").isObject()) {
            JsonNode definitions = content.get("definitions");

            for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();
                schema.createType(this, "definitions/" + e.getKey(), e.getValue());
            }
        }

        initRequired();
        initProperties("properties", properties);
        initProperties("patternProperties", patternProperties);

        if (content.has("additionalProperties") && content.get("additionalProperties").isObject()) {
            JsonNode properties = content.get("additionalProperties");

            TypeDefinition definition = schema.createType(this, "additionalProperties", properties);
            if (definition != null) {
                additionalProperties = definition;
            }
        }
    }

    protected void initRequired() {
        JsonNode required = content.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode value : required) {
                requiredProperties.add(value.asText());
            }
        }
    }

    protected void initProperties(String container, Map<String, TypeDefinition> properties) {
        if (content.has(container) && content.get(container).isObject()) {

            JsonNode node = content.get(container);
            if (JsonReference.isReference(node)) {
                node = schema.resolve(JsonReference.getPointer(node));
            }

            for (Iterator<Entry<String, JsonNode>> it = node. fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();

                String property = e.getKey().replaceAll("/", "~1");
                TypeDefinition type = schema.createType(this, container + "/" + property, e.getValue());
                if (type != null) {
                    properties.put(e.getKey(), type);
                }
            }
        }
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        TypeDefinition type = getProperties().get(property);

        if (type == null) {
            type = getPatternType(property);
        }

        if (type == null && additionalProperties != null) {
            type = additionalProperties;
        }

        return type;
    }

    /**
     * Returns the list of properties that must be present in an instance of this type.
     * 
     * @return list of required properties
     */
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Returns the list of properties that can be added to an instance of this type.
     * 
     * @return list of properties
     */
    public Map<String, TypeDefinition> getProperties() {
        return properties;
    }

    /**
     * Returns the list of pattern properties that can be added to an instance of this type.
     * 
     * @return list of pattern properties
     */
    public Map<String, TypeDefinition> getPatternProperties() {
        return patternProperties;
    }

    /**
     * Returns the list of additional properties that can be added to an instance of this type.
     * 
     * @return list of additional properties
     */
    public TypeDefinition getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Returns the type definition associated to pattern property that matches the given property.
     * 
     * @param property
     * @return type
     */
    public TypeDefinition getPatternType(String property) {
        TypeDefinition found = null;
        Iterator<String> patterns = patternProperties.keySet().iterator();

        property = property.replaceAll("~1", "/");

        while (patterns.hasNext() && found == null) {
            String pattern = patterns.next();
            try {
                Matcher matcher = Pattern.compile(pattern).matcher(property);
                if (matcher.find() || matcher.matches()) {
                    found = patternProperties.get(pattern);
                }
            } catch (PatternSyntaxException e) {
            	Activator.getDefault().logError("Problem in JSON Schema", e);
            }
        }

        return found;
    }

}