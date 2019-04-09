/*******************************************************************************
 * Copyright (c) 2019 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.providers;

import java.net.URI;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.validation.SwaggerError;

/**
 * Client can implement this interface to provide custom validators.
 * 
 * Implementations should be registered as extensions to the extension point <i>com.reprezen.swagedit.validator</i>.
 */
public interface ValidationProvider {

    public static final String ID = "com.reprezen.swagedit.validator";

    /**
     * Execute a validation on the node present in the document.
     * 
     * @param document
     *            which contains the node.
     * @param baseURI
     *            of the document.
     * @param node
     *            to be validated.
     * @return validation errors.
     */
    Set<SwaggerError> validate(JsonDocument document, URI baseURI, AbstractNode node);

    /**
     * Returns true if the validation should be performed.
     * 
     * @param document
     *            being validated.
     * @param preferenceStore
     *            holding all preferences.
     * @return true if validator is active.
     */
    boolean isActive(JsonDocument document, IPreferenceStore preferenceStore);

}
