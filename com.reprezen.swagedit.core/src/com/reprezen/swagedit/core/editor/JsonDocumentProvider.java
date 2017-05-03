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
package com.reprezen.swagedit.core.editor;

import java.util.Set;

import org.dadacoalition.yedit.editor.ColorManager;
import org.dadacoalition.yedit.editor.scanner.YAMLToken;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public abstract class JsonDocumentProvider extends FileDocumentProvider {

	private final IPreferenceStore store;

	protected abstract IDocument createEmptyDocument();

	public JsonDocumentProvider(IPreferenceStore store) {
		this.store = store;
	}

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			JsonScanner scanner = new JsonScanner(new ColorManager(), store);
			Set<String> tokens = YAMLToken.VALID_TOKENS.keySet();
			FastPartitioner partitioner = new FastPartitioner(scanner, tokens.toArray(new String[tokens.size()]));
			document.setDocumentPartitioner(partitioner);
			partitioner.connect(document);
		}

		return document;
	}

}
