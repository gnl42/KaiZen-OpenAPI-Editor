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
 * Represents a JSON reference that should be resolved as a type definition.
 *
 */
public class ReferenceTypeDefinition extends TypeDefinition {

    private TypeDefinition resolved;

    public ReferenceTypeDefinition(JsonSchema schema, JsonPointer pointer, JsonNode definition) {
        super(schema, pointer, definition, JsonType.UNDEFINED);
    }

    public TypeDefinition resolve() {
        if (resolved != null) {
            return resolved;
        }
        return resolved = schema.getManager().resolve(this, content.get(JsonReference.PROPERTY).asText());
    }

    @Override
    public JsonType getType() {
        return resolve().getType();
    }

    @Override
    public String getDescription() {
        return resolve().getDescription();
    }

    @Override
    public JsonPointer getPointer() {
        return resolve().getPointer();
    }

    @Override
    public JsonNode asJson() {
        return resolve().asJson();
    }

    @Override
    public String getContainingProperty() {
        return resolve().getContainingProperty();
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        return resolve().getPropertyType(property);
    }
}
