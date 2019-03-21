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
package com.reprezen.swagedit.editor;

import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.ALL_VALIDATION_PREFS;
import static com.reprezen.swagedit.preferences.SwaggerPreferenceConstants.EXAMPLE_VALIDATION;

import org.dadacoalition.yedit.YEditLog;
import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.editor.JsonEditor;
import com.reprezen.swagedit.core.validation.Validator;
import com.reprezen.swagedit.validation.SwaggerValidator;

/**
 * SwagEdit editor.
 * 
 */
public class SwaggerEditor extends JsonEditor {

    public static final String ID = "com.reprezen.swagedit.editor";

    private final IPropertyChangeListener validationChangeListener = event -> {

        if (ALL_VALIDATION_PREFS.contains(event.getProperty())) {

            if (!event.getProperty().equals(EXAMPLE_VALIDATION)) {
                boolean newValue = getPreferenceStore().getBoolean(event.getProperty());
                Activator.getDefault().getSchema().allowJsonRefInContext(event.getProperty(), newValue);
            }

            try {
                createValidationOperation(false).run(new NullProgressMonitor());
            } catch (CoreException e) {
                YEditLog.logException(e);
            }
        }
    };

    public SwaggerEditor() {
        super(new SwaggerDocumentProvider(), //
                // ZEN-4361 Missing marker location indicators (Overview Ruler) next to editor scrollbar in KZOE
                new ChainedPreferenceStore(new IPreferenceStore[] { //
                        Activator.getDefault().getPreferenceStore(), //
                        // Preferences store for EditorsPlugin has settings to show/hide the rules and markers
                        EditorsPlugin.getDefault().getPreferenceStore() }));

        getPreferenceStore().addPropertyChangeListener(validationChangeListener);
    }

    @Override
    public void dispose() {
        getPreferenceStore().removePropertyChangeListener(validationChangeListener);
        super.dispose();
    }

    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new SwaggerSourceViewerConfiguration(Activator.getDefault().getPreferenceStore());
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
    }

    @Override
    protected Validator createValidator() {
        Validator validator = new SwaggerValidator();
        validator.setExampleValidation(getPreferenceStore().getBoolean(EXAMPLE_VALIDATION));

        return validator;
    }
}