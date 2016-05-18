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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class SwaggerFileHyperlink implements IHyperlink {

	private final IFile targetFile;
	private final IRegion targetRegion;
	private final IRegion linkRegion;
	private final String label;

	public SwaggerFileHyperlink(IRegion linkRegion, String label, IFile targetFile, IRegion targetRegion) {
		this.linkRegion = linkRegion;
		this.label = label;
		this.targetFile = targetFile;
		this.targetRegion = targetRegion;
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
		try {
			ITextEditor openEditor = (ITextEditor) IDE.openEditor(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), 
					targetFile);

			if (targetRegion != null) {
				openEditor.selectAndReveal(targetRegion.getOffset(), targetRegion.getLength());
			}

		} catch (PartInitException | ClassCastException e) {
			e.printStackTrace();
		}
	}

}
