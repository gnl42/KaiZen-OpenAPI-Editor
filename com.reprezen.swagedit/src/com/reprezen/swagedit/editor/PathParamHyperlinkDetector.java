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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.json.JsonUtil;

import io.swagger.models.HttpMethod;

/**
 * Hyperlink detector that detects links from path parameters.
 *
 */
public class PathParamHyperlinkDetector extends AbstractSwaggerHyperlinkDetector {

	protected static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(\\w+)\\}");

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		SwaggerDocument document = (SwaggerDocument) textViewer.getDocument();

		String basePath;
		try {
			basePath = document.getPath(region);
		} catch (BadLocationException e) {
			basePath = null;
		}

		// not a path definition
		if (emptyToNull(basePath) == null || !basePath.startsWith(":paths:")) {
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

		// find selected parameter
		Matcher matcher = PARAMETER_PATTERN.matcher(info.text);
		String parameter = null;
		int start = 0, end = 0;
		while (matcher.find() && parameter == null) {
			if (matcher.start() <= info.column && matcher.end() >= info.column) {
				parameter = matcher.group(1);
				start = matcher.start();
				end = matcher.end();
			}
		}

		// no parameter found
		if (emptyToNull(parameter) == null) {
			return null;
		}

		Iterable<String> targetPaths = findParameterPath(document, basePath, parameter);
		IRegion linkRegion = new Region(info.getOffset() + start, end - start);

		List<IHyperlink> links = new ArrayList<>();
		for (String path: targetPaths) {
			IRegion target = document.getRegion(path);
			if (target != null) {
				links.add(new SwaggerHyperlink(parameter, textViewer, linkRegion, target));
			}
		}

		return links.isEmpty() ? null : links.toArray(new IHyperlink[links.size()]);
	}

	private Iterable<String> findParameterPath(final SwaggerDocument document, final String basePath, String parameter) {
		JsonNode parent = document.getNodeForPath(basePath);

		if (parent == null || !parent.isObject())
			return Lists.newArrayList();

		List<String> paths = new ArrayList<>();
		for (HttpMethod method : HttpMethod.values()) {
			String mName = method.name().toLowerCase();
			JsonNode parameters = parent.at("/" + mName + "/parameters");

			if (parameters != null && parameters.isArray()) {		

				for (int i = 0; i < parameters.size(); i++) {
					JsonNode current = parameters.get(i);

					if (JsonUtil.isRef(current)) {

						JsonPointer ptr = getPointer(current);
						JsonNode resolved = document.asJson().at(ptr);

						if (resolved != null && resolved.isObject() && resolved.has("name")) {
							if (parameter.equals( resolved.get("name").asText() )) {
								paths.add(ptr.toString().replaceAll("/", ":"));
							}
						}

					} else if (current.isObject() && current.has("name")) {

						if (parameter.equals( current.get("name").asText() )) {
							paths.add(basePath + ":" + mName + ":parameters:@" + i);
						}
					}
				}
			}
		}

		return paths;
	}

	protected JsonPointer getPointer(JsonNode ref) {
		String asText = ref.get("$ref").asText();
		if (asText.startsWith("#"))
			asText = asText.substring(1);

		return JsonPointer.compile(asText);
	}

}
