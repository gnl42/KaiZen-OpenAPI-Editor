package com.reprezen.swagedit.editor;

import java.util.List;

import org.dadacoalition.yedit.editor.YEdit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;

public class SwaggerEditor extends YEdit {

	public static final String ID = "com.reprezen.swagedit.editor";

	private final Validator validator = new Validator();

	public SwaggerEditor() {
		super();
		setDocumentProvider(new SwaggerDocumentProvider());
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		checkErrors();
	}

	protected void checkErrors() {
		final IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		final IDocument document = getDocumentProvider().getDocument(getEditorInput());
		final String content = document.get();
		final List<SwaggerError> errors = validator.validate(content);

		for (SwaggerError error: errors) {
			try {
				error.addMarker(file, document);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		super.doSaveAs();
	}

	@Override
	protected void handleEditorInputChanged() {
		super.handleEditorInputChanged();
	}

}
