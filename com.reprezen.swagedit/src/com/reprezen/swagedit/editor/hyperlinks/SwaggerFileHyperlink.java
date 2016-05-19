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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.io.CharStreams;
import com.reprezen.swagedit.editor.SwaggerDocument;

public class SwaggerFileHyperlink implements IHyperlink {

	private final IRegion linkRegion;
	private final String label;
	private final IFile targetFile;
	private final IFileStore targetStore;
	private final String pointer;

	public SwaggerFileHyperlink(IRegion linkRegion, String label, IFile targetFile, String pointer) {
		this.linkRegion = linkRegion;
		this.label = label;
		this.targetFile = targetFile;
		this.targetStore = null;
		this.pointer = pointer;
	}

	public SwaggerFileHyperlink(IRegion linkRegion, String label, IFileStore targetStore, String pointer) {
		this.linkRegion = linkRegion;
		this.label = label;
		this.targetFile = null;
		this.targetStore = targetStore;
		this.pointer = pointer;
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
		if (targetFile == null && targetStore == null)
			return;

		try {
			final IWorkbenchPage page = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage();

			IEditorPart editor;
			if (targetStore == null) {
				editor = IDE.openEditor(page, targetFile);
			} else {
				editor = IDE.openEditorOnFileStore(page, targetStore);
			}

			if (editor instanceof ITextEditor && pointer != null) {
				IRegion region = getTarget();
				if (region != null) {
					((ITextEditor) editor).selectAndReveal(region.getOffset(), region.getLength());
				}
			}
		} catch (ClassCastException | CoreException e) {
			e.printStackTrace();
		}
	}

	private IRegion getTarget() throws CoreException {
		SwaggerDocument doc;
		if (targetStore != null) {
			doc = getExternalDocument(targetStore.openInputStream(EFS.NONE, null));
		} else {
			doc = getExternalDocument(targetFile.getContents());
		}

		return doc.getRegion(pointer);
	}

	private SwaggerDocument getExternalDocument(InputStream content) {
		final SwaggerDocument doc = new SwaggerDocument();
		try {
			doc.set(CharStreams.toString(new InputStreamReader(content)));
		} catch (IOException e) {
			return null;
		}

		return doc;
	}

}
