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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;
import com.reprezen.swagedit.schema.ArrayTypeDefinition;
import com.reprezen.swagedit.schema.ComplexTypeDefinition;
import com.reprezen.swagedit.schema.JsonType;
import com.reprezen.swagedit.schema.MultipleTypeDefinition;
import com.reprezen.swagedit.schema.ObjectTypeDefinition;
import com.reprezen.swagedit.schema.ReferenceTypeDefinition;
import com.reprezen.swagedit.schema.TypeDefinition;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider {

    public Collection<Proposal> getProposals(JsonPointer pointer, Model model, String prefix) {
        final AbstractNode node = model.find(pointer);
        if (node == null) {
            return Collections.emptyList();
        }
        return getProposals(node.getType(), node, prefix);
    }

    public Collection<Proposal> getProposals(JsonPointer pointer, Model model) {
        return getProposals(pointer, model, null);
    }

    public Collection<Proposal> getProposals(AbstractNode node) {
        return getProposals(node.getType(), node, null);
    }

    protected Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {

        if (type instanceof ReferenceTypeDefinition) {
            type = ((ReferenceTypeDefinition) type).resolve();
        }

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
            return createObjectProposals((ObjectTypeDefinition) type, node, prefix);
        case ALL_OF:
        case ANY_OF:
        case ONE_OF:
            return createComplextTypeProposals((ComplexTypeDefinition) type, node, prefix);
        case UNDEFINED:
            Collection<Proposal> proposals = new LinkedHashSet<>();
            if (type instanceof MultipleTypeDefinition) {
                for (TypeDefinition currentType : ((MultipleTypeDefinition) type).getMultipleTypes()) {
                    proposals.addAll(getProposals(currentType, node, prefix));
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
        Collection<Proposal> proposals = new LinkedHashSet<>();

        String labelType = type.getType().getValue();

        proposals.add(new Proposal("true", "true", type.getDescription(), labelType));
        proposals.add(new Proposal("false", "false", type.getDescription(), labelType));

        return proposals;
    }

    protected Proposal createPropertyProposal(String key, TypeDefinition type) {
        if (type == null || "default".equals(key)) {
            return null;
        }

        String labelType;
        if (Objects.equals(key, type.getContainingProperty())) {
            labelType = type.getType().getValue();
        } else {
            labelType = type.getContainingProperty();
        }

        return new Proposal(key + ":", key, type.getDescription(), labelType);
    }

    protected Collection<Proposal> createObjectProposals(ObjectTypeDefinition type, AbstractNode element,
            String prefix) {
        final Collection<Proposal> proposals = new LinkedHashSet<>();

        for (String property : type.getProperties().keySet()) {
            Proposal proposal = createPropertyProposal(property, type.getProperties().get(property));
            if (proposal != null) {
                if (Strings.emptyToNull(prefix) != null && property.startsWith(prefix)) {
                    proposals.add(proposal);
                } else if (element.get(property) == null) {
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

        if (proposals.isEmpty()) {
            proposals.add(new Proposal("_key_" + ":", "_key_", null, null));
        }

        return proposals;
    }

    protected Collection<Proposal> createArrayProposals(ArrayTypeDefinition type, AbstractNode node) {
        Collection<Proposal> proposals = new LinkedHashSet<>();

        if (type.itemsType.getType() == JsonType.ENUM) {
            String labelType = type.itemsType.getContainingProperty();

            for (String literal : enumLiterals(type.itemsType)) {
                proposals.add(new Proposal("- " + literal, literal, type.getDescription(), labelType));
            }
        } else {
            proposals.add(new Proposal("-", "-", type.getDescription(), "array item"));
        }

        return proposals;
    }

    protected Collection<Proposal> createComplextTypeProposals(ComplexTypeDefinition type, AbstractNode node,
            String prefix) {
        final Collection<Proposal> proposals = new LinkedHashSet<>();

        for (TypeDefinition definition : type.getComplexTypes()) {
            proposals.addAll(getProposals(definition, node, prefix));
        }

        return proposals;
    }

    protected Collection<String> enumLiterals(TypeDefinition type) {
        Collection<String> literals = new LinkedHashSet<>();
        for (JsonNode literal : type.asJson().get("enum")) {
            literals.add(literal.asText());
        }
        return literals;
    }

    protected Collection<Proposal> createEnumProposals(TypeDefinition type, AbstractNode node) {
        final Collection<Proposal> proposals = new LinkedHashSet<>();
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

            String labelType = type.getType().getValue();

            proposals.add(new Proposal(replStr, literal, type.getDescription(), labelType));
        }

        return proposals;
    }

}
