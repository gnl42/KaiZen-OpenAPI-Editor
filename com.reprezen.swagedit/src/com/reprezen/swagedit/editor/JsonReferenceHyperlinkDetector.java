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
package com.reprezen.swagedit.editor;

import static com.google.common.base.Strings.emptyToNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * Hyperlink detector that detects links from JSON references.
 *
 */
public class JsonReferenceHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

	protected static final Pattern LOCAL_REF_PATTERN = Pattern.compile("['|\"]?#([/\\w+]+)['|\"]?");
	protected static final Pattern EXTERN_REF_PATTERN = Pattern.compile("^['|\"]?(\\w+\\.y[a]?ml)#([/\\w*]*)['|\"]?");

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		SwaggerDocument document = (SwaggerDocument) textViewer.getDocument();

		String basePath;
		try {
			basePath = document.getPath(region);
		} catch (BadLocationException e) {
			basePath = null;
		}

		// not a json reference
		if (emptyToNull(basePath) == null || !basePath.endsWith("$ref")) {
			return null;
		}

		HyperlinkInfo info;
		try {
			info = getHyperlinkInfo(textViewer, region);
		} catch (BadLocationException e) {
			return null;
		}

		if (info == null) {
			return null;
		}

		String label = null;
		IRegion target = null;
		String ref = null;

		Matcher matcher = EXTERN_REF_PATTERN.matcher(info.text);
		if (matcher.matches()) {
			matcher.reset();

			// TODO link to external file
			String fileName = null;
			if (matcher.find()) {
				fileName = matcher.group(1);
				ref = matcher.group(2);
			}

		} else {
			// local reference
			matcher = LOCAL_REF_PATTERN.matcher(info.text);
			if (matcher.find()) {
				ref = matcher.group(1);
			}

			if (emptyToNull(ref) == null) {
				return null;
			}

			label = ref;
			target = document.getRegion(ref.replaceAll("/", ":"));
		}

		// no target means no hyperlink
		if (target == null) {
			return null;
		}

		return new IHyperlink[] { new SwaggerHyperlink(label, textViewer, info.region, target) };
	}

}
