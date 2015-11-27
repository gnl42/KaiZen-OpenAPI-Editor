package com.reprezen.swagedit.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dadacoalition.yedit.YEditLog;
import org.dadacoalition.yedit.editor.YEdit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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
import org.yaml.snakeyaml.error.YAMLException;

import com.reprezen.swagedit.validation.SwaggerError;
import com.reprezen.swagedit.validation.Validator;

/**
 * SwagEdit editor.
 * 
 */
public class SwaggerEditor extends YEdit {

	public static final String ID = "com.reprezen.swagedit.editor";

	private final Validator validator = new Validator();
	private ProjectionSupport projectionSupport;
	private Annotation[] oldAnnotations;
	private ProjectionAnnotationModel annotationModel;

	private final IDocumentListener changeListener = new IDocumentListener() {
		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {}
		@Override
		public void documentChanged(DocumentEvent event) {
			if (event.getDocument() instanceof SwaggerDocument) {
				final SwaggerDocument document = (SwaggerDocument) event.getDocument();

				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						document.onChange();
						validate();
					}
				});
			}
		}
	};

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
		
		getDocumentProvider().getDocument(getEditorInput()).addDocumentListener(changeListener);
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
		final Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>();
		for (Position position: positions) {
			newAnnotations.put(new ProjectionAnnotation(), position);
		}

		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);
		oldAnnotations = newAnnotations.keySet().toArray(new Annotation[0]);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		validate();
	}

	@Override
	public void doSaveAs() {
		super.doSaveAs();
		validate();
	}

	protected void validate() {
		IEditorInput editorInput = getEditorInput();
		IDocument document = getDocumentProvider().getDocument(getEditorInput());

		// if the file is not part of a workspace it does not seems that it is a
		// IFileEditorInput
		// but instead a FileStoreEditorInput. Unclear if markers are valid for
		// such files.
		if (!(editorInput instanceof IFileEditorInput)) {
			YEditLog.logError("Marking errors not supported for files outside of a project.");
			YEditLog.logger.info("editorInput is not a part of a project.");
			return;
		}
		if (document instanceof SwaggerDocument) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			clearMarkers(file);
			validateYaml(file, (SwaggerDocument) document);
			validateSwagger(file, (SwaggerDocument) document);
		}
	}

	protected void clearMarkers(IFile file) {
		int depth = IResource.DEPTH_INFINITE;
		try {
			file.deleteMarkers(IMarker.PROBLEM, true, depth);
		} catch (CoreException e) {
			YEditLog.logException(e);
			YEditLog.logger.warning("Failed to delete markers:\n" + e.toString());
		}
	}

	protected void validateYaml(IFile file, SwaggerDocument document) {
		if (document.getYamlError() instanceof YAMLException) {
			addMarker(SwaggerError.create((YAMLException) document.getYamlError()), file, document);
		}
	}

	protected void validateSwagger(IFile file, SwaggerDocument document) {
		final List<SwaggerError> errors = validator.validate(document);

		for (SwaggerError error : errors) {
			addMarker(error, file, document);
		}
	}

	private IMarker addMarker(SwaggerError error, IFile target, IDocument document) {
		IMarker marker = null;
		try {
			marker = target.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.SEVERITY, error.getLevel());
			marker.setAttribute(IMarker.MESSAGE, error.getMessage());
			marker.setAttribute(IMarker.LINE_NUMBER, error.getLine());
		} catch (CoreException e) {
			YEditLog.logException(e);
			YEditLog.logger.warning("Failed to create marker for syntax error: \n" + e.toString());
		}

		return marker;
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
