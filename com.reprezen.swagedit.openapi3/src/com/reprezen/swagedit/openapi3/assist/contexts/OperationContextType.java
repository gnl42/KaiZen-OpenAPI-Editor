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

import java.util.Collection;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.contexts.SchemaContextType;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * ContextType that collects proposals from operations pointers.
 */
public class OperationContextType extends SchemaContextType {

    private final JsonPointer operationPointer = JsonPointer.compile("/definitions/operation");

    public OperationContextType(CompositeSchema schema, String regex) {
        super(schema, "operation", "operation", regex);
    }

    @Override
    public Collection<Proposal> collectProposals(JsonModel document, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        // final List<JsonNode> nodes = document.findByType(operationPointer);
        //
        // for (JsonNode node : nodes) {
        // String pointer = node.getPointerString();
        // String basePath = (path != null ? path.toString() : "") + "#" + pointer;
        // String key = node.getProperty();
        // String value = basePath;
        // String encoded = URLUtils.encodeURL(value);
        //
        // results.add(new Proposal("\"" + encoded + "\"", key, null, value));
        // }

        return results;
    }
}