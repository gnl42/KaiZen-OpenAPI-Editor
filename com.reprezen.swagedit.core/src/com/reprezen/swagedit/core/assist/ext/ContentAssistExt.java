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
package com.reprezen.swagedit.core.assist.ext;

import java.util.Collection;

import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

/**
 * Implementation of this interface can be used to provide additional completion proposals that cannot be extracted from
 * a JSON schema.
 */
public interface ContentAssistExt {

    /**
     * Returns true if this extension can be used to provide proposals for the given type definition.
     * 
     * @param type
     * @return true if proposals
     */
    boolean canProvideContentAssist(TypeDefinition type);

    /**
     * Returns a collection of prposals
     * 
     * @param type
     * @param node
     * @param prefix
     * @return proposals
     */
    Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix);

}
