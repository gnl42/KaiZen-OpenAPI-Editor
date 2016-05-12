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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.swagger.models.HttpMethod;

/**
 * Hyperlink detector able to identify hyperlinks inside a Swagger document.
 * 
 */
public class SwaggerHyperlinkDetector extends AbstractHyperlinkDetector {

	protected static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(\\w+)\\}");
	protected static final Pattern LOCAL_REF_PATTERN = Pattern.compile("['|\\\"]?#([/\\w+]+)['|\\\"]?");

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer viewer, IRegion region, boolean canShowMultipleHyperlinks) {
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();

		HyperlinkInfo position;
		try {
			position = HyperlinkInfo.create(viewer, region);
		} catch (BadLocationException e) {
			return null;
		}

		if (position == null) {
			return null;
		}

		final HyperlinkInfo updated = HyperlinkType.
				from(position).
				computeLinks(document, position);

		final List<SwaggerHyperlink> links = new ArrayList<>();
		for (String path : updated.getLinks()) {
			IRegion target = document.getRegion(path);
			if (target != null) {
				links.add(new SwaggerHyperlink(updated.text, viewer, updated.region, target));
			}
		}

		return links.size() > 0 ? links.toArray(new IHyperlink[links.size()]) : null;
	}

	/**
	 * Enumeration of hyperlink types.
	 */
	protected enum HyperlinkType {

		PATH_PARAM {

			@Override
			public HyperlinkInfo computeLinks(SwaggerDocument document, HyperlinkInfo position) {
				String basePath;
				try {
					basePath = document.getPath(position.region);
				} catch (BadLocationException e) {
					return position;
				}

				List<String> links = new ArrayList<>();
				Matcher matcher = PARAMETER_PATTERN.matcher(position.text);

				String parameter = null;
				int start = 0, end = 0;
				while (matcher.find() && parameter == null) {
					if (matcher.start() <= position.column && matcher.end() >= position.column) {
						parameter = matcher.group(1);
						start = matcher.start();
						end = matcher.end();
					}
				}

				if (Strings.emptyToNull(parameter) == null) {
					return position;
				}

				JsonNode parent = document.getNodeForPath(basePath);
				Iterables.addAll(links, findParameterPath(basePath, parameter, parent));

				HyperlinkInfo result = new HyperlinkInfo(
						new Region(position.getOffset() + start, end - start),
						parameter, 
						position.column);
				result.setLinks(links);

				return result;
			}

			private Iterable<String> findParameterPath(final String basePath, final String param, JsonNode parent) {
				if (parent == null || !parent.isObject())
					return Lists.newArrayList();

				List<String> paths = new ArrayList<>();
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
				String mName = method.name().toLowerCase();
				JsonNode parameters = parent.at("/" + mName + "/parameters");
				List<JsonNode> values = parameters.findValues("name");
				JsonNode found = Iterables.find(values, new Predicate<JsonNode>() {
					@Override
					public boolean apply(JsonNode node) {
						return param.equals(node.asText());
					}
				}, null);

				return found != null ? ":" + mName + ":parameters:@" + values.indexOf(found) : null;
			}

		},

		JSON_REF {

			@Override
			public HyperlinkInfo computeLinks(SwaggerDocument document, HyperlinkInfo position) {
				Matcher matcher = LOCAL_REF_PATTERN.matcher(position.text);

				String ref = null;
				if (matcher.find()) {
					ref = matcher.group(1);
				}

				if (ref == null) {
					return position;
				}

				HyperlinkInfo result = new HyperlinkInfo(position.region, ref, position.column);
				result.links.add(ref.replaceAll("/", ":"));

				return result;
			}
		},

		OTHER {

			@Override
			public HyperlinkInfo computeLinks(SwaggerDocument document, HyperlinkInfo position) {
				final JsonNode found = document.asJson().at("/definitions/" + position.text);
				final String linkPath = found != null ? ":definitions:" + position.text : null;

				if (linkPath == null) {
					return position;
				}

				position.links.add(linkPath);
				return position;
			}
		};

		public static HyperlinkType from(HyperlinkInfo position) {
			if (PARAMETER_PATTERN.matcher(position.text).find()) {
				return PATH_PARAM;
			} else if (LOCAL_REF_PATTERN.matcher(position.text).find()) {
				return JSON_REF;
			}

			return OTHER;
		}

		public abstract HyperlinkInfo computeLinks(SwaggerDocument document, HyperlinkInfo position);
	}

	/**
	 * Contains details about potential hyperlinks inside a Swagger document.
	 */
	protected static class HyperlinkInfo {

		public final IRegion region;
		public final String text;

		private final int column;
		private final List<String> links = new ArrayList<>();

		HyperlinkInfo(IRegion region, String text, int column) {
			this.region = region;
			this.text = text;
			this.column = column;
		}

		public List<String> getLinks() {
			return links;
		}

		public void setLinks(List<String> links) {
			this.links.addAll(links);
		}

		public int getOffset() {
			return region.getOffset();
		}

		public int getLength() {
			return region.getLength();
		}

		public static HyperlinkInfo create(ITextViewer viewer, IRegion region) throws BadLocationException {
			final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
			final ITextSelection textSelection = (ITextSelection) viewer.getSelectionProvider().getSelection();

			// get offset of selected word
			// get length of selected word

			IRegion line = document.getLineInformationOfOffset(region.getOffset());

			final String lineContent = document.get(line.getOffset(), line.getLength());
			if (lineContent == null || emptyToNull(lineContent) == null) {
				return null;
			}

			final int column = textSelection.getOffset() - document.getLineOffset(textSelection.getStartLine());
			final IRegion selected = getSelectedRegion(line, lineContent, column);
			final String text = document.get(selected.getOffset(), selected.getLength());

			return new HyperlinkInfo(selected, text, column);
		}

		/**
		 * Returns the region containing the word selected or under the user's
		 * cursor.
		 * 
		 * @param region
		 * @param content
		 * @param column
		 * @return Region
		 */
		protected static Region getSelectedRegion(IRegion region, String content, int column) {
			// find next space or if not
			// after position is end of content.
			int end = content.indexOf(" ", column);
			if (end == -1) {
				end = content.length();
			}

			// find previous space
			// if not, start is 0.
			int start = 0;
			int idx = 0;
			do {
				idx = content.indexOf(" ", idx);
				if (idx != -1) {
					idx++;
					if (column >= idx) {
						start = idx;
					}
				}
			} while (start < column && idx != -1);

			int offset = region.getOffset() + start;
			int length = end - start;

			return new Region(offset, length);
		}
	}
}
