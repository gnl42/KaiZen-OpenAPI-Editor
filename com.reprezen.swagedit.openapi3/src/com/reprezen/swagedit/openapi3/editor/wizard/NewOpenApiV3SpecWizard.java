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
package com.reprezen.swagedit.openapi3.editor.wizard;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.reprezen.swagedit.common.wizard.NewFileWizard;
import com.reprezen.swagedit.openapi3.Activator;
import com.reprezen.swagedit.openapi3.editor.OpenApi3Editor;

public class NewOpenApiV3SpecWizard extends NewFileWizard {

    @Override
    protected WizardNewFileCreationPage newFileCreationPage(IStructuredSelection selection) {
        return new NewOpenApiV3SpecWizardPage(selection);
    }

    public NewOpenApiV3SpecWizard() {
        super(OpenApi3Editor.ID);
    }

    private static class NewOpenApiV3SpecWizardPage extends WizardNewFileCreationPage {

        private final String extension = "yaml";

        public NewOpenApiV3SpecWizardPage(IStructuredSelection selection) {
            super("SwagEditNewWizardPage", selection);
            setTitle("OpenAPI v3 Spec");
            setDescription(
                    "This wizard creates a new OpenAPI v3 Spec in YAML format, which can be opened in SwagEdit.");
            setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/openAPI_64.png"));
            setFileExtension(extension);
        }

        @Override
        protected InputStream getInitialContents() {
            try {
                return Activator.getDefault().getBundle().getEntry("/resources/default.yaml").openStream();
            } catch (IOException e) {
                return null;
            }
        }

    }

}