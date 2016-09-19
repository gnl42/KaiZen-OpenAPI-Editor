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
package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.ArrayTypeDefinition;
import com.reprezen.swagedit.json.ComplexTypeDefinition;
import com.reprezen.swagedit.json.JsonType;
import com.reprezen.swagedit.json.ObjectTypeDefinition;
import com.reprezen.swagedit.json.TypeDefinition;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider {

    public Collection<Proposal> getProposals(JsonPointer pointer, Model model) {
        final AbstractNode node = model.find(pointer);
        System.out.println(">> " + pointer + " " + node);
        if (node == null) {
            return Collections.emptyList();
        }

        System.out.println(node.getType());
        return getProposals(node.getType(), node);
    }

    public Collection<Proposal> getProposals(AbstractNode node) {
        return getProposals(node.getType(), node);
    }

    protected Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node) {
        Collection<Proposal> proposals = new LinkedHashSet<>();

        switch (type.getType()) {
        case STRING:
        case INTEGER:
        case NUMBER:
            proposals.add(new Proposal("", "", getDescription(type.getDefinition()), type.getType().getValue()));
            break;
        case BOOLEAN:
            proposals
                    .add(new Proposal("true", "true", getDescription(type.getDefinition()), type.getType().getValue()));
            proposals.add(
                    new Proposal("false", "false", getDescription(type.getDefinition()), type.getType().getValue()));
            break;
        case ENUM:
            proposals = createEnumProposals(type, node);
            break;
        case ARRAY:
            proposals = createArrayProposals((ArrayTypeDefinition) type, node);
            break;
        case OBJECT:
            proposals = createObjectProposals((ObjectTypeDefinition) type, node);
            break;
        case ALL_OF:
        case ANY_OF:
        case ONE_OF:
            proposals = createComplextTypeProposals((ComplexTypeDefinition) type, node);
            break;
        case UNDEFINED:
            break;
        }

        return proposals;
    }

    protected Proposal createPropertyProposal(String key, JsonNode value) {
        // final JsonNode resolved = resolve(schema, value);
        final JsonType type = JsonType.valueOf(value);

        return new Proposal(key + ":", key, getDescription(value), type.getValue());
    }

    protected String getDescription(JsonNode node) {
        return node.has("description") ? node.get("description").asText() : null;
    }

    protected Collection<Proposal> createObjectProposals(ObjectTypeDefinition type, AbstractNode element) {
        final Collection<Proposal> proposals = new LinkedHashSet<>();
        final JsonNode definition = type.getDefinition();

        if (definition.has("properties")) {
            final JsonNode properties = definition.get("properties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                final String key = it.next();

                if (element.get(key) == null) {
                    proposals.add(createPropertyProposal(key, properties.get(key)));
                }
            }
        }

        if (definition.has("patternProperties")) {
            final JsonNode properties = definition.get("patternProperties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                String key = it.next();
                final JsonNode value = properties.get(key);

                if (key.startsWith("^")) {
                    key = key.substring(1);
                }

                proposals.add(createPropertyProposal(key, value));
            }
        }

        if (proposals.isEmpty()) {
            proposals.add(new Proposal("_key_" + ":", "_key_", null, null));
        }

        return proposals;
    }

    protected Collection<Proposal> createArrayProposals(ArrayTypeDefinition type, AbstractNode node) {
        Collection<Proposal> proposals = new ArrayList<>();

        if (type.itemsType.getType() == JsonType.ENUM) {
            String replStr;
            for (String literal : enumLiterals(type.itemsType)) {
                // if the type of array is string and
                // current value is a number, it should be put
                // into quotes to avoid validation issues
                // if (NumberUtils.isNumber(literal) && "string".equals(subType)) {
                // replStr = "\"" + literal + "\"";
                // } else {
                replStr = "- " + literal;
                // }

                proposals.add(new Proposal(replStr, literal, getDescription(type.itemsType.getDefinition()),
                        type.itemsType.getType().getValue()));
            }
        } else {
            proposals.add(new Proposal("-", "-", getDescription(type.getDefinition()), "array item"));
        }

        return proposals;
    }

    protected Collection<Proposal> createComplextTypeProposals(ComplexTypeDefinition type, AbstractNode node) {
        final Collection<Proposal> proposals = new HashSet<>();

        for (TypeDefinition definition : type.getAllTypes()) {
            proposals.addAll(getProposals(definition, node));
        }

        return proposals;
    }

    protected List<String> enumLiterals(TypeDefinition type) {
        List<String> literals = new ArrayList<>();
        for (JsonNode literal : type.getDefinition().get("enum")) {
            literals.add(literal.asText());
        }
        return literals;
    }

    protected Collection<Proposal> createEnumProposals(TypeDefinition type, AbstractNode node) {
        final Collection<Proposal> proposals = new ArrayList<>();
        final String subType = type.getDefinition().has("type") ? //
                type.getDefinition().get("type").asText() : //
                null;

        String descr = getDescription(type.getDefinition());
        String replStr;

        for (String literal : enumLiterals(type)) {
            // if the type of array is string and
            // current value is a number, it should be put
            // into quotes to avoid validation issues
            if (NumberUtils.isNumber(literal) && "string".equals(subType)) {
                replStr = "\"" + literal + "\"";
            } else {
                replStr = literal;
            }

            proposals.add(new Proposal(replStr, literal, descr, type.getType().getValue()));
        }

        return proposals;
    }

}
