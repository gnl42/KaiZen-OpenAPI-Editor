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
package com.reprezen.swagedit.core.assist.ext;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ResponseCodeContentAssistExt implements ContentAssistExt {

    private static final JsonPointer pointer = JsonPointer.compile("/definitions/responses");

    private final List<String> baseCodes = Lists.newArrayList("100", "200", "300", "400", "500", "default");
    private final ArrayNode statusCodes;

    public ResponseCodeContentAssistExt() {
        statusCodes = init();
    }

    private ArrayNode init() {
        try {
            final URL file = Activator.getDefault().getBundle().getResource("resources/status-codes.json");
            try {
                return (ArrayNode) new ObjectMapper().readTree(file);
            } catch (IOException e) {
                return null;
            }
        } catch (NullPointerException e) {
            // For standalone tests, bundle cannot be loaded.
            return null;
        }
    }

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        Collection<Proposal> proposals = Lists.newArrayList();

        for (Iterator<JsonNode> it = statusCodes(prefix); it.hasNext();) {
            JsonNode current = it.next();
            String code = current.get("code").asText();
            String description = current.get("description").asText();
            String phase = current.get("phrase").asText();

            proposals.add(new Proposal(code + ":", code, description, phase));
        }

        proposals.add(new Proposal("x-", "x-", null, "vendorExtension"));

        return proposals;
    }

    private Iterator<JsonNode> statusCodes(final String prefix) {
        final boolean noPrefix = Strings.emptyToNull(prefix) == null;

        return Iterators.filter(statusCodes.elements(), new Predicate<JsonNode>() {
            @Override
            public boolean apply(JsonNode node) {
                String key = node.get("code").asText();

                if (noPrefix) {
                    return baseCodes.contains(key);
                }

                return key.startsWith(prefix);
            }
        });
    }
}
