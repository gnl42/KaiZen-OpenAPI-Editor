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
package com.reprezen.swagedit.core.assist.contexts;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.assist.ProposalBuilder;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.CompositeSchema;

public abstract class SchemaContextType extends RegexContextType {

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
    public Collection<ProposalBuilder> collectProposals(JsonDocument document, IPath path) {
        return collectProposals(document.getModel(), path);
    }

    @Override
    public Collection<ProposalBuilder> collectProposals(JsonNode document, IPath path) {
        return collectProposals(Model.parse(getSchema(), document), path);
    }

    public abstract Collection<ProposalBuilder> collectProposals(Model parse, IPath path);

}
