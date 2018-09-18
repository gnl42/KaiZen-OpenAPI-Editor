/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.assist.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.assist.ProposalDescriptor;
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
    public Collection<ProposalDescriptor> collectProposals(Model model, IPath path) {
        final Collection<ProposalDescriptor> results = new ArrayList<>();
        final List<AbstractNode> nodes = model.findByType(operationPointer);

        for (AbstractNode node : nodes) {
            String pointer = node.getPointerString();
            String basePath = (path != null ? path.toString() : "") + "#" + pointer;
            String key = node.getProperty();
            String value = basePath;
            String encoded = URLUtils.encodeURL(value);

            results.add(new ProposalDescriptor(key).replacementString("\"" + encoded + "\"").type(value));
        }

        return results;
    }
}