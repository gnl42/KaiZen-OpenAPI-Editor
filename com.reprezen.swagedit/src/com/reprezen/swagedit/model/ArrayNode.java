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
package com.reprezen.swagedit.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;

public class ArrayNode extends AbstractNode {

    private final List<AbstractNode> elements = new ArrayList<>();

    public ArrayNode(AbstractNode parent, JsonPointer ptr) {
        super(parent, ptr);
    }

    @Override
    public AbstractNode get(int pos) {
        return elements.get(pos);
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public void add(AbstractNode model) {
        this.elements.add(model);
    }

    @Override
    public Iterable<AbstractNode> elements() {
        return elements;
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