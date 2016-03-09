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
package com.reprezen.swagedit.validation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.json.JsonSchemaManager;

public class SwaggerError {

	public String schema;
	public String schemaPointer;
	public String instancePointer;
	public String keyword;
	public String message;

	public int level;
	public int line;
	public int indent = 0;
	private static final JsonNode swaggerSchema = new JsonSchemaManager().getSwaggerSchema().asJson();;

	public SwaggerError(int line, int level, String message) {
		this.line = line;
		this.level = level;
		this.message = message;
	}

	public SwaggerError(int level, String message) {
		this(1, level, message);
	}

	public SwaggerError(YAMLException exception) {
		this.level = IMarker.SEVERITY_ERROR;
		this.message = exception.getMessage();

		if (exception instanceof MarkedYAMLException) {
			this.line = ((MarkedYAMLException) exception).getProblemMark().getLine() + 1;
		} else {
			this.line = 1;
		}
	}

	public String getMessage() {
		return message;
	}

	public int getLevel() {
		return level;
	}

	public int getLine() {
		return line;
	}

	String getMessage(boolean withIndent) {
		if (withIndent) {
			final StringBuilder builder = new StringBuilder();
			builder.append(Strings.repeat("\t", indent));
			builder.append(" - ");
			builder.append(message);
			builder.append("\n");

			return builder.toString();
		}

		return message;
	}

	public static class MultipleSwaggerError extends SwaggerError {

		private final Map<String, Set<SwaggerError>> errors = new HashMap<>();

		public MultipleSwaggerError(int line, int level) {
			super(line, level, null);
		}

		public void put(String key, Set<SwaggerError> errors) {
			this.errors.put(key, errors);
		}

		public Map<String, Set<SwaggerError>> getErrors() {
			return errors;
		}

		@Override
		String getMessage(boolean withIndent) {
			return getMessage();
		}

		@Override
		public String getMessage() {
			Set<String> orderedErrorLocations = new TreeSet<>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					if (errors.get(o1).size() != errors.get(o2).size()) {
						return errors.get(o1).size() - errors.get(o2).size();
					}
					return o1.compareTo(o2);
				}
			});
			orderedErrorLocations.addAll(errors.keySet());

			final StringBuilder builder = new StringBuilder();
			final String tabs = Strings.repeat("\t", indent);

			builder.append(tabs);
			builder.append("Failed to match exactly one schema:");
			builder.append("\n");

			for (String location : orderedErrorLocations) {
				builder.append(tabs);
				builder.append(" - ");
				builder.append(getHumanFriendlyText(location));
				builder.append(":");
				builder.append("\n");

				for (SwaggerError e : errors.get(location)) {
					builder.append(e.getMessage(true));
				}
			}

			return builder.toString();
		}

		protected String getHumanFriendlyText(String location) {
			JsonNode swaggerSchemaNode = findNode(location);
			if (swaggerSchemaNode == null) {
				return location;
			}
			JsonNode title = swaggerSchemaNode.get("title");
			if (title != null) {
				return title.asText();
			}
			// "$ref":"#/definitions/headerParameterSubSchema"
			JsonNode ref = swaggerSchemaNode.get("$ref");
			if (ref != null) {
				return ref.asText().substring(ref.asText().lastIndexOf("/") + 1);
			}
			return location;
		}

		protected JsonNode findNode(String path) {
			JsonNode result = findNode(Lists.newLinkedList(Arrays.asList(path.split("/"))), swaggerSchema);
			return result;
		}

		protected JsonNode findNode(LinkedList<String> path, JsonNode root) {
			if (root == null) {
				return null;
			}
			// retrieves the first element, and also *removes* it
			String firstSegment = path.pop();
			if (Strings.isNullOrEmpty(firstSegment)) {
				return findNode(path, root);
			}
			int firstSegmentAsNumber = -1;
			try {
				firstSegmentAsNumber = Integer.parseInt(firstSegment);
			} catch (NumberFormatException e) {
				// ignore
			}
			JsonNode nodeForSegment = firstSegmentAsNumber == -1 ? root.get(firstSegment)
					: root.get(firstSegmentAsNumber);
			if (path.isEmpty()) {
				return nodeForSegment;
			}
			return findNode(path, nodeForSegment);
		}
	}

}
