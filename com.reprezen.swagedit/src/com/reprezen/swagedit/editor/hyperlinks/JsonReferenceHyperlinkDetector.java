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

import static com.google.common.base.Strings.emptyToNull;

import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;

import com.reprezen.swagedit.editor.DocumentUtils;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.references.ExternalReference;
import com.reprezen.swagedit.json.references.JsonReference;

/**
 * Hyperlink detector that detects links from JSON references.
 *
 */
public class JsonReferenceHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

	protected static final Pattern LOCAL_REF_PATTERN = Pattern.compile("['|\"]?#([/\\w+]+)['|\"]?");

	@Override
	protected boolean canDetect(String basePath) {
		return emptyToNull(basePath) != null && basePath.endsWith("$ref");
	}

	@Override
	protected IHyperlink[] doDetect(SwaggerDocument doc, ITextViewer viewer, HyperlinkInfo info, String basePath) {
		FileEditorInput input = DocumentUtils
				.getActiveEditorInput();
		

		JsonReference reference = JsonReference.create(
				doc.asJson(),
				input != null ? input.getPath() : null,
				info.text.trim().replaceAll("'|\"", ""));

		if (!reference.isValid()) {
			return null;
		}

		if (reference instanceof ExternalReference) {		
			return new IHyperlink[] { 
				new SwaggerFileHyperlink(info.region, info.text, (ExternalReference) reference) 
			};
		} else {
			IRegion target = doc.getRegion(reference.pointerAsPath());
			if (target == null) {
				return null;
			}
			return new IHyperlink[] { 
				new SwaggerHyperlink(reference.pointer.toString(), viewer, info.region, target) 
			};
		}
	}

}
