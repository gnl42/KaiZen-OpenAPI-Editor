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
package com.reprezen.swagedit.core.assist.ext;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.assist.ProposalBuilder;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.ArrayTypeDefinition;
import com.reprezen.swagedit.core.schema.TypeDefinition;

/**
 * Content assist extension providing completion proposals for mime types.
 *
 */
public class MediaTypeContentAssistExt implements ContentAssistExt {

    private final List<JsonPointer> validPointers = Lists.newArrayList(//
            JsonPointer.compile("/definitions/mediaTypeList"), // Swagger v2
            JsonPointer.compile("/definitions/mimeType"), // OAS3
            JsonPointer.compile("/definitions/mediaTypes") // OAS3
            ); 

    private final JsonNode mediaTypes;

    public MediaTypeContentAssistExt() {
        mediaTypes = init();
    }

    private JsonNode init() {
        try {
            final URL file = Activator.getDefault().getBundle().getResource("resources/mediaTypes.json");
            try {
                return new ObjectMapper().readTree(file);
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
        return type != null && type.getPointer() != null && validPointers.contains(type.getPointer());
    }

    @Override
    public Collection<ProposalBuilder> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        Collection<ProposalBuilder> proposals = new ArrayList<>();

        prefix = Strings.emptyToNull(prefix);

        for (JsonNode mediaType : mediaTypes) {
            String asText = mediaType.asText();
            if (prefix != null) {
                if (asText.contains(prefix.trim())) {
                    proposals.add(createProposal(type, mediaType));
                }
            } else {
                proposals.add(createProposal(type, mediaType));
            }
        }

        return proposals;
    }

    private ProposalBuilder createProposal(TypeDefinition type, JsonNode mediaType) {
        if (type instanceof ArrayTypeDefinition) {
            return new ProposalBuilder(mediaType.asText()).replacementString("- " + mediaType.asText()).description("").type("mimeType");
        }
        return new ProposalBuilder(mediaType.asText()).replacementString(mediaType.asText()).description("").type("mimeType");
    }
}
