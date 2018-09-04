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
package com.reprezen.swagedit.core.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;

public class ArrayNode extends AbstractNode {

    private final List<AbstractNode> elements = new ArrayList<>();

    ArrayNode(Model model, AbstractNode parent, JsonPointer ptr) {
        super(model, parent, ptr);
    }

    @Override
    public AbstractNode get(int pos) {
        return elements.get(pos);
    }

    @Override
    public ArrayNode asArray() {
        return this;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public void add(AbstractNode model) {
        this.elements.add(model);
    }

    @Override
    public AbstractNode[] elements() {
        return elements.toArray(new AbstractNode[elements.size()]);
    }

    @Override
    public String getText() {
        return getProperty() == null ? "" : getProperty();
    }

    @Override
    public String toString() {
        return "[ " + elements + " ]";
    }
}