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
package com.reprezen.swagedit.common.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

public abstract class NewFileWizard extends Wizard implements INewWizard {

    private WizardNewFileCreationPage page;
    private IStructuredSelection selection;
    private String editorId;

    abstract protected WizardNewFileCreationPage newFileCreationPage(IStructuredSelection selection);

    public NewFileWizard(String editorId) {
        super();
        this.editorId = editorId;
        setNeedsProgressMonitor(true);
    }

    /**
     * Adding the page to the wizard.
     */
    public void addPages() {
        page = newFileCreationPage(selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final IFile file = page.createNewFile();
        if (file == null || !file.exists()) {
            return false;
        }

        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, file, editorId);
                } catch (PartInitException e) {
                }
            }
        });

        return true;
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}