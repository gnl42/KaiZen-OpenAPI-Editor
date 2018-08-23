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

import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ParameterInContentAssistExt implements ContentAssistExt {

    private static final String description = "The location of the parameter. Possible values are \"query\", \"header\", \"path\" or \"cookie\"";
    private static final JsonPointer pointer = JsonPointer.compile("/definitions/parameter/properties/in");

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        return Arrays.asList( //
                new Proposal("query", "query", description, "string"),
                new Proposal("header", "header", description, "string"),
                new Proposal("path", "path", description, "string"),
                new Proposal("cookie", "cookie", description, "string"));
    }

}
