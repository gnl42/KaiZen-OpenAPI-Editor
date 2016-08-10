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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Iterables;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;

public class OutlineContentProvider implements ITreeContentProvider {

    private Model model;

    @Override
    public void dispose() {
        model = null;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof Model) {
            this.model = (Model) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (model == null) {
            return null;
        }

        return new Object[] { model.getRoot() };
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
