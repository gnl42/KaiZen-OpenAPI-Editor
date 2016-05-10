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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.reprezen.swagedit.templates.SwaggerContextType;

public class SwaggerHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer viewer, IRegion region, boolean canShowMultipleHyperlinks) {
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		final ISelection selection = viewer.getSelectionProvider().getSelection();

		String candidate;
		IRegion lineInfo;
		IRegion linkRegion = null;
		IRegion targetRegion = null;

		int line = 0, column = 0;
		try {
			lineInfo = document.getLineInformationOfOffset(region.getOffset());
			candidate = document.get(lineInfo.getOffset(), lineInfo.getLength());			

			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;

				line = textSelection.getStartLine();
				try {
					column = textSelection.getOffset() - document.getLineOffset(line); 
				} catch (BadLocationException e) {
					// TODO: handle exception
				}
			}	
		} catch (BadLocationException e) {
			return null;
		}

		final String basePath = document.getPath(line, column);
		final String contextType = SwaggerContextType.getContextType(basePath);

		if (SwaggerContextType.PathItemContextType.CONTEXT_ID.equals(contextType)) {
			final Matcher matcher = Pattern.compile("\\{(\\w+)\\}").matcher(candidate);
			String param = null;
			boolean isParameter = false;
			while (matcher.find() && !isParameter) {
				if (matcher.start() <= column && matcher.end() >= column) {
					param = matcher.group();
					isParameter = true;
				}
			}
			if (isParameter && Strings.emptyToNull(param) != null) {
				param = param.replaceAll("\\{|\\}", "");
				String paramPath = findParameterPath(basePath, param, document.getNodeForPath(basePath));
				if (paramPath != null) {
					targetRegion = document.getRegion(paramPath);
					linkRegion = new Region(lineInfo.getOffset(), lineInfo.getLength());
				}
			}
		} else {
			int lastSpace = candidate.lastIndexOf(" ");
			if (lastSpace > -1) {
				candidate = candidate.substring(lastSpace + 1, candidate.length());
			}
			
			String linkPath = findDefinition(document.asJson(), candidate);
			targetRegion = document.getRegion(linkPath);
			linkRegion = new Region(lineInfo.getOffset() + lastSpace + 1, lineInfo.getLength() - (lastSpace + 1)); 
		}

		return linkRegion != null && targetRegion != null ? 
				new IHyperlink[] { new SwaggerHyperlink(candidate, viewer, linkRegion, targetRegion) } : 
					null;
	}

	private String findDefinition(JsonNode asJson, String candidate) {
		JsonNode definitions = asJson.get("definitions");
		if (definitions != null && definitions.isObject()) {
			if (definitions.get(candidate) != null) {
				return ":definitions:" + candidate;
			}
		}

		return null;
	}

	private String findParameterPath(String basePath, String param, JsonNode parent) {
		if (parent == null || !parent.isObject())
			return null;

		if (parent.has("get") && parent.get("get").has("parameters")) {
			JsonNode parameters = parent.get("get").get("parameters");
			if (parameters.isArray()) {
				int pos = 1;
				for (JsonNode parameter: parameters) {
					if (parameter.isObject() && parameter.has("name")) {
						String name = parameter.get("name").asText();
						if (Objects.equals(param, name)) {
							return basePath + ":get:parameters:@" + pos;
						}
					}
					pos++;
				}
			}
		}
		return null;
	}

}
