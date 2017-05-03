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

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.editor.JsonEditor;

/**
 * SwagEdit editor.
 * 
 */
public class SwaggerEditor extends JsonEditor {

    public static final String ID = "com.reprezen.swagedit.editor";
  
    public SwaggerEditor() {
        super(new SwaggerDocumentProvider());
    }
    
    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new SwaggerSourceViewerConfiguration(Activator.getDefault().getPreferenceStore());
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
    }
    
}