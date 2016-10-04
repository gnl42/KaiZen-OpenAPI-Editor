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

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;

/**
 * Represents a JSON schema type definition for arrays.
 * 
 * <br/>
 * 
 * Example of an array type definition:
 * 
 * <code>
 * <pre>
 * "tags": {
 *   "type": "array",
 *   "items": {
 *     "$ref": "#/definitions/tag"
 *   },
 *   "uniqueItems": true
 * }
 * </pre>
 * </code>
 *
 */
public class ArrayTypeDefinition extends TypeDefinition {

    public final TypeDefinition itemsType;

    public ArrayTypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);

        itemsType = TypeDefinition.create(schema, getItemsPointer());
    }

    private JsonPointer getItemsPointer() {
        String p = getPointer().toString();

        JsonNode node = content.get("items");

        if (node.isObject() && node.has(JsonReference.PROPERTY)) {
            p = node.get(JsonReference.PROPERTY).asText().substring(1);
        } else {
            p += "/items";
        }

        return JsonPointer.compile(p);
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        return itemsType;
    }
}