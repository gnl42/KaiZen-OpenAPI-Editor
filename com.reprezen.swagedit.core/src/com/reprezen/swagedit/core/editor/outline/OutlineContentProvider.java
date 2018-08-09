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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.json.JsonModel;

public class OutlineContentProvider implements ITreeContentProvider {

    private Iterable<JsonModel> models;

    @Override
    public void dispose() {
        models = Lists.newArrayList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null) {
            this.models = Lists.newArrayList();
        } else if (newInput instanceof JsonModel) {
            this.models = Lists.newArrayList((JsonModel) newInput);
        } else if (Iterable.class.isAssignableFrom(newInput.getClass())) {
            this.models = (Iterable<JsonModel>) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (models == null || Iterables.isEmpty(models)) {
            return null;
        }

        List<Pair<JsonModel, JsonPointer>> roots = Lists.newArrayList();
        for (JsonModel model : models) {
            roots.add(Pair.of(model, JsonPointer.compile("")));
        }

        return roots.toArray(new Object[roots.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Pair) {
            JsonModel model = ((Pair<JsonModel, JsonPointer>) parentElement).getLeft();
            JsonPointer ptr = ((Pair<JsonModel, JsonPointer>) parentElement).getRight();

            List<Pair<JsonModel, JsonPointer>> result = Lists.newArrayList();
            Set<JsonPointer> children = model.getPaths().get(ptr);
            if (children != null) {
                for (JsonPointer p : children) {
                result.add(Pair.of(model, p));
            }
            }
            return result.toArray();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof Pair) {
            JsonModel model = ((Pair<JsonModel, JsonPointer>) element).getLeft();
            JsonPointer ptr = ((Pair<JsonModel, JsonPointer>) element).getRight();

            Set<JsonPointer> children = model.getPaths().get(ptr);
            return children != null && !children.isEmpty();
        }
        return false;
    }

}
