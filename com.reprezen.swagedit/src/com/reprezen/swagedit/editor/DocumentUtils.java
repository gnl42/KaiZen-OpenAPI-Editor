package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.google.common.io.CharStreams;

public class DocumentUtils {

	public static FileEditorInput getActiveEditorInput() {
		IEditorInput input = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.getActiveEditor()
				.getEditorInput();

		return input instanceof FileEditorInput ? (FileEditorInput) input : null;
	}

	public static IPath resolve(IPath base, String path) {
		IPath extPath = new Path(path);
		if (!extPath.isAbsolute()) {
			URI baseURI = base.toFile().toURI();
			URI resolvedURI;
			try {
				resolvedURI = baseURI.resolve(extPath.toOSString());
			} catch (IllegalArgumentException e) {
				return null;
			}

			extPath = new Path(resolvedURI.getPath());
		}
		return extPath;
	}

	public static SwaggerDocument getDocument(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(path);

		SwaggerDocument doc = null;
		if (file != null && file.exists() && file.getFileExtension().matches("ya?ml")) {
			doc = new SwaggerDocument();
			try {
				doc.set(CharStreams.toString(new InputStreamReader(file.getContents())));
			} catch (IOException | CoreException e) {
				return null;
			}
		}

		return doc;
	}

}
