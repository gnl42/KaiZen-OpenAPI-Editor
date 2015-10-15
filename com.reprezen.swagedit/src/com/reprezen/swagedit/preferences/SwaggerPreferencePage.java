package com.reprezen.swagedit.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reprezen.swagedit.Activator;

public class SwaggerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SwaggerPreferencePage() {
		super(GRID);
		setDescription("Swagger Preferences");
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub
		
	}

	

}
