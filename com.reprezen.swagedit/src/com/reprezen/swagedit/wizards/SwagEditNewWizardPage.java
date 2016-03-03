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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.Messages;

public class SwagEditNewWizardPage extends WizardNewFileCreationPage {

	private final String extension = "yaml";

	/**
	 * Constructor for SwagEditNewWizardPage.
	 * 
	 * @param selection
	 */
	public SwagEditNewWizardPage(IStructuredSelection selection) {
		super("SwagEditNewWizardPage", selection);
		setTitle(Messages.swagedit_wizard_title);
		setDescription(Messages.swagedit_wizard_description);
		setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/swagger_64.png"));
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