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
import static com.reprezen.swagedit.openapi3.preferences.OpenApi3PreferenceConstants.EXAMPLE_VALIDATION;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reprezen.swagedit.openapi3.Activator;

public class OpenApi3ValidationPreferences extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, IPropertyChangeListener {

    public OpenApi3ValidationPreferences() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("OpenAPI preferences for validation:");
    }

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    protected void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).applyTo(composite);

        addField(new BooleanFieldEditor(ADVANCED_VALIDATION, "Enable advanced validation",
                composite));
        addField(new BooleanFieldEditor(EXAMPLE_VALIDATION, "Enable examples validation", composite));
    }
}
