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

import java.util.ArrayList;
import java.util.List;
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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.templates.SwaggerContextType;

import io.swagger.models.HttpMethod;

public class SwaggerHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer viewer, IRegion region, boolean canShowMultipleHyperlinks) {
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		final HyperlinkPosition position = HyperlinkPosition.create(viewer, region);
		if (position == null) {
			return null;
		}

		final List<IHyperlink> links = new ArrayList<>();
		final String basePath = document.getPath(position.line, position.column);
		switch (Context.from(basePath)) {
		case PATH_PARAM:
			links.addAll(pathParamHyperlinks(viewer, document, position, basePath));
			break;
		case JSON_REF:
			links.addAll(referenceHyperlinks(viewer, document, position));
			break;
		default:
			links.addAll(definitionHyperlinks(viewer, document, position));
			break;
		}

		return links.size() > 0 ? links.toArray(new IHyperlink[links.size()]) : null;
	}

	private List<IHyperlink> referenceHyperlinks(ITextViewer viewer, SwaggerDocument document,
			HyperlinkPosition position) {
		final List<IHyperlink> links = new ArrayList<>();
		String content = position.content.replaceAll("'|\"", "");

		if (content.startsWith("#")) {
			// is local
			content = content.substring(1);
			String localPath = content.replaceAll("/", ":");

			IRegion link = new Region(position.getLinkOffset(), position.getLinkLength());
			IRegion region = document.getRegion(localPath);

			if (link != null && region != null) {
				links.add(new SwaggerHyperlink(content, viewer, link, region));
			}
		} else {
			// TODO
			// external references
		}

		return links;
	}

	/**
	 * Returns hyperlinks that target the definition of a path parameter.
	 * 
	 * @param viewer
	 * @param document
	 * @param position
	 * @param basePath
	 * @return array of links
	 */
	private List<IHyperlink> pathParamHyperlinks(ITextViewer viewer, SwaggerDocument document,
			HyperlinkPosition position, String basePath) {
		final List<IHyperlink> links = new ArrayList<>();

		// find the parameter selected by the user
		// parameter is define between {} so can be
		// match by a regex {(\w+)}
		Matcher matcher = Pattern.compile("\\{(\\w+)\\}").matcher(position.content);

		// find the group that is present under the
		// user's cursor (current column)
		String param = null;
		boolean isFound = false;
		int start = 0, end = 0, column = position.column;

		while (matcher.find() && !isFound) {
			if (matcher.start() <= column && matcher.end() >= column) {
				isFound = true;
				// remove {} is still present
				param = matcher.group().replaceAll("\\{|\\}", "");
				// keep for computing hyperlink region
				start = matcher.start();
				end = matcher.end();
			}
		}

		if (isFound && Strings.emptyToNull(param) != null) {
			JsonNode parent = document.getNodeForPath(basePath);
			// hyperlink region computed from position of parameter in selected
			// content
			IRegion linkRegion = new Region(position.getLinkOffset() + start, end - start);

			for (String path : findParameterPath(basePath, param, parent)) {
				IRegion targetRegion = document.getRegion(path);
				if (targetRegion != null) {
					links.add(new SwaggerHyperlink(position.content, viewer, linkRegion, targetRegion));
				}
			}
		}

		return links;
	}

	/**
	 * Returns hyperlinks that target a schema definitions. The hyperlink can be
	 * created from any word in the document that matches the key of the
	 * definitions object.
	 * 
	 * @param viewer
	 * @param document
	 * @param position
	 * @return array of links
	 */
	private List<IHyperlink> definitionHyperlinks(ITextViewer viewer, SwaggerDocument document,
			HyperlinkPosition position) {

		final List<IHyperlink> links = new ArrayList<>();
		final JsonNode found = document.asJson().at("/definitions/" + position.content);
		final String linkPath = found != null ? ":definitions:" + position.content : null;

		if (linkPath != null) {
			IRegion linkRegion = new Region(position.getLinkOffset(), position.getLinkLength());
			IRegion targetRegion = document.getRegion(linkPath);

			links.add(new SwaggerHyperlink(position.content, viewer, linkRegion, targetRegion));
		}

		return links;
	}

	private Iterable<String> findParameterPath(final String basePath, final String param, JsonNode parent) {
		if (parent == null || !parent.isObject())
			return Lists.newArrayList();

		final List<String> paths = new ArrayList<>();
		for (HttpMethod method : HttpMethod.values()) {
			String path = getParamPath(method, param, parent);
			if (path != null) {
				paths.add(path);
			}
		}

		return Iterables.transform(paths, new Function<String, String>() {
			@Override
			public String apply(String s) {
				return basePath + s;
			}
		});
	}

	private String getParamPath(HttpMethod method, final String param, JsonNode parent) {
		final String mName = method.name().toLowerCase();
		final JsonNode parameters = parent.at("/" + mName + "/parameters");
		final List<JsonNode> values = parameters.findValues("name");
		final JsonNode found = Iterables.find(values, new Predicate<JsonNode>() {
			@Override
			public boolean apply(JsonNode node) {
				return param.equals(node.asText());
			}
		}, null);

		return found != null ? ":" + mName + ":parameters:@" + values.indexOf(found) : null;
	}

	protected enum Context {
		PATH_PARAM, JSON_REF, OTHER;

		public static Context from(String path) {
			if (Strings.emptyToNull(path) == null)
				return OTHER;

			String contextType = SwaggerContextType.getContextType(path);
			if (SwaggerContextType.PathItemContextType.CONTEXT_ID.equals(contextType)) {
				return PATH_PARAM;
			} else if (path.endsWith("$ref")) {
				return JSON_REF;
			} else {
				return OTHER;
			}
		}

	}

	protected static class HyperlinkPosition {
		public final IRegion lineInfo;
		public final String content;
		public final int line;
		public final int column;
		public final int lastSpace;

		private HyperlinkPosition(IRegion lineInfo, String content, int line, int column, int lastSpace) {
			this.lineInfo = lineInfo;
			this.content = content;
			this.line = line;
			this.column = column;
			this.lastSpace = lastSpace;
		}

		public int getLinkOffset() {
			return lineInfo.getOffset() + lastSpace + 1;
		}

		public int getLinkLength() {
			return lineInfo.getLength() - (lastSpace + 1);
		}

		public static HyperlinkPosition create(ITextViewer viewer, IRegion region) {
			final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();

			IRegion lineInfo;
			String selectedContent;
			int column = 0;
			int line = 0;

			try {
				lineInfo = document.getLineInformationOfOffset(region.getOffset());
				line = document.getLineOfOffset(region.getOffset());
				selectedContent = document.get(lineInfo.getOffset(), lineInfo.getLength());
			} catch (BadLocationException ex) {
				return null;
			}

			final ISelection selection = viewer.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;

				try {
					column = textSelection.getOffset() - document.getLineOffset(textSelection.getStartLine());
				} catch (BadLocationException e) {
					// TODO: handle exception
				}
			}

			int lastSpace = selectedContent.lastIndexOf(" ");
			if (lastSpace > -1) {
				selectedContent = selectedContent.substring(lastSpace + 1, selectedContent.length());
			}

			return new HyperlinkPosition(lineInfo, selectedContent, line, column, lastSpace);
		}
	}
}
