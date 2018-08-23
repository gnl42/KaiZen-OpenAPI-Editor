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
package com.reprezen.swagedit.core.editor.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Iterables;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;

public class OutlineContentProvider implements ITreeContentProvider {

    private Iterable<Model> models;

    @Override
    public void dispose() {
        models = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null) {
            this.models = Arrays.asList();
        } else if (newInput instanceof Model) {
            this.models = Arrays.asList((Model) newInput);
        } else if (Iterable.class.isAssignableFrom(newInput.getClass())) {
            this.models = (Iterable<Model>) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (models == null || Iterables.isEmpty(models)) {
            return null;
        }

        List<Object> roots = Arrays.asList();
        for (Model model : models) {
            roots.add(model.getRoot());
        }

        return roots.toArray(new Object[roots.size()]);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof AbstractNode) {
            return Iterables.toArray(((AbstractNode) parentElement).elements(), AbstractNode.class);
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof AbstractNode) {
            return ((AbstractNode) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof AbstractNode) {
            return !Iterables.isEmpty(((AbstractNode) element).elements());
        }
        return false;
    }

}
