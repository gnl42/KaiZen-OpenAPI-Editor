package com.reprezen.swagedit.core.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.reprezen.swagedit.core.Activator;

public abstract class TextDocumentMarkerResolution implements IMarkerResolution {

    /**
     * @return IRegion to be selected in the editor, can be null
     * @throws CoreException
     */
    public abstract IRegion processFix(IDocument document, IMarker marker) throws CoreException;

    public void run(IMarker marker) {
        try {
            IResource resource = marker.getResource();
            if (resource.getType() != IResource.FILE) {
                throw new CoreException(createStatus(null, "The editor is not a File: " + resource.getName()));
            }
            IFile file = (IFile) resource;
            ITextEditor editor = openTextEditor(file);
            IDocument document = editor.getDocumentProvider().getDocument(new FileEditorInput(file));
            if (document == null) {
                throw new CoreException(createStatus(null, "The document is null"));
            }
            IRegion region = processFix(document, marker);
            if (region != null) {
                editor.selectAndReveal(region.getOffset(), region.getLength());
            }
        } catch (CoreException e) {
            Activator.getDefault().getLog().log(e.getStatus());
        }
    }

    protected ITextEditor openTextEditor(IFile file) throws CoreException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part;
        try {
            part = IDE.openEditor(page, file, true);
        } catch (PartInitException e) {
            throw new CoreException(createStatus(e, "Cannot open editor"));
        }
        if (!(part instanceof ITextEditor)) {
            throw new CoreException(createStatus(null, "The editor is not TextEditor: " + part));
        }
        return (ITextEditor) part;
    }

    protected IStatus createStatus(Exception e, String msg) {
        return new Status(Status.ERROR, Activator.PLUGIN_ID, "Cannot process the quick fix: " + msg, e);
    }
}