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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.ProposalBuilder;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class SchemaFormatContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/schema/properties/format");

    private static final Map<String, List<ProposalBuilder>> values = ImmutableMap.<String, List<ProposalBuilder>> builder()
            .put("boolean", ImmutableList.<ProposalBuilder> of()) //
            .put("array", ImmutableList.<ProposalBuilder> of()) //
            .put("object", ImmutableList.<ProposalBuilder> of()) //
            .put("null", ImmutableList.<ProposalBuilder> of()) //
            .put("integer",
                    ImmutableList.of( //
                            new ProposalBuilder("int32").replacementString("int32").type("string"), //
                            new ProposalBuilder("int64").replacementString("int64").type("string"))) //
            .put("number",
                    ImmutableList.of( //
                            new ProposalBuilder("float").replacementString("float").type("string"), //
                            new ProposalBuilder("double").replacementString("double").type("string"))) //
            .put("string",
                    ImmutableList.of( //
                            new ProposalBuilder("byte").replacementString("byte").type("string"), //
                            new ProposalBuilder("binary").replacementString("binary").type("string"), //
                            new ProposalBuilder("date").replacementString("date").type("string"), //
                            new ProposalBuilder("date-time").replacementString("date-time").type("string"), //
                            new ProposalBuilder("password").replacementString("password").type("string"), //
                            new ProposalBuilder("").replacementString("").type("string")))
            .build();

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<ProposalBuilder> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        List<ProposalBuilder> proposals = Lists.newArrayList();

        if (node.getParent() != null && node.getParent().get("type") != null) {
            String filter = (String) node.getParent().get("type").asValue().getValue();

            if (values.containsKey(filter)) {
                return values.get(filter);
            }
        }

        for (List<ProposalBuilder> value : values.values()) {
            for (ProposalBuilder v : value) {
                proposals.add(v);
            }
        }

        proposals.add(new ProposalBuilder("").replacementString("").type("string"));

        return proposals;
    }

}
