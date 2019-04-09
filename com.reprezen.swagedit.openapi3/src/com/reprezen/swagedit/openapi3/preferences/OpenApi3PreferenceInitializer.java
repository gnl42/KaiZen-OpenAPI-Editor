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
package com.reprezen.swagedit.openapi3.preferences;

import static com.reprezen.swagedit.openapi3.preferences.OpenApi3PreferenceConstants.ADVANCED_VALIDATION;

import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reprezen.swagedit.core.editor.JsonDocument.Version;
import com.reprezen.swagedit.core.preferences.JsonPreferenceInitializer;
import com.reprezen.swagedit.core.providers.PreferenceProvider;
import com.reprezen.swagedit.core.utils.ExtensionUtils;
import com.reprezen.swagedit.openapi3.Activator;

public class OpenApi3PreferenceInitializer extends JsonPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setColorPreferences(store);
        store.setDefault(ADVANCED_VALIDATION, true);

        Set<PreferenceProvider> providers = ExtensionUtils.getPreferenceProviders();
        providers.forEach(provider -> {
            provider.initializeDefaultPreferences(Version.OPENAPI, store);
        });
    }

}
