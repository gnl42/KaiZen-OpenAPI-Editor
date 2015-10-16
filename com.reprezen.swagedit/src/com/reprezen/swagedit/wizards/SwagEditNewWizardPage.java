package com.reprezen.swagedit.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.reprezen.swagedit.Activator;

public class SwagEditNewWizardPage extends WizardNewFileCreationPage {

	private final String extension = "yaml";

	/**
	 * Constructor for SwagEditNewWizardPage.
	 * 
	 * @param selection
	 */
	public SwagEditNewWizardPage(IStructuredSelection selection) {
		super("SwagEditNewWizardPage", selection);
		setTitle("Swagger");
		setDescription("This wizard creates a new file with *.yaml extension that can be opened by the swagger editor.");
		setImageDescriptor(Activator.imageDescriptorFromPlugin("com.reprezen.swagedit", "icons/swagger_64.jpg"));
		setFileExtension(extension);
	}

}