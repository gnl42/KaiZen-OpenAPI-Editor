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

import org.dadacoalition.yedit.preferences.ColorPreferences;

import com.reprezen.swagedit.openapi3.Activator;

/*
 * This implementation of preference page overrides the YEdit implementation but 
 * uses it's own preference store.
 * 
 */
public class OpenApiColorPreferences extends ColorPreferences {

    public OpenApiColorPreferences() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Swagger Color Preferences for syntax highlighting");
    }

}
