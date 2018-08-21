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

import org.dadacoalition.yedit.YEditLog;
import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.editor.JsonEditor;
import com.reprezen.swagedit.core.validation.Validator;
import com.reprezen.swagedit.preferences.SwaggerPreferenceConstants;
import com.reprezen.swagedit.validation.SwaggerValidator;

/**
 * SwagEdit editor.
 * 
 */
public class SwaggerEditor extends JsonEditor {

    public static final String ID = "com.reprezen.swagedit.editor";
    
    protected final IPropertyChangeListener preferenceChangeListener = new JsonPreferenceChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            super.propertyChange(event);
            if (SwaggerPreferenceConstants.ALL_VALIDATION_PREFS.contains(event.getProperty())) {
                // Boolean comes from changing a value, String comes when restoring the default value as it uses
                // getDefaultString(name)
                boolean newValue = event.getNewValue() instanceof Boolean ? (Boolean) event.getNewValue()
                        : Boolean.valueOf((String) event.getNewValue());
                Activator.getDefault().getSchema().allowJsonRefInContext(event.getProperty(), newValue);
                try {
                    createValidationOperation(false).run(new NullProgressMonitor());
                } catch (CoreException e) {
                    YEditLog.logException(e);
                }
            }
        }
    };
  
    public SwaggerEditor() {
        super(new SwaggerDocumentProvider(), Activator.getDefault().getPreferenceStore());
    }
    
    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new SwaggerSourceViewerConfiguration(Activator.getDefault().getPreferenceStore());
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
    }
    
    @Override
    protected Validator createValidator() {
        return new SwaggerValidator();
    }
}