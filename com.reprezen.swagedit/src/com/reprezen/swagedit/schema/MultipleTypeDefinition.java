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

import java.util.List;

/**
 * Represents a JSON schema type definition that is defined in separated types.
 * 
 */
public class MultipleTypeDefinition extends TypeDefinition {

    private final List<TypeDefinition> multipleTypes;

    public MultipleTypeDefinition(SwaggerSchema schema, List<TypeDefinition> multipleTypes) {
        super(schema, null, null, JsonType.UNDEFINED);
        this.multipleTypes = multipleTypes;
    }

    public List<TypeDefinition> getMultipleTypes() {
        return multipleTypes;
    }

    @Override
    public String toString() {
        return multipleTypes.toString();
    }
}
