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

import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import com.reprezen.swagedit.openapi3.Activator;
import com.reprezen.swagedit.openapi3.editor.OpenApi3Editor;

public class OpenApiTemplatePreferences extends TemplatePreferencePage implements IWorkbenchPreferencePage {

    protected static class SwaggerEditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {

        public SwaggerEditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable,
                ContextTypeRegistry registry) {
            super(parent, template, edit, isNameModifiable, registry);
        }

        protected SourceViewer createViewer(Composite parent) {
            SourceViewer viewer = new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            SourceViewerConfiguration configuration = new OpenApi3Editor.OpenApi3SourceViewerConfiguration();
            viewer.configure(configuration);

            return viewer;
        }

    }

    public OpenApiTemplatePreferences() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTemplateStore(Activator.getDefault().getTemplateStore());
        setContextTypeRegistry(Activator.getDefault().getContextTypeRegistry());

    }

    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
        SwaggerEditTemplateDialog dialog = new SwaggerEditTemplateDialog(getShell(), template, edit, isNameModifiable,
                getContextTypeRegistry());
        if (dialog.open() == Window.OK) {
            return dialog.getTemplate();
        }
        return null;
    }

    protected boolean isShowFormatterSetting() {
        return false;
    }

}
