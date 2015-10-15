package com.reprezen.swagedit.preferences;

import org.dadacoalition.yedit.Activator;
import org.dadacoalition.yedit.preferences.ColorPreferences;

public class SwaggerColorPreferences extends ColorPreferences {

	public SwaggerColorPreferences() {
		super();
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Swagger Color Preferences for syntax highlighting");
	}

}
