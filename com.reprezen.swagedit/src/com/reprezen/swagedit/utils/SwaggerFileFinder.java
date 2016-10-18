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
package com.reprezen.swagedit.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Lists;

public class SwaggerFileFinder {

    /**
     * Represents the scope for which the JSON reference proposals have to be computed. <br/>
     * The default scope LOCAL means that JSON references will be computed only from inside the currently edited file.
     * The scope PROJECT means that JSON references will be computed from files inside the same project has the
     * currently edited file. The scope WORKSPACE means that JSON references will be computed from files present in the
     * current workspace.
     */
    public enum Scope {
        LOCAL(0) {
            @Override
            public Scope next() {
                return PROJECT;
            }
        },
        PROJECT(1) {
            @Override
            public Scope next() {
                return WORKSPACE;
            }
        },
        WORKSPACE(2) {
            @Override
            public Scope next() {
                return LOCAL;
            }
        };

        private final int value;

        Scope(int v) {
            this.value = v;
        }

        public int getValue() {
            return value;
        }

        public abstract Scope next();

        public static Scope get(int cycle) {
            switch (cycle) {
            case 1:
                return PROJECT;
            case 2:
                return WORKSPACE;
            default:
                return Scope.LOCAL;
            }
        }
    }

    public Iterable<IFile> collectFiles(Scope scope, IFile currentFile) {
        if (currentFile == null) {
            return Lists.newArrayList();
        }

        switch (scope) {
        case PROJECT:
            return collectFiles(currentFile.getProject(), currentFile);
        case WORKSPACE:
            return collectFiles(currentFile.getWorkspace().getRoot(), currentFile);
        default:
            return Lists.newArrayList(currentFile);
        }
    }

    protected Iterable<IFile> collectFiles(IContainer parent, IFile currentFile) {
        final FileVisitor visitor = new FileVisitor(currentFile);

        try {
            parent.accept(visitor, 0);
        } catch (CoreException e) {
            return Lists.newArrayList();
        }
        return visitor.getFiles();
    }

    private static class FileVisitor implements IResourceProxyVisitor {

        private final List<IFile> files;
        private final IFile currentFile;

        public FileVisitor(IFile currentFile) {
            this.currentFile = currentFile;
            this.files = new ArrayList<IFile>();

            if (currentFile != null) {
                files.add(currentFile);
            }
        }

        @Override
        public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getType() == IResource.FILE
                    && (proxy.getName().endsWith("yaml") || proxy.getName().endsWith("yml"))) {

                if (!proxy.isDerived()) {
                    IFile file = (IFile) proxy.requestResource();
                    if (!file.equals(currentFile)) {
                        files.add(file);
                    }
                }
            } else if (proxy.getType() == IResource.FOLDER
                    && (proxy.isDerived() || proxy.getName().equalsIgnoreCase("gentargets"))) {
                return false;
            }
            return true;
        }

        public List<IFile> getFiles() {
            return files;
        }
    }

}
