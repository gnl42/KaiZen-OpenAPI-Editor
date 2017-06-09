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
import com.reprezen.swagedit.core.utils.URLUtils;

/**
 * ContextType that collects proposals from operations pointers.
 */
public class OperationContextType extends SchemaContextType {

    private final JsonPointer operationPointer = JsonPointer.compile("/definitions/operation");

    public OperationContextType(CompositeSchema schema, String regex) {
        super(schema, "operation", "operation", regex);
    }

    @Override
    public Collection<Proposal> collectProposals(Model model, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        final List<AbstractNode> nodes = model.findByType(operationPointer);

        for (AbstractNode node : nodes) {
            String pointer = node.getPointerString();
            String basePath = (path != null ? path.toString() : "") + "#" + pointer + "/";
            String key = node.getProperty();
            String value = basePath;
            String encoded = URLUtils.encodeURL(value);

            results.add(new Proposal("\"" + encoded + "\"", key, null, value));
        }

        return results;
    }
}