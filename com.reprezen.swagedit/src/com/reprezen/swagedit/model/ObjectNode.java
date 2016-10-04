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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Iterables;

public class ObjectNode extends AbstractNode {

    private final Map<String, AbstractNode> elements = new LinkedHashMap<>();

    public ObjectNode(AbstractNode parent, JsonPointer ptr) {
        super(parent, ptr);
    }

    @Override
    public AbstractNode get(int pos) {
        return Iterables.get(elements.values(), pos);
    }

    @Override
    public AbstractNode get(String property) {
        return elements.get(property);
    }

    public AbstractNode put(String property, AbstractNode value) {
        this.elements.put(property, value);
        return this;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public Iterable<AbstractNode> elements() {
        return elements.values();
    }

    @Override
    public String getText() {
        return getProperty() == null ? "" : getProperty();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}