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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;
import com.reprezen.swagedit.schema.ArrayTypeDefinition;
import com.reprezen.swagedit.schema.ComplexTypeDefinition;
import com.reprezen.swagedit.schema.JsonType;
import com.reprezen.swagedit.schema.MultipleTypeDefinition;
import com.reprezen.swagedit.schema.ObjectTypeDefinition;
import com.reprezen.swagedit.schema.TypeDefinition;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider {

    public Collection<Proposal> getProposals(JsonPointer pointer, Model model) {
        final AbstractNode node = model.find(pointer);

        if (node == null) {
            return Collections.emptyList();
        }
        return getProposals(node.getType(), node);
    }

    public Collection<Proposal> getProposals(AbstractNode node) {
        return getProposals(node.getType(), node);
    }

    protected Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node) {
        switch (type.getType()) {
        case STRING:
        case INTEGER:
        case NUMBER:
            return createPrimitiveProposals(type);
        case BOOLEAN:
            return createBooleanProposals(type);
        case ENUM:
            return createEnumProposals(type, node);
        case ARRAY:
            return createArrayProposals((ArrayTypeDefinition) type, node);
        case OBJECT:
            return createObjectProposals((ObjectTypeDefinition) type, node);
        case ALL_OF:
        case ANY_OF:
        case ONE_OF:
            return createComplextTypeProposals((ComplexTypeDefinition) type, node);
        case UNDEFINED:
            Collection<Proposal> proposals = new LinkedHashSet<>();
            if (type instanceof MultipleTypeDefinition) {
                for (TypeDefinition currentType : ((MultipleTypeDefinition) type).getMultipleTypes()) {
                    proposals.addAll(getProposals(currentType, node));
                }
            }
            return proposals;
        }
        return Collections.emptyList();
    }

    protected Collection<Proposal> createPrimitiveProposals(TypeDefinition type) {
        String label;
        if (type.getType() == JsonType.UNDEFINED) {
            label = type.getContainingProperty();
        } else {
            label = type.getType().getValue();
        }

        return Lists.newArrayList(new Proposal("", "", type.getDescription(), label));
    }

    protected Collection<Proposal> createBooleanProposals(TypeDefinition type) {
        Collection<Proposal> proposals = new ArrayList<>();

        String label = type.getContainingProperty();

        proposals.add(new Proposal("true", "true", type.getDescription(), label));
        proposals.add(new Proposal("false", "false", type.getDescription(), label));

        return proposals;
    }

    protected Proposal createPropertyProposal(String key, TypeDefinition type) {
        if (type != null) {
            String label;
            if (Objects.equals(key, type.getContainingProperty())) {
                label = type.getType().getValue();
            } else {
                label = type.getContainingProperty();
            }

            return new Proposal(key + ":", key, type.getDescription(), label);
        } else {
            return null;
        }
    }

    protected Collection<Proposal> createObjectProposals(ObjectTypeDefinition type, AbstractNode element) {
        final Collection<Proposal> proposals = new LinkedHashSet<>();

        for (String property : type.getProperties().keySet()) {
            if (element.get(property) == null) {
                Proposal proposal = createPropertyProposal(property, type.getProperties().get(property));
                if (proposal != null) {
                    proposals.add(proposal);
                }
            }
        }

        for (String property : type.getPatternProperties().keySet()) {
            TypeDefinition typeDef = type.getPatternProperties().get(property);

            if (property.startsWith("^")) {
                property = property.substring(1);
            }

            Proposal proposal = createPropertyProposal(property, typeDef);
            if (proposal != null) {
                proposals.add(proposal);
            }
        }

        for (String property : type.getAdditionalProperties().keySet()) {
            if (element.get(property) == null) {
                Proposal proposal = createPropertyProposal(property, type.getAdditionalProperties().get(property));
                if (proposal != null) {
                    proposals.add(proposal);
                }
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
                replStr = "- " + literal;

                proposals.add(new Proposal(replStr, literal, type.itemsType.getDescription(),
                        type.itemsType.getContainingProperty()));
            }
        } else {
            proposals.add(new Proposal("-", "-", type.getDescription(), "array item"));
        }

        return proposals;
    }

    protected Collection<Proposal> createComplextTypeProposals(ComplexTypeDefinition type, AbstractNode node) {
        final Collection<Proposal> proposals = new HashSet<>();

        for (TypeDefinition definition : type.getComplexTypes()) {
            proposals.addAll(getProposals(definition, node));
        }

        return proposals;
    }

    protected List<String> enumLiterals(TypeDefinition type) {
        List<String> literals = new ArrayList<>();
        for (JsonNode literal : type.asJson().get("enum")) {
            literals.add(literal.asText());
        }
        return literals;
    }

    protected Collection<Proposal> createEnumProposals(TypeDefinition type, AbstractNode node) {
        final Collection<Proposal> proposals = new ArrayList<>();
        final String subType = type.asJson().has("type") ? //
                type.asJson().get("type").asText() : //
                null;

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

            proposals.add(new Proposal(replStr, literal, type.getDescription(), type.getContainingProperty()));
        }

        return proposals;
    }

}
