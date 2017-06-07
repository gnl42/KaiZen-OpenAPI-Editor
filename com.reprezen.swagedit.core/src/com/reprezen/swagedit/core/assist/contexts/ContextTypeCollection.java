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
package com.reprezen.swagedit.core.assist.contexts;
import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.model.Model;

public class ContextTypeCollection {

    private final Iterable<ContextType> contextTypes;

    protected ContextTypeCollection(Iterable<ContextType> contextTypes) {
        this.contextTypes = contextTypes;
    }

    public ContextType get(Model model, JsonPointer pointer) {
        for (ContextType next : contextTypes) {
            if (next.canProvideProposal(model, pointer)) {
                return next;
            }
        }
        return ContextType.UNKNOWN;
    }
}