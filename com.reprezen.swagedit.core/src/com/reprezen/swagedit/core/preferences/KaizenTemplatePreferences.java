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
package com.reprezen.swagedit.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public abstract class KaizenTemplatePreferences extends TemplatePreferencePage implements IWorkbenchPreferencePage {

    protected static class KaizenEditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {

        private final SourceViewerConfiguration sourceViewerConfiguration;

        public KaizenEditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable,
                ContextTypeRegistry registry, SourceViewerConfiguration sourceViewerConfiguration) {
            super(parent, template, edit, isNameModifiable, registry);

            this.sourceViewerConfiguration = sourceViewerConfiguration;
        }

        protected SourceViewer createViewer(Composite parent) {
            SourceViewer viewer = new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            viewer.configure(sourceViewerConfiguration);

            return viewer;
        }

    }

    protected final SourceViewerConfiguration sourceViewerConfiguration;

    public KaizenTemplatePreferences(SourceViewerConfiguration sourceViewerConfiguration,
            IPreferenceStore preferenceStore, TemplateStore templateStore, ContextTypeRegistry contextTypeRegistry) {
        this.sourceViewerConfiguration = sourceViewerConfiguration;
        setPreferenceStore(preferenceStore);
        setTemplateStore(templateStore);
        setContextTypeRegistry(contextTypeRegistry);
    }

    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
        KaizenEditTemplateDialog dialog = new KaizenEditTemplateDialog(getShell(), template, edit, isNameModifiable,
                getContextTypeRegistry(), sourceViewerConfiguration);
        if (dialog.open() == Window.OK) {
            return dialog.getTemplate();
        }
        return null;
    }

    protected boolean isShowFormatterSetting() {
        return false;
    }

}
