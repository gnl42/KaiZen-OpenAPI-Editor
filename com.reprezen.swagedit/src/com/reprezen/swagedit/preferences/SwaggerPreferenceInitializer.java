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
package com.reprezen.swagedit.preferences;

import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_SCHEME_OBJECT;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.preferences.JsonPreferenceInitializer;
import com.reprezen.swagedit.core.utils.ExtensionUtils;
import com.reprezen.swagedit.core.validation.Validator;

/*
 * SwagEdit default preference values.
 * 
 */
public class SwaggerPreferenceInitializer extends JsonPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setColorPreferences(store);

        store.setDefault(VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT, false);
        store.setDefault(VALIDATION_REF_SECURITY_SCHEME_OBJECT, false);
        store.setDefault(VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY, false);
        store.setDefault(VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT, false);

        ExtensionUtils.resolveProviders(Validator.VALIDATION_PROVIDERS_ID).forEach(provider -> {
            provider.initializeDefaultPreferences(false, store);
        });
    }

}
