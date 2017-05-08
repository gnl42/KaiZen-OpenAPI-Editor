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
package com.reprezen.swagedit.core.schema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

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

    public ArrayTypeDefinition(JsonSchema schema, JsonPointer pointer, JsonNode definition) {
        super(schema, pointer, definition, JsonType.ARRAY);
        itemsType = schema.createType(this, "items", definition.get("items"));
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        return itemsType;
    }
}