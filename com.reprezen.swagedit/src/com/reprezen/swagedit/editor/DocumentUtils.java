package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
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
		if (!extPath.isAbsolute() && base != null) {
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
		if (path == null || !path.getFileExtension().matches("ya?ml")) {
			return null;
		}

		InputStream content = null;
		IFile file = getWorkspaceFile(path);
		if (file == null) {
			IFileStore store = getExternalFile(path);
			if (store != null) {
				try {
					content = store.openInputStream(EFS.NONE, null);
				} catch (CoreException e) {
					content = null;
				}
			}
		} else if (file.exists()) {
			try {
				content = file.getContents();
			} catch (CoreException e) {
				content = null;
			}
		}

		if (content == null) {
			return null;
		}

		SwaggerDocument doc = new SwaggerDocument();
		try {
			doc.set(CharStreams.toString(new InputStreamReader(content)));
		} catch (IOException e) {
			return null;
		}

		return doc;
	}

	public static IFile getWorkspaceFile(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin
				.getWorkspace()
				.getRoot();

		return root.getFileForLocation(path);
	}

	public static IFileStore getExternalFile(IPath path) {
		IFileStore fileStore = EFS
				.getLocalFileSystem()
				.getStore(path);

		IFileInfo fileInfo = fileStore.fetchInfo();

		return fileInfo != null && fileInfo.exists() ? fileStore : null;
	}

}
