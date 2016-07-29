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
package com.reprezen.swagedit.editor.outline;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class OutlineContentProvider implements ITreeContentProvider {

    private List<OutlineElement> nodes;

    @Override
    public void dispose() {
        nodes = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput != null) {
            this.nodes = (List<OutlineElement>) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (nodes == null) {
            return null;
        }

        return nodes.toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof OutlineElement) {
            return ((OutlineElement) parentElement).getChildren().toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof OutlineElement) {
            OutlineElement parent = ((OutlineElement) element).getParent();

            if (parent != element) {
                return parent;
            }
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof OutlineElement) {
            return !((OutlineElement) element).getChildren().isEmpty();
        }
        return false;
    }

}
