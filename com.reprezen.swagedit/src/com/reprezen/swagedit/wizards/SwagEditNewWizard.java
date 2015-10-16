package com.reprezen.swagedit.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class SwagEditNewWizard extends Wizard implements INewWizard {

	private SwagEditNewWizardPage page;
	private IStructuredSelection selection;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public SwagEditNewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new SwagEditNewWizardPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		final IFile file = page.createNewFile();
		if (file != null && file.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream() {
		String contents =
				"swagger: '2.0'\n" +
				"info:\n" +
				"  version: 1.0.0\n" +
				"  title: Sample\n"+
				"  description: |";

		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}