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
package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider extends AbstractProposalProvider {

    @Override
    protected Iterable<JsonNode> createProposals(JsonPointer path, Model model, int cycle) {
        System.out.println(path);
        AbstractNode node = model.find(path);

        return node.getProposals();
    }

}
