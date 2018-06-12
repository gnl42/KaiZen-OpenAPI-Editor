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
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.utils.URLUtils;

/**
 * Represents the different contexts for which a JSON reference may be computed. <br/>
 */
public abstract class ContextType {
    public static final ContextType UNKNOWN = new ContextType(null, "") {

        @Override
        public boolean canProvideProposal(JsonModel document, JsonPointer pointer) {
            return false;
        }
    };

    private final String value;
    private final String label;
    private final boolean isLocalOnly;

    public ContextType(String value, String label) {
        this.value = value;
        this.label = label;
        this.isLocalOnly = false;
    }

    public ContextType(String value, String label,  boolean isLocalOnly) {
        this.value = value;
        this.label = label;
        this.isLocalOnly = isLocalOnly;
    }
    
    public abstract boolean canProvideProposal(JsonModel document, JsonPointer pointer);

    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    public boolean isLocalOnly() {
        return isLocalOnly;
    }
    
    public Collection<Proposal> collectProposals(JsonModel document, IPath path) {
        return collectProposals(document, path);
    }

    /**
     * Returns all proposals found in the document at the specified field.
     * 
     * @param document
     * @param fieldName
     * @param path
     * @return Collection of proposals
     */
    public Collection<Proposal> collectProposals(JsonNode document, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        if (value() == null) {
            return results;
        }

        final JsonNode nodes = document.at(value());
        if (nodes == null) {
            return results;
        }

        final String basePath = (path != null ? path.toString() : "") + "#/" + value() + "/";

        for (Iterator<String> it = nodes.fieldNames(); it.hasNext();) {
            String key = it.next();
            String value = basePath + key.replaceAll("/", "~1");
            String encoded = URLUtils.encodeURL(value);

            results.add(new Proposal("\"" + encoded + "\"", key, null, value));
        }

        return results;
    }

    public static ContextTypeCollection newContentTypeCollection(Iterable<ContextType> contextTypes) {
        return new ContextTypeCollection(contextTypes);
    }

    public static ContextTypeCollection emptyContentTypeCollection() {
        return new ContextTypeCollection(Collections.<ContextType> emptyList());
    }
}