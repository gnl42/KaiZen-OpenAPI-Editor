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

import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

import com.reprezen.swagedit.core.editor.JsonDocument;

/**
 * Client may implement this interface to provide custom preferences to KaiZen editors.
 * 
 * Implementations should provide an extension to the extension point <i>com.reprezen.swagedit.preferences</i>.
 *
 */
public interface PreferenceProvider {

    public static final String ID = "com.reprezen.swagedit.preferences";

    /**
     * Returns a list of editors that should be added to the preference page.
     * 
     * @param version
     *            of document
     * @param composite
     * @return
     */
    List<FieldEditor> createFields(JsonDocument.Version version, Composite composite);

    /**
     * Initializes provided preferences into the given preference store.
     * 
     * @param version
     *            of document
     * @param store
     *            to access preferences
     */
    void initializeDefaultPreferences(JsonDocument.Version version, IPreferenceStore store);

}
