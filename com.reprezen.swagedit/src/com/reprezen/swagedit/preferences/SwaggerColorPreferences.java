package com.reprezen.swagedit.preferences;

import org.dadacoalition.yedit.preferences.ColorPreferences;

import com.reprezen.swagedit.Activator;

/*
 * This implementation of preference page overrides the YEdit implementation but 
 * uses it's own preference store.
 * 
 */
public class SwaggerColorPreferences extends ColorPreferences {

	public SwaggerColorPreferences() {
		super();

        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Swagger Color Preferences for syntax highlighting");
	}

}
