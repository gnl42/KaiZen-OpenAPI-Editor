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
package com.reprezen.swagedit.openapi3.editor;

import org.eclipse.jface.text.IDocument;

import com.reprezen.swagedit.core.editor.JsonDocumentProvider;
import com.reprezen.swagedit.openapi3.Activator;

public class OpenApi3DocumentProvider extends JsonDocumentProvider {

	public OpenApi3DocumentProvider() {
		super(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected IDocument createEmptyDocument() {
		return new OpenApi3Document();
	}

}
