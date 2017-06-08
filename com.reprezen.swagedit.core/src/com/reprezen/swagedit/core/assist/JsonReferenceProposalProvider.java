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
package com.reprezen.swagedit.core.assist;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.contexts.ContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextTypeCollection;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonDocumentManager;
import com.reprezen.swagedit.core.utils.DocumentUtils;
import com.reprezen.swagedit.core.utils.SwaggerFileFinder;
import com.reprezen.swagedit.core.utils.SwaggerFileFinder.Scope;

/**
 * Completion proposal provider for JSON references.
 */
public class JsonReferenceProposalProvider {

    private final JsonDocumentManager manager = JsonDocumentManager.getInstance();
    private final ContextTypeCollection contextTypes;
    private final String fileContentType;

    public JsonReferenceProposalProvider(ContextTypeCollection contextTypes, String fileContentType) {
        this.contextTypes = contextTypes;
        this.fileContentType = fileContentType;
    }

    protected IFile getActiveFile() {
        return DocumentUtils.getActiveEditorInput().getFile();
    }

    protected ContextTypeCollection getContextTypes() {
        return contextTypes;
    }

    public boolean canProvideProposal(JsonPointer pointer) {
        return pointer != null && contextTypes.get(pointer) != ContextType.UNKNOWN;
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
     * @param document
     * @param scope
     * @return proposals
     */
    public Collection<Proposal> getProposals(JsonPointer pointer, JsonDocument document, Scope scope) {
        final ContextType type = contextTypes.get(pointer);
        final IFile currentFile = getActiveFile();
        final IPath basePath = currentFile.getParent().getFullPath();
        final List<Proposal> proposals = Lists.newArrayList();

        if (scope == Scope.LOCAL) {
            proposals.addAll(type.collectProposals(document, null));
        } else if (!type.isLocalOnly()) {
            final SwaggerFileFinder fileFinder = new SwaggerFileFinder(fileContentType);

            for (IFile file : fileFinder.collectFiles(scope, currentFile)) {
                IPath relative = file.equals(currentFile) ? null : file.getFullPath().makeRelativeTo(basePath);
                if (file.equals(currentFile)) {
                    proposals.addAll(type.collectProposals(document, relative));
                } else {
                    proposals.addAll(type.collectProposals(manager.getDocument(file.getLocationURI()), relative));
                }
            }
        }

        return proposals;
    }

}
