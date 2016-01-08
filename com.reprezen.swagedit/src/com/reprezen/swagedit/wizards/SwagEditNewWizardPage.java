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
		setTitle(Messages.swagger);
		setDescription(Messages.wizard_description);
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