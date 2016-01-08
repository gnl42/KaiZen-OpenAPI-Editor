package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
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
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.SchemaDefinitionProvider;
import com.reprezen.swagedit.json.JsonType;
import com.reprezen.swagedit.json.JsonUtil;
import com.reprezen.swagedit.json.SchemaDefinition;

/**
 * Provider of completion proposals.
 */
public class SwaggerProposalProvider {

	private final ObjectMapper mapper = new ObjectMapper();

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
	public Collection<? extends ICompletionProposal> getCompletionProposals(String path, SwaggerDocument document, String prefix, int documentOffset) {
		final List<ICompletionProposal> result = new ArrayList<>();
		final Set<JsonNode> proposals;
		final SchemaDefinitionProvider walker = new SchemaDefinitionProvider();

		if (path.endsWith("$ref")) {
			proposals = createReferenceProposals(document.asJson());
		} else {
			proposals = createProposals(document.getNodeForPath(path), walker.getDefinitions(path));
		}

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

	public List<? extends ICompletionProposal> getCompletionProposals(String path, JsonNode data, String prefix,int documentOffset) {
		final SchemaDefinitionProvider walker = new SchemaDefinitionProvider();
		final Set<SchemaDefinition> definitions = walker.getDefinitions(path);
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

	public Set<JsonNode> createReferenceProposals(JsonNode document) {
		final Set<JsonNode> proposals = new LinkedHashSet<>();

		if (document.has("definitions")) {
			JsonNode definitions = document.get("definitions");
			
			for (Iterator<String> it = definitions.fieldNames(); it.hasNext();) {
				String key = it.next();
				
				proposals.add( mapper.createObjectNode()
						.put("value", "\"#/definitions/" + key + "\"")
						.put("label", key)
						.put("type", "ref") );
			}
			
		}

		return proposals;
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
				.put("label", "-"));
		
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
				.put("value", "\"\"")
				.put("label", "\"\""));

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
			for (Iterator<String> it = definition.definition.get("properties").fieldNames(); it.hasNext();) {
				String key = it.next();

				if (!data.has(key)) {			
					JsonNode value = definition.definition.get("properties").get(key);

					JsonNode keyNode = mapper.createObjectNode()
							.put("value", key + ":")
							.put("label", key)
							.put("type", JsonType.valueOf(value).getValue());

					proposals.add(keyNode);
				}
			}
		}

		if (definition.definition.has("patternProperties")) {
			for (Iterator<String> it = definition.definition.get("patternProperties").fieldNames(); it.hasNext();) {
				String key = it.next();
				JsonNode value = definition.definition.get("patternProperties").get(key);
				if (key.startsWith("^")) {
					key = key.substring(1);
				}

				JsonNode keyNode = mapper.createObjectNode()
						.put("value", key + ":")
						.put("label", key)
						.put("type", JsonType.valueOf(value).getValue());

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
