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

import org.eclipse.jface.preference.IPreferenceStore;

import com.reprezen.swagedit.core.preferences.JsonPreferenceInitializer;

public class OpenApi3PreferenceInitializer extends JsonPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = com.reprezen.swagedit.openapi3.Activator.getDefault().getPreferenceStore();
        setColorPreferences(store);
    }

}
