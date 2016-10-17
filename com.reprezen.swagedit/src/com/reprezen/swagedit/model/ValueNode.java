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

import com.fasterxml.jackson.core.JsonPointer;

public class ValueNode extends AbstractNode {

    private final Object value;

    public ValueNode(Model model, AbstractNode parent, JsonPointer ptr, Object value) {
        super(model, parent, ptr);

        this.value = value;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String getText() {
        String text = getProperty() != null ? getProperty() + ": " : "";
        return text + (value != null ? getValue().toString() : "");
    }

    @Override
    public String toString() {
        return getText();
    }
}