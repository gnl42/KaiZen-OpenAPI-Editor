package com.reprezen.swagedit.core.assist.contexts;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.CompositeSchema;

public abstract class SchemaContextType extends ContextType {

    private final CompositeSchema schema;

    public SchemaContextType(CompositeSchema schema, String value, String label, String regex) {
        this(schema, value, label, regex, false);
    }

    public SchemaContextType(CompositeSchema schema, String value, String label, String regex, boolean isLocalOnly) {
        super(value, label, regex, isLocalOnly);

        this.schema = schema;
    }

    public CompositeSchema getSchema() {
        return schema;
    }

    @Override
    public Collection<Proposal> collectProposals(JsonDocument document, IPath path) {
        return collectProposals(document.getModel(), path);
    }

    @Override
    public Collection<Proposal> collectProposals(JsonNode document, IPath path) {
        return collectProposals(Model.parse(getSchema(), document), path);
    }

    public abstract Collection<Proposal> collectProposals(Model parse, IPath path);

}
