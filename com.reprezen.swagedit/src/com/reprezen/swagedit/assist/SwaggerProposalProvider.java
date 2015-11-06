package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SwaggerProposalProvider {

	public final static String TYPE = "_type";
	private final ObjectMapper mapper = new ObjectMapper();

	public JsonNode get(JsonNode schema, JsonNode data, JsonNode definition) {	
		switch (JsonUtil.getType(schema, definition)) {
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

	private JsonNode createArrayProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		JsonNode proposal = mapper.createObjectNode()
				.put(TYPE, "array");

		return proposal;
	}

	private JsonNode createOneOfProposal(JsonNode schema, JsonNode data, JsonNode definition) {
		final JsonNode oneOf = definition.get("oneOf");
		final ArrayNode values = mapper.createArrayNode();
		for (JsonNode one: oneOf) {
			values.add(get(schema, data, JsonUtil.getRef(schema, one)));
		}

		return mapper.createObjectNode()
				.put(TYPE, "oneOf")
				.set("oneOf", values);
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

		if (definition.has("properties")) {
			for (Iterator<String> it = definition.get("properties").fieldNames(); it.hasNext();) {
				String key = it.next();
				JsonNode value = definition.get("properties").get(key);

				if (!data.has(key)) {
					properties.put(key, JsonUtil.getType(schema, value));
				}
			}
		}
		
		return properties;
	}

	public List<ICompletionProposal> getProposals(JsonNode proposal, int offset) {
		if (proposal == null || !proposal.has(TYPE))
			return Collections.emptyList();

		switch (proposal.get(TYPE).asText()) {
		case "string":
			return getStringProposals(proposal, offset);
		case "object":
			return getObjectProposals(proposal, offset);
		case "array":
			return getArrayProposals(proposal, offset);
		case "enum":
			return getEnumProposals(proposal, offset);
		case "oneOf":
			return getOneOfProposals(proposal, offset);
		default:
			return Collections.emptyList();
		}
	}

	private List<ICompletionProposal> getOneOfProposals(JsonNode proposal, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		for (JsonNode p: proposal.get("oneOf")) {
			result.addAll(getProposals(p, offset));
		}
		return result;
	}

	private List<ICompletionProposal> getStringProposals(JsonNode proposal, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		result.add(new CompletionProposal("''", offset, 0, "''".length(), null,"''", null, null));
		return result;
	}

	private List<ICompletionProposal> getArrayProposals(JsonNode proposal, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		result.add(new CompletionProposal("-", offset, 0, "-".length(), null,"-", null, null));
		return result;
	}

	private List<ICompletionProposal> getEnumProposals(JsonNode proposal, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		for (JsonNode literal: proposal.get("literals")) {
			final String value = literal.asText();

			result.add(new CompletionProposal(value, offset, 0, value.length(), null,value, null, null));
		}

		return result;
	}

	private List<ICompletionProposal> getObjectProposals(JsonNode proposal, int offset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		for (Iterator<String> it = proposal.fieldNames(); it.hasNext();) {
			String key = it.next();

			if (!TYPE.equals(key)) {
				final JsonNode value = proposal.get(key);
				final String label = key + " " + value.asText();
	
				result.add(new CompletionProposal(key, offset, 0, key.length(), null, label, null, null));
			}
		}

		if (result.isEmpty()) {
			result.add(new CompletionProposal("object", offset, 0, "object".length(), null,"object",null, null));
		}
		
		return result;
	}
}
