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
package com.reprezen.swagedit.editor.hyperlinks;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.references.ExternalReference;

public class SwaggerFileHyperlink implements IHyperlink {

	private final IRegion linkRegion;
	private final String label;
	private com.reprezen.swagedit.json.references.ExternalReference reference;

	public SwaggerFileHyperlink(IRegion linkRegion, String label, ExternalReference reference) {
		this.linkRegion = linkRegion;
		this.label = label;
		this.reference = reference;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return linkRegion;
	}

	@Override
	public String getTypeLabel() {
		return label;
	}

	@Override
	public String getHyperlinkText() {
		return label;
	}

	@Override
	public void open() {
		if (reference == null || !reference.isValid()) {
			return;
		}

		try {
			final IWorkbenchPage page = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage();

			IEditorPart editor = null;		
			IFile file = DocumentUtils.getWorkspaceFile(reference.path);
			if (file != null) {
				editor = IDE.openEditor(page, file);
			} else {
				IFileStore fileStore = DocumentUtils.getExternalFile(reference.path);
				if (fileStore != null && fileStore.fetchInfo().exists()) {
					try {
						editor = IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						// TODO: handle exception
					}
				}
			}

			if (editor instanceof ITextEditor) {
				IRegion region = getTarget();
				if (region != null) {
					((ITextEditor) editor).selectAndReveal(region.getOffset(), region.getLength());
				}
			}
		} catch (ClassCastException | CoreException e) {
			// TODO
		}
	}

	private IRegion getTarget() throws CoreException {
		SwaggerDocument doc = DocumentUtils.getDocument(reference.path);
		if (doc == null) {
			return null;
		}

		return doc.getRegion(reference.pointerAsPath());
	}

}
