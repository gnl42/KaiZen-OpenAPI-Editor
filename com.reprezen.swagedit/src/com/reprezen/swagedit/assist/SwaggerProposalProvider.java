package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.validation.SwaggerSchema;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider {

	private final ObjectMapper mapper = new ObjectMapper();
	private final SwaggerSchema schema = new SwaggerSchema();

	private final Styler typeStyler = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
		}
	};

	/**
	 * Returns a list of completion proposals that are created from a single
	 * proposal object.
	 * 
	 * @param path
	 * @param document
	 * @param prefix
	 * @param documentOffset
	 * @return list of completion proposals
	 */
	public List<? extends ICompletionProposal> getCompletionProposals(String path, JsonNode data, String prefix,int documentOffset) {
		final Set<JsonNode> definitions = schema.getDefinitions(path);
		final Set<JsonNode> proposals = createProposals(data, definitions);
		final List<ICompletionProposal> result = new ArrayList<>();

		prefix = Strings.emptyToNull(prefix);

		for (JsonNode proposal: proposals) {
			String value = proposal.get("value").asText();
			String label = proposal.get("label").asText();
			String type = proposal.has("type") ? proposal.get("type").asText() : null;

			StyledString styledString = new StyledString(label);
			if (type != null) {
				styledString
				.append(": ", typeStyler)
				.append(type, typeStyler);
			}

			if (prefix != null) {
				if (value.startsWith(prefix)) {
					value = value.substring(prefix.length(), value.length());
					result.add(new StyledCompletionProposal(value, styledString, documentOffset, 0, value.length()));
				}
			} else {
				result.add(new StyledCompletionProposal(value, styledString, documentOffset, 0, value.length()));
			}
		}

		return result;
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
	public Set<JsonNode> createProposals(JsonNode data, JsonNode definition) {
		if (definition == null) {
			return Collections.emptySet();
		}

		switch (schema.getType(definition)) {
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
	public Set<JsonNode> createProposals(JsonNode data, Set<JsonNode> definitions) {
		Set<JsonNode> proposals = new HashSet<>();
		for (JsonNode definition: definitions) {
			Set<JsonNode> pp = createProposals(data, definition);
			if (!pp.isEmpty()) {
				proposals.addAll(pp);
			}
		}

		return proposals;
	}

	private Set<JsonNode> createArrayProposal(JsonNode data, JsonNode definition) {
		final Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "-")
				.put("label", "-"));
		
		return proposals;
	}

	private Set<JsonNode> createOneOfProposal(JsonNode data, JsonNode definition) {
		return collectOneOf(data, definition, new LinkedHashSet<JsonNode>());
	}

	private Set<JsonNode> collectOneOf(JsonNode data, JsonNode oneOf, Set<JsonNode> acc) {
		JsonType type = schema.getType(oneOf);

		if (JsonType.ONE_OF == type) {
			for (JsonNode one : oneOf.get(JsonType.ONE_OF.getValue())) {
				acc.addAll(collectOneOf(data, JsonUtil.getRef(schema.asJson(), one), acc));
			}
		} else {
			acc.addAll(createProposals(data, JsonUtil.getRef(schema.asJson(), oneOf)));
		}

		return acc;
	}

	private Set<JsonNode> createEnumProposal(JsonNode data, JsonNode definition) {
		final Set<JsonNode> proposals = new LinkedHashSet<>();

		for (JsonNode literal : definition.get("enum")) {
			String value;
			if (literal.isBoolean()) {
				value = literal.asText();
			} else {
				value = "\"" + literal.asText() + "\"";
			}

			proposals.add(mapper.createObjectNode()
					.put("value", value)
					.put("label", literal.asText()));
		}

		return proposals;
	}

	private Set<JsonNode> createStringProposal(JsonNode data, JsonNode definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "\"\"")
				.put("label", "\"\""));

		return proposals;
	}

	private Set<JsonNode> createBooleanProposal(JsonNode data, JsonNode definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();
		proposals.add(mapper.createObjectNode()
				.put("value", "true")
				.put("label", "true"));
		proposals.add(mapper.createObjectNode()
				.put("value", "false")
				.put("label", "false"));

		return proposals;
	}

	private Set<JsonNode> createObjectProposal(JsonNode data, JsonNode definition) {
		Set<JsonNode> proposals = new LinkedHashSet<>();

		if (definition.has("properties")) {
			for (Iterator<String> it = definition.get("properties").fieldNames(); it.hasNext();) {
				String key = it.next();

				if (!data.has(key)) {			
					JsonNode value = definition.get("properties").get(key);

					JsonNode keyNode = mapper.createObjectNode()
							.put("value", key + ":")
							.put("label", key)
							.put("type", schema.getType(value).getValue());

					proposals.add(keyNode);
				}
			}
		}

		if (definition.has("patternProperties")) {
			for (Iterator<String> it = definition.get("patternProperties").fieldNames(); it.hasNext();) {
				String key = it.next();
				JsonNode value = definition.get("patternProperties").get(key);
				if (key.startsWith("^")) {
					key = key.substring(1);
				}

				JsonNode keyNode = mapper.createObjectNode()
						.put("value", key + ":")
						.put("label", key)
						.put("type", schema.getType(value).getValue());

				proposals.add(keyNode);
			}
		}

		if (proposals.isEmpty()) {
			proposals.add(mapper.createObjectNode()
					.put("value", "_key_" + ":")
					.put("label", "_key_"));
		}

		return proposals;
	}
}
