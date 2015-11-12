package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * 
 * 
 *
 */
public class SwaggerProposalProvider {

	public final static String TYPE = "_type";

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Returns proposal for the given data and schema definition.
	 * 
	 * @param schema
	 * @param data
	 * @param definition
	 * @return proposal
	 */
	public JsonNode get(JsonNode schema, JsonNode data, JsonNode definition) {
		final String type = JsonUtil.getType(schema, definition);

		if (type != null) {
			switch (type) {
			case "object":
				return createObjectProposal(schema, data, definition);
			case "string":
				return createStringProposal(schema, data, definition);
			case "enum":
				return createEnumProposal(schema, data, definition);
			case "oneOf":
				return createOneOfProposal(schema, data, definition);
			case "array":
				return createArrayProposal(schema, data, definition);
			default:
				return null;
			}
		}

		return null;
	}

	private JsonNode createArrayProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		JsonNode proposal = mapper.createObjectNode()
				.put(TYPE, "array");

		return proposal;
	}

	private JsonNode createOneOfProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		final ArrayNode values = mapper.createArrayNode();
		
		List<JsonNode> collect = collect(schema, data, definition, new LinkedList<JsonNode>());
		Iterable<JsonNode> withOutObjects = Iterables.filter(collect, new Predicate<JsonNode>() {
			@Override
			public boolean apply(JsonNode node) {
				return !"object".equals( node.get(TYPE).asText() );
			}
			
		});
		Iterable<JsonNode> filter = Iterables.filter(collect, new Predicate<JsonNode>() {
			@Override
			public boolean apply(JsonNode node) {
				return "object".equals( node.get(TYPE).asText() );
			}
		});

		ObjectNode merged = mapper.createObjectNode()
				.put(TYPE, "object");

		ArrayNode keys = merged.putArray("keys");
		Set<JsonNode> allKeys = new HashSet<>();
		for (JsonNode n: filter) {
			ArrayNode otherKeys = (ArrayNode) n.get("keys");
			for (JsonNode k: otherKeys) {
				allKeys.add( k );
			}
		}

		keys.addAll(allKeys);

		Set<JsonNode> set = Sets.newHashSet(withOutObjects);
		set.add(merged);
		values.addAll(set);

		return mapper.createObjectNode()
				.put(TYPE, "oneOf")
				.set("oneOf", values);
	}

	private List<JsonNode> collect(JsonNode schema, JsonNode data, JsonNode oneOf, List<JsonNode> acc) {
		String type = JsonUtil.getType(schema, JsonUtil.getRef(schema, oneOf));
		
		if ("oneOf".equals(type)) {
			for (JsonNode one: oneOf.get("oneOf")) {
				acc.addAll(collect(schema, data, JsonUtil.getRef(schema, one), acc));
			}
		} else {
			acc.add(get(schema, data, JsonUtil.getRef(schema, oneOf)));
		}

		return acc;
	}

	private JsonNode createEnumProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		JsonNode proposal = mapper.createObjectNode()
				.put(TYPE, "enum")
				.set("literals", definition.get("enum"));

		return proposal;
	}

	private JsonNode createStringProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		return mapper.createObjectNode()
				.put(TYPE, "string");
	}

	private JsonNode createObjectProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		ObjectNode properties = mapper.createObjectNode()
				.put(TYPE, "object");

		ArrayNode keys = properties
				.putArray("keys");

		if (definition.has("properties")) {
			for (Iterator<String> it = definition.get("properties").fieldNames(); it.hasNext();) {
				String key = it.next();
				JsonNode value = definition.get("properties").get(key);
				String type = JsonUtil.getType(schema, value);
				String label = key + " - " + type;

				JsonNode keyNode = mapper.createObjectNode()
						.put("key", key)
						.put("type", type)
						.put("label", label);

				keys.add(keyNode);
			}
		}
		
		if (definition.has("patternProperties")) {
			for (Iterator<String> it = definition.get("patternProperties").fieldNames(); it.hasNext();) {
				String key = it.next();

				JsonNode value = definition.get("patternProperties").get(key);		
				String type = JsonUtil.getType(schema, value);

				if (key.startsWith("^")) {
					key = key.substring(1);
				}

				String label = key + " - " + type;
				JsonNode keyNode = mapper.createObjectNode()
						.put("key", key)
						.put("type", type)
						.put("label", label);

				keys.add(keyNode);
			}
		}
		
		return properties;
	}

	/**
	 * Returns a list of completion proposals that are created from a single proposal object.
	 * 
	 * @param proposal
	 * @param prefix
	 * @param offset
	 * @return list of completion proposals
	 */
	public List<ICompletionProposal> getProposals(JsonNode proposal, String prefix, int offset) {
		if (proposal == null || !proposal.has(TYPE))
			return Collections.emptyList();

		if (prefix != null && prefix.trim().isEmpty()) {
			prefix = null;
		}

		switch (proposal.get(TYPE).asText()) {
		case "string":
			return getStringProposals(proposal, prefix, offset);
		case "object":
			return getObjectProposals(proposal, prefix, offset);
		case "array":
			return getArrayProposals(proposal, prefix, offset);
		case "enum":
			return getEnumProposals(proposal, prefix, offset);
		case "oneOf":
			return getOneOfProposals(proposal, prefix, offset);
		default:
			return Collections.emptyList();
		}
	}

	private List<ICompletionProposal> getOneOfProposals(JsonNode proposal, String prefix, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		for (JsonNode p: proposal.get("oneOf")) {
			result.addAll(getProposals(p, prefix, offset));
		}
		return result;
	}

	private List<ICompletionProposal> getStringProposals(JsonNode proposal, String prefix, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		result.add(new CompletionProposal("''", offset, 0, "''".length(), null,"''", null, null));
		return result;
	}

	private List<ICompletionProposal> getArrayProposals(JsonNode proposal, String prefix, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		result.add(new CompletionProposal("-", offset, 0, "-".length(), null,"-", null, null));
		return result;
	}

	private List<ICompletionProposal> getEnumProposals(JsonNode proposal, String prefix, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		for (JsonNode literal: proposal.get("literals")) {
			final String value = literal.asText();
			
			if (prefix != null) {
				if (value.startsWith(prefix)) {
					final String replacement = value.substring(prefix.length(), value.length());
					result.add(new CompletionProposal(replacement, offset, 0, replacement.length(), null, value, null, null));
				}
			} else {
				result.add(new CompletionProposal(value, offset, 0, value.length(), null, value, null, null));
			}
		}

		return result;
	}

	private List<ICompletionProposal> getObjectProposals(JsonNode proposal, String prefix, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();

		for (final JsonNode key: proposal.get("keys")) {
			final String value = key.get("key").asText();
			final String label = key.get("label").asText();
			
			if (prefix != null) {
				if (value.startsWith(prefix)) {
					final String replacement = value.substring(prefix.length(), value.length());
					result.add(new CompletionProposal(replacement, offset, 0, replacement.length(), null, label, null, null));
				}
			} else {
				result.add(new CompletionProposal(value, offset, 0, value.length(), null, label, null, null));
			}
		}

		if (result.isEmpty()) {
			result.add(new CompletionProposal("_key_", offset, 0, "_key_".length(), null, "key", null, null));
		}

		return result;
	}

}
