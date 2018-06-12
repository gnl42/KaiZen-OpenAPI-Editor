/*******************************************************************************
 * Copyright (c) 2017 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.assist.ext;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class SchemaTypeContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/schema/properties/type");

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, JsonNode node, String prefix) {
        return Lists.newArrayList( //
                new Proposal("array", "array", null, "enum"), //
                new Proposal("boolean", "boolean", null, "enum"), //
                new Proposal("integer", "integer", null, "enum"), //
                new Proposal("\"null\"", "null", null, "enum"), //
                new Proposal("number", "number", null, "enum"), //
                new Proposal("object", "object", null, "enum"), //
                new Proposal("string", "string", null, "enum"));
    }

}
