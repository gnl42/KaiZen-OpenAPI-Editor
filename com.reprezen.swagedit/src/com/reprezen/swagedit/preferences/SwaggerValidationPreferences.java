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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reprezen.swagedit.Activator;

public class SwaggerValidationPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public SwaggerValidationPreferences() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Swagger preferences for validation");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT, "Security Definitions Object", getFieldEditorParent()));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_SCHEME_OBJECT, "Security Scheme Object", getFieldEditorParent()));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY, "Security Requirements Array", getFieldEditorParent()));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT, "Security Requirement Object", getFieldEditorParent()));
    }

}
