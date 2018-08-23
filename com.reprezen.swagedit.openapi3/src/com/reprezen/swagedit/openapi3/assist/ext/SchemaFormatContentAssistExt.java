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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

public class SchemaFormatContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/schema/properties/format");

    private static final Map<String, List<Proposal>> values = Collections.unmodifiableMap(Stream
            .of(//
                    new SimpleEntry<>("boolean", asList()), //
                    new SimpleEntry<>("array", asList()), //
                    new SimpleEntry<>("object", asList()), //
                    new SimpleEntry<>("null", asList()), //
                    new SimpleEntry<>("integer",
                            asList( //
                                    new Proposal("int32", "int32", null, "string"), //
                                    new Proposal("int64", "int64", null, "string"))), //
                    new SimpleEntry<>("number",
                            asList( //
                                    new Proposal("float", "float", null, "string"), //
                                    new Proposal("double", "double", null, "string"))), //
                    new SimpleEntry<>("string",
                            asList( //
                                    new Proposal("byte", "byte", null, "string"), //
                                    new Proposal("binary", "binary", null, "string"), //
                                    new Proposal("date", "date", null, "string"), //
                                    new Proposal("date-time", "date-time", null, "string"), //
                                    new Proposal("password", "password", null, "string"), //
                                    new Proposal("", "", null, "string")))) //
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> (List<Proposal>) e.getValue())));
            

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        List<Proposal> proposals = new ArrayList<>();

        if (node.getParent() != null && node.getParent().get("type") != null) {
            String filter = (String) node.getParent().get("type").asValue().getValue();

            if (values.containsKey(filter)) {
                return values.get(filter);
            }
        }

        for (List<Proposal> value : values.values()) {
            for (Proposal v : value) {
                proposals.add(v);
            }
        }

        proposals.add(new Proposal("", "", null, "string"));

        return proposals;
    }

}
