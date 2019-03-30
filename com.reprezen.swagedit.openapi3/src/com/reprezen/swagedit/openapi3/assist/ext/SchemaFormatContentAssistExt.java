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

import static java.util.Arrays.asList;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.assist.ProposalDescriptor;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class SchemaFormatContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/schema/properties/format");

    private static final Map<String, List<ProposalDescriptor>> values = Collections.unmodifiableMap(Stream
            .of(//
                    new SimpleEntry<>("boolean", asList()), //
                    new SimpleEntry<>("array", asList()), //
                    new SimpleEntry<>("object", asList()), //
                    new SimpleEntry<>("null", asList()), //
                    new SimpleEntry<>("integer",
                            asList( //
                                    new ProposalDescriptor("int32").replacementString("int32").type("string"), //
                                    new ProposalDescriptor("int64").replacementString("int64").type("string"))), //
                    new SimpleEntry<>("number",
                            asList( //
                                    new ProposalDescriptor("float").replacementString("float").type("string"), //
                                    new ProposalDescriptor("double").replacementString("double").type("string"))), //
                    new SimpleEntry<>("string",
                            asList( //
                                    new ProposalDescriptor("byte").replacementString("byte").type("string"), //
                                    new ProposalDescriptor("binary").replacementString("binary").type("string"), //
                                    new ProposalDescriptor("date").replacementString("date").type("string"), //
                                    new ProposalDescriptor("date-time").replacementString("date-time").type("string"), //
                                    new ProposalDescriptor("password").replacementString("password").type("string"), //
                                    new ProposalDescriptor("").replacementString("").type("string"))))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> (List<ProposalDescriptor>) e.getValue())));
            
    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<ProposalDescriptor> getProposals(TypeDefinition type, AbstractNode node, String prefix, JsonDocument jsonDocument) {
        List<ProposalDescriptor> proposals = new ArrayList<>();

        if (node.getParent() != null && node.getParent().get("type") != null) {
            String filter = (String) node.getParent().get("type").asValue().getValue();

            if (values.containsKey(filter)) {
                return values.get(filter);
            }
        }

        for (List<ProposalDescriptor> value : values.values()) {
            for (ProposalDescriptor v : value) {
                proposals.add(v);
            }
        }

        proposals.add(new ProposalDescriptor("").replacementString("").type("string"));

        return proposals;
    }

}
