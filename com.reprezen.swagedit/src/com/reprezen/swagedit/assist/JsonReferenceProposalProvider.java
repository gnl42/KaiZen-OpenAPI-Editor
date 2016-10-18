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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.json.references.JsonDocumentManager;
import com.reprezen.swagedit.utils.DocumentUtils;
import com.reprezen.swagedit.utils.SwaggerFileFinder;
import com.reprezen.swagedit.utils.SwaggerFileFinder.Scope;
import com.reprezen.swagedit.utils.URLUtils;

/**
 * Completion proposal provider for JSON references.
 */
public class JsonReferenceProposalProvider {

    protected static final String SCHEMA_DEFINITION_REGEX = "^/definitions/(\\w+/)+\\$ref|.*schema/(\\w+/)?\\$ref";
    protected static final String RESPONSE_REGEX = ".*responses/\\d+/\\$ref";
    protected static final String PARAMETER_REGEX = ".*/parameters/\\d+/\\$ref";
    protected static final String PATH_ITEM_REGEX = "/paths/~1[^/]+/\\$ref";

    private final JsonDocumentManager manager = JsonDocumentManager.getInstance();

    protected IFile getActiveFile() {
        return DocumentUtils.getActiveEditorInput().getFile();
    }

    /**
     * Returns collection of JSON reference proposals.
     * 
     * If the scope is local, it will only return JSON references from within the current document.
     * 
     * If the scope is project, it will return all JSON references from within the current document and from all
     * documents inside the same project.
     * 
     * If the scope is workspace, it will return all JSON references from within the current document and from all
     * documents inside the same workspace.
     * 
     * @param pointer
     * @param doc
     * @param scope
     * @return proposals
     */
    public Collection<Proposal> getProposals(JsonPointer pointer, JsonNode doc, Scope scope) {
        final ContextType type = ContextType.get(pointer.toString());
        final IFile currentFile = getActiveFile();
        final IPath basePath = currentFile.getParent().getFullPath();
        final List<Proposal> proposals = Lists.newArrayList();

        if (scope == Scope.LOCAL) {
            proposals.addAll(collectProposals(doc, type.value(), null));
        } else {
            final SwaggerFileFinder fileFinder = new SwaggerFileFinder();

            for (IFile file : fileFinder.collectFiles(scope, currentFile)) {
                IPath relative = file.equals(currentFile) ? null : file.getFullPath().makeRelativeTo(basePath);
                JsonNode content = file.equals(currentFile) ? doc : manager.getDocument(file.getLocationURI());
                proposals.addAll(collectProposals(content, type.value(), relative));
            }
        }

        return proposals;
    }

    /**
     * Represents the different contexts for which a JSON reference may be computed. <br/>
     * The context type is determined by the pointer (path) on which the completion proposal has been activated.
     */
    protected enum ContextType {
        SCHEMA_DEFINITION("definitions", "schemas"), //
        PATH_ITEM("paths", "path items"), //
        PATH_PARAMETER("parameters", "parameters"), //
        PATH_RESPONSE("responses", "responses"), //
        UNKNOWN(null, "");

        private final String value;
        private final String label;

        private ContextType(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String value() {
            return value;
        }

        public String label() {
            return label;
        }

        public static ContextType get(String path) {
            if (Strings.emptyToNull(path) == null) {
                return UNKNOWN;
            }

            if (path.matches(SCHEMA_DEFINITION_REGEX)) {
                return SCHEMA_DEFINITION;
            } else if (path.matches(PARAMETER_REGEX)) {
                return PATH_PARAMETER;
            } else if (path.matches(RESPONSE_REGEX)) {
                return PATH_RESPONSE;
            } else if (path.matches(PATH_ITEM_REGEX)) {
                return PATH_ITEM;
            }

            return UNKNOWN;
        }
    }

    /**
     * Returns all proposals found in the document at the specified field.
     * 
     * @param document
     * @param fieldName
     * @param path
     * @return Collection of proposals
     */
    protected Collection<Proposal> collectProposals(JsonNode document, String fieldName, IPath path) {
        final Collection<Proposal> results = Lists.newArrayList();
        if (fieldName == null || !document.has(fieldName)) {
            return results;
        }

        final JsonNode parameters = document.get(fieldName);
        final String basePath = (path != null ? path.toString() : "") + "#/" + fieldName + "/";

        for (Iterator<String> it = parameters.fieldNames(); it.hasNext();) {
            String key = it.next();
            String value = basePath + key.replaceAll("/", "~1");
            String encoded = URLUtils.encodeURL(value);

            results.add(new Proposal("\"" + encoded + "\"", key, null, value));
        }

        return results;
    }

}
