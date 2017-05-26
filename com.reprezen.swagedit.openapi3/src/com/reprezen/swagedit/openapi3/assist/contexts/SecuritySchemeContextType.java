package com.reprezen.swagedit.openapi3.assist.contexts;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.contexts.SchemaContextType;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * ContextType that collects proposals from security schemes names.
 */
public class SecuritySchemeContextType extends SchemaContextType {

    private final JsonPointer securityPointer = JsonPointer.compile("/components/securitySchemes");

    public SecuritySchemeContextType(CompositeSchema schema, String regex) {
        super(schema, "securitySchemes", "securitySchemes", regex);
    }

    @Override
    public Collection<Proposal> collectProposals(Model model, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        final List<AbstractNode> nodes = Lists.newArrayList();
        for (AbstractNode node : model.allNodes()) {
            if (securityPointer.equals(node.getPointer())) {
                nodes.add(node);
            }
        }

        for (AbstractNode node : nodes) {
            if (node.isObject()) {
                for (String key : node.asObject().fieldNames()) {
                    results.add(new Proposal(key, key, null, node.getProperty()));
                }
            }
        }

        return results;
    }

}