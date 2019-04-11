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

import static com.reprezen.swagedit.core.preferences.KaizenPreferencePage.VALIDATION_PREFERENCE_PAGE;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.VALIDATION_REF_SECURITY_SCHEME_OBJECT;

import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.editor.JsonDocument.Version;
import com.reprezen.swagedit.core.providers.PreferenceProvider;
import com.reprezen.swagedit.core.utils.ExtensionUtils;

public class SwaggerValidationPreferences extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage, IPropertyChangeListener {

    public SwaggerValidationPreferences() {
        // GRID is needed because we are not attaching the editor fields directly to
        // FieldEditorParent, but to its child
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Swagger preferences for validation:");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);

        GridLayoutFactory.fillDefaults() //
                .numColumns(2) //
                .margins(5, 8) //
                .spacing(5, 20) //
                .applyTo(composite);

        Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
        GridDataFactory.fillDefaults().span(2, 1).applyTo(group);

        group.setText("Allow JSON references in additional contexts");

        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_DEFINITIONS_OBJECT, "Security Definitions Object",
                group));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_SCHEME_OBJECT, "Security Scheme Object", group));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_REQUIREMENTS_ARRAY, "Security Requirements Array",
                group));
        addField(new BooleanFieldEditor(VALIDATION_REF_SECURITY_REQUIREMENT_OBJECT, "Security Requirement Object",
                group));

        // FieldEditor set parent layout to GridLayout with margin = 0
        ((GridLayout) group.getLayout()).marginTop = 8;
        ((GridLayout) group.getLayout()).marginBottom = 8;
        ((GridLayout) group.getLayout()).marginLeft = 8;
        ((GridLayout) group.getLayout()).marginRight = 8;

        // Validation

        Set<PreferenceProvider> providers = ExtensionUtils.getPreferenceProviders(VALIDATION_PREFERENCE_PAGE);
        providers.forEach(provider -> {
            for (FieldEditor field : provider.createFields(Version.SWAGGER, composite)) {
                addField(field);
            }
        });
    }

}
