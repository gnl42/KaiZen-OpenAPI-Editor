package com.reprezen.swagedit.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dadacoalition.yedit.editor.YEdit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;

public class SwaggerEditor extends YEdit {

	public static final String ID = "com.reprezen.swagedit.editor";

	private final Validator validator = new Validator();
	private ProjectionSupport projectionSupport;
	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;

	public SwaggerEditor() {
		super();
		setDocumentProvider(new SwaggerDocumentProvider());
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();

		SourceViewerConfiguration configuration = getSourceViewerConfiguration();

		if (configuration instanceof SwaggerSourceViewerConfiguration) {
			((SwaggerSourceViewerConfiguration) configuration).setEditor(this);
		}
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.install();

		// turn projection mode on
		viewer.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = viewer.getProjectionAnnotationModel();
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(),
				styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	public void updateFoldingStructure(List<Position> positions) {
		Annotation[] annotations = new Annotation[positions.size()];
		// this will hold the new annotations along
		// with their corresponding positions
		Map<Annotation, Position> newAnnotations = new HashMap<>();
		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}

		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = annotations;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		checkErrors();
	}

	@Override
	public void doSaveAs() {
		super.doSaveAs();
		checkErrors();
	}

	protected void checkErrors() {
		final IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		final IDocument document = getDocumentProvider().getDocument(getEditorInput());
		final List<SwaggerError> errors = validator.validate((SwaggerDocument) document);

		for (SwaggerError error : errors) {
			try {
				error.addMarker(file, document);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void handleEditorInputChanged() {
		super.handleEditorInputChanged();
	}

	public void redrawViewer() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (getSourceViewer() != null) {
					getSourceViewer().getTextWidget().redraw();
				}
			}
		});
	}

}
