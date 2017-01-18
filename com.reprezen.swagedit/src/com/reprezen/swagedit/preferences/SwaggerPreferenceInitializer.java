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

import org.dadacoalition.yedit.preferences.PreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import com.reprezen.swagedit.Activator;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.*;

/*
 * SwagEdit default preference values.
 * 
 */
public class SwaggerPreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_DEFAULT, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.BOLD_DEFAULT, false);
        store.setDefault(PreferenceConstants.ITALIC_DEFAULT, false);
        store.setDefault(PreferenceConstants.UNDERLINE_DEFAULT, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_DEFAULT, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.BOLD_DEFAULT, false);
        store.setDefault(PreferenceConstants.ITALIC_DEFAULT, false);
        store.setDefault(PreferenceConstants.UNDERLINE_DEFAULT, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_COMMENT, new RGB(57, 127, 98));
        store.setDefault(PreferenceConstants.BOLD_COMMENT, false);
        store.setDefault(PreferenceConstants.ITALIC_COMMENT, false);
        store.setDefault(PreferenceConstants.UNDERLINE_COMMENT, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_KEY, new RGB(130, 0, 82));
        store.setDefault(PreferenceConstants.BOLD_KEY, true);
        store.setDefault(PreferenceConstants.ITALIC_KEY, false);
        store.setDefault(PreferenceConstants.UNDERLINE_KEY, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_DOCUMENT, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.BOLD_DOCUMENT, false);
        store.setDefault(PreferenceConstants.ITALIC_DOCUMENT, false);
        store.setDefault(PreferenceConstants.UNDERLINE_DOCUMENT, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_SCALAR, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.BOLD_SCALAR, false);
        store.setDefault(PreferenceConstants.ITALIC_SCALAR, false);
        store.setDefault(PreferenceConstants.UNDERLINE_SCALAR, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_ANCHOR, new RGB(175, 0, 255));
        store.setDefault(PreferenceConstants.BOLD_ANCHOR, false);
        store.setDefault(PreferenceConstants.ITALIC_ANCHOR, false);
        store.setDefault(PreferenceConstants.UNDERLINE_ANCHOR, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_ALIAS, new RGB(175, 0, 255));
        store.setDefault(PreferenceConstants.BOLD_ALIAS, false);
        store.setDefault(PreferenceConstants.ITALIC_ALIAS, false);
        store.setDefault(PreferenceConstants.UNDERLINE_ALIAS, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_TAG_PROPERTY, new RGB(175, 0, 255));
        store.setDefault(PreferenceConstants.BOLD_TAG_PROPERTY, false);
        store.setDefault(PreferenceConstants.ITALIC_TAG_PROPERTY, false);
        store.setDefault(PreferenceConstants.UNDERLINE_TAG_PROPERTY, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_INDICATOR_CHARACTER, new RGB(0, 0, 0));
        store.setDefault(PreferenceConstants.BOLD_INDICATOR_CHARACTER, false);
        store.setDefault(PreferenceConstants.ITALIC_INDICATOR_CHARACTER, false);
        store.setDefault(PreferenceConstants.UNDERLINE_INDICATOR_CHARACTER, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.COLOR_CONSTANT, new RGB(45, 32, 244));
        store.setDefault(PreferenceConstants.BOLD_CONSTANT, true);
        store.setDefault(PreferenceConstants.ITALIC_CONSTANT, false);
        store.setDefault(PreferenceConstants.UNDERLINE_CONSTANT, false);
        
        store.setDefault(VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT, true);
        store.setDefault(VALIDATION_REF_SECURITY_SCHEME_OBJECT, true);
        store.setDefault(VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY, false);
        store.setDefault(VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT, false);
    }

}
