/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.schema;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.core.schema.JsonSchema;
import com.reprezen.swagedit.core.schema.ObjectTypeDefinition;

public class OpenApi3Schema extends CompositeSchema {

    private JsonSchema coreType;
   
    public OpenApi3Schema() {

        JsonNode core;
        try {
            core = mapper.readTree(getClass().getResourceAsStream("core.json"));
        } catch (IOException e) {
            return;
        }

        JsonNode content;
        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema_v3.json"));
        } catch (IOException e) {
            return;
        }

        coreType = new JsonSchema(core, this);
        coreType.setType(new ObjectTypeDefinition(coreType, JsonPointer.compile(""), core));

        swaggerType = new JsonSchema(content, this);
        swaggerType.setType(new ObjectTypeDefinition(swaggerType, JsonPointer.compile(""), content));

    }



}
