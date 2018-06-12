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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class SchemaFormatContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/schema/properties/format");

    private static final Map<String, List<Proposal>> values = ImmutableMap.<String, List<Proposal>> builder()
            .put("boolean", ImmutableList.<Proposal> of()) //
            .put("array", ImmutableList.<Proposal> of()) //
            .put("object", ImmutableList.<Proposal> of()) //
            .put("null", ImmutableList.<Proposal> of()) //
            .put("integer",
                    ImmutableList.of( //
                    new Proposal("int32", "int32", null, "string"), //
                            new Proposal("int64", "int64", null, "string"))) //
            .put("number",
                    ImmutableList.of( //
                    new Proposal("float", "float", null, "string"), //
                            new Proposal("double", "double", null, "string"))) //
            .put("string",
                    ImmutableList.of( //
                    new Proposal("byte", "byte", null, "string"), //
                    new Proposal("binary", "binary", null, "string"), //
                    new Proposal("date", "date", null, "string"), //
                    new Proposal("date-time", "date-time", null, "string"), //
                    new Proposal("password", "password", null, "string"), //
                            new Proposal("", "", null, "string")))
            .build();

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, JsonNode node, String prefix) {
        List<Proposal> proposals = Lists.newArrayList();
        //
        // if (node.getParent() != null && node.getParent().get("type") != null) {
        // String filter = (String) node.getParent().get("type").asValue().getValue();
        //
        // if (values.containsKey(filter)) {
        // return values.get(filter);
        // }
        // }
        //
        // for (List<Proposal> value : values.values()) {
        // for (Proposal v : value) {
        // proposals.add(v);
        // }
        // }

        proposals.add(new Proposal("", "", null, "string"));

        return proposals;
    }

}
