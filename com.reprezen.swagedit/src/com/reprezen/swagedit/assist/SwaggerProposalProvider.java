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
package com.reprezen.swagedit.assist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonType;
import com.reprezen.swagedit.json.JsonUtil;
import com.reprezen.swagedit.json.SchemaDefinition;
import com.reprezen.swagedit.json.SchemaDefinitionProvider;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider extends AbstractProposalProvider {

	@Override
	protected Iterable<JsonNode> createProposals(String path, SwaggerDocument document, int cycle) {
		final SchemaDefinitionProvider walker = new SchemaDefinitionProvider();
		return createProposals(document.getNodeForPath(path), walker.getDefinitions(path));
	}

	/**
	 * Returns a list of proposals for the given data and schema definition.
	 * 
	 * A proposal is a JSON object of the following format: <code>
	 * {
	 *   value: string,
	 *   label: string
	 * }
	 * </code>
	 * 
	 * @param schema
	 * @param data
	 * @param definition
	 * @return proposal
	 */
	public Set<JsonNode> createProposals(JsonNode data, SchemaDefinition definition) {
		if (definition == null) {
			return Collections.emptySet();
		}

		switch (definition.type) {
		case OBJECT:
			return createObjectProposal(data, definition);
		case STRING:
			return createStringProposal(data, definition);
		case BOOLEAN:
			return createBooleanProposal(data, definition);
		case ENUM:
			return createEnumProposal(data, definition);
		case ONE_OF:
			return createOneOfProposal(data, definition);
		case ARRAY:
			return createArrayProposal(data, definition);
		case ANY_OF:
			return createAnyOfProposal(data, definition);
		case ALL_OF:
			return createAllOfProposal(data, definition);
		default:
			return Sets.newHashSet();
		}
	}

	/**
	 * Returns a list of proposals for the given data and set of schema definition.
	 * 
	 * @param data
	 * @param definitions
	 * @return proposals
	 */
	public Set<JsonNode> createProposals(JsonNode data, Set<SchemaDefinition> definitions) {
		Set<JsonNode> proposals = new HashSet<>();
		for (SchemaDefinition definition: definitions) {
			Set<JsonNode> pp = createProposals(data, definition);
			if (!pp.isEmpty()) {
				proposals.addAll(pp);
			}
		}

		return proposals;
	}

	private Set<JsonNode> createArrayProposal(JsonNode data, SchemaDefinition definition) {
		final Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "-")
				.put("label", "-")
				.put("type", "array item"));
		
		return proposals;
	}

	private Set<JsonNode> createOneOfProposal(JsonNode data, SchemaDefinition definition) {
		return collect(data, definition, new LinkedHashSet<JsonNode>());
	}
	
	private Set<JsonNode> createAnyOfProposal(JsonNode data, SchemaDefinition definition) {
		return collect(data, definition, new LinkedHashSet<JsonNode>());
	}
	
	private Set<JsonNode> createAllOfProposal(JsonNode data, SchemaDefinition definition) {
		return collect(data, definition, new LinkedHashSet<JsonNode>());
	}

	private Set<JsonNode> collect(JsonNode data, SchemaDefinition definition, Set<JsonNode> acc) {
		final JsonType type = definition.type;

		if (JsonType.ONE_OF == type || JsonType.ANY_OF == type) {
			if (definition.definition.has(type.getValue())) {
				final JsonNode all = definition.definition.get(type.getValue());

				if (all.isObject()) {
					acc.addAll(collect(data, JsonUtil.getReference(definition.schema, all), acc));
				} else if (all.isArray()) {
					for (JsonNode one: all) {
						acc.addAll(collect(data, JsonUtil.getReference(definition.schema, one), acc));
					}
				}
			}
		} else {
			acc.addAll(createProposals(data, JsonUtil.getReference(definition.schema, definition.definition)));
		}

		return acc;
	}

	private Set<JsonNode> createEnumProposal(JsonNode data, SchemaDefinition definition) {
		final Set<JsonNode> proposals = new LinkedHashSet<>();

		final String type = definition.definition.has("type") ? 
				definition.definition.get("type").asText() :
				null;

		for (JsonNode literal : definition.definition.get("enum")) {
			String value = literal.asText();

			// if the type of array is string and 
			// current value is a number, it should be put 
			// into quotes to avoid validation issues
			if (NumberUtils.isNumber(value) && "string".equals(type)) {
				value = "\"" + value + "\"";
			}

			proposals.add(mapper.createObjectNode()
					.put("value", value)
					.put("label", literal.asText()));
		}

		return proposals;
	}

	private Set<JsonNode> createStringProposal(JsonNode data, SchemaDefinition definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "")
				.put("label", "")
				.put("type", "string"));

		return proposals;
	}

	private Set<JsonNode> createBooleanProposal(JsonNode data, SchemaDefinition definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "true")
				.put("label", "true"));
		proposals.add(mapper.createObjectNode()
				.put("value", "false")
				.put("label", "false"));

		return proposals;
	}

	private Set<JsonNode> createObjectProposal(JsonNode data, SchemaDefinition definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();

		if (definition.definition.has("properties")) {
			final JsonNode properties = definition.definition.get("properties");

			for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
				final String key = it.next();

				if (!data.has(key)) {							
					proposals.add(createPropertyProposal(definition, key, properties.get(key)));
				}
			}
		}

		if (definition.definition.has("patternProperties")) {
			final JsonNode properties = definition.definition.get("patternProperties");

			for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
				String key = it.next();
				final JsonNode value = properties.get(key);

				if (key.startsWith("^")) {
					key = key.substring(1);
				}

				proposals.add(createPropertyProposal(definition, key, value));
			}
		}

		if (proposals.isEmpty()) {
			proposals.add(mapper.createObjectNode()
					.put("value", "_key_" + ":")
					.put("label", "_key_"));
		}

		return proposals;
	}

	private JsonNode createPropertyProposal(SchemaDefinition definition, String key, JsonNode value) {
		final SchemaDefinition resolvedDefinition = JsonUtil.getReference(definition.schema, value);
		final JsonType type = resolvedDefinition.type;

		return mapper.createObjectNode()
				.put("value", key + ":")
				.put("label", key)
				.put("type", type == JsonType.UNDEFINED && resolvedDefinition.descriptor != null ? 
						resolvedDefinition.descriptor : 
							type.getValue());
	}

}
