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
package com.reprezen.swagedit.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.reprezen.swagedit.common.wizard.NewFileWizard;
import com.reprezen.swagedit.editor.SwaggerEditor;

public class SwagEditNewWizard extends NewFileWizard {

    @Override
    protected WizardNewFileCreationPage newFileCreationPage(IStructuredSelection selection) {
        return new SwagEditNewWizardPage(selection);
    }

    public SwagEditNewWizard() {
        super(SwaggerEditor.ID);
    }

}