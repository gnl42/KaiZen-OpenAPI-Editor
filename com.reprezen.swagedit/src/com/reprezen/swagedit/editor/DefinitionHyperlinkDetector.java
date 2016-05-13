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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Hyperlink detector that detects links to and inside schema definition elements.
 * 
 */
public class DefinitionHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

	protected static final String TAGS_PATTERN = "^[:\\W+|\\w+]*:tags([:\\W+|\\w+]+)";
	protected static final String REQUIRED_PATTERN = "^([:\\W+|\\w+]+)(:required[:\\W+|\\w+]+)";

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		SwaggerDocument document = (SwaggerDocument) textViewer.getDocument();

		String basePath;
		try {
			basePath = document.getPath(region);
		} catch (BadLocationException e) {
			basePath = null;
		}

		// not a definition or property
		if (emptyToNull(basePath) == null || !basePath.matches(REQUIRED_PATTERN) && !basePath.matches(TAGS_PATTERN)) {
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

		String targetPath;
		if (basePath.matches(REQUIRED_PATTERN)) {
			targetPath = getRequiredPropertyPath(document, info, basePath);
		} else {
			targetPath = getTagDefinitionPath(document, info, basePath);
		}

		if (targetPath == null) {
			return null;
		}

		IRegion target = document.getRegion(targetPath);
		if (target == null) {
			return null;
		}

		return new IHyperlink[] { new SwaggerHyperlink(info.text, textViewer, info.region, target) };
	}

	protected String getRequiredPropertyPath(SwaggerDocument document, HyperlinkInfo info, String basePath) {
		Matcher matcher = Pattern.compile(REQUIRED_PATTERN).matcher(basePath);
		String containerPath = null;
		if (matcher.find()) {
			containerPath = matcher.group(1);
		}

		if (emptyToNull(containerPath) == null) {
			return null;
		}

		JsonNode container = document.getNodeForPath(containerPath);
		if (container.at("/properties/" + info.text) != null) {
			return containerPath + ":properties:" + info.text;
		} else {
			return null;
		}
	}

	protected String getTagDefinitionPath(SwaggerDocument document, HyperlinkInfo info, String basePath) {
		String path = "/definitions/" + info.text;
		JsonNode definition = document.asJson().at(path);

		return definition != null ? ":definitions:" + info.text : null;
	}

}
