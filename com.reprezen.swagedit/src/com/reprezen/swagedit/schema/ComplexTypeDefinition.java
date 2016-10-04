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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.swagedit.json.references.JsonReference;

/**
 * Represents a JSON schema type definition of a complex type, eg oneOf, allOf, anyOf types.
 * 
 * <br/>
 * 
 * Example of a complex type definition:
 * 
 * <code>
 * <pre>
 * "responseValue": {
 *   "oneOf": [
 *     {
 *       "$ref": "#/definitions/response"
 *     },
 *     {
 *       "$ref": "#/definitions/jsonReference"
 *     }
 *   ]
 * }
 * </pre>
 * </code>
 *
 */
public class ComplexTypeDefinition extends TypeDefinition {

    private final Collection<TypeDefinition> complexTypes = new LinkedHashSet<>();

    public ComplexTypeDefinition(final SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);
        init();
    }

    private void init() {
        final ArrayNode container = (ArrayNode) content.get(type.getValue());

        for (int i = 0; i < container.size(); i++) {
            JsonNode current = container.get(i);

            JsonPointer p;
            if (JsonReference.isReference(current)) {
                p = JsonPointer.compile(current.get(JsonReference.PROPERTY).asText().substring(1));
            } else {
                p = JsonPointer.compile(pointer.toString() + "/" + type.getValue() + "/" + i);
            }

            TypeDefinition def = schema.getType(p);
            if (def == null) {
                def = TypeDefinition.create(getSchema(), p);
                if (def != null) {
                    complexTypes.add(def);
                }
            } else {
                complexTypes.add(def);
            }
        }
    }

    /**
     * Returns the list of types contained by this complex type.
     * 
     * @return list of types
     */
    public Collection<TypeDefinition> getComplexTypes() {
        return complexTypes;
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        final List<TypeDefinition> collect = new ArrayList<>();
        collectProperties(property, this, collect);

        if (collect.isEmpty()) {
            return null;
        }

        if (collect.size() == 1) {
            return collect.get(0);
        }

        return new MultipleTypeDefinition(schema, collect);
    }

    private void collectProperties(String property, TypeDefinition current, List<TypeDefinition> collect) {
        if (current instanceof ObjectTypeDefinition) {
            if (current.getPropertyType(property) != null) {
                collect.add(current.getPropertyType(property));
            }
        } else if (current instanceof ComplexTypeDefinition) {
            for (TypeDefinition type : ((ComplexTypeDefinition) current).complexTypes) {
                collectProperties(property, type, collect);
            }
        }
    }
}