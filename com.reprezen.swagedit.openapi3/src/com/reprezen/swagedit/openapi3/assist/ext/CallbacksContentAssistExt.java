/*******************************************************************************
 * Copyright (c) 2017 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.assist.ext;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.ProposalBuilder;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class CallbacksContentAssistExt implements ContentAssistExt {

    private final JsonPointer pointer = JsonPointer.compile("/definitions/callbacks");

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<ProposalBuilder> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        return Lists.newArrayList( //
                new ProposalBuilder("x-:").replacementString("x-").type("specificationExtension"));
    }

}
