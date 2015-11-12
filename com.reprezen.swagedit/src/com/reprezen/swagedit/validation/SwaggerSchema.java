package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.reprezen.swagedit.assist.JsonUtil;
import com.reprezen.swagedit.assist.SwaggerProposalProvider;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();
	private final SwaggerProposalProvider proposalProvider = new SwaggerProposalProvider();

	private JsonNode tree;
	private JsonSchema schema;

	/**
	 * Returns swagger 2.0 schema
	 * 
	 * @return swagger schema.
	 */
	public JsonSchema getSchema() {
		if (schema == null) {
			final JsonNode schemaObject = asJson();

			if (schemaObject != null) {
				try {
					schema = factory.getJsonSchema(schemaObject);
				} catch (ProcessingException e) {
					e.printStackTrace();
				}
			}
		}

		return schema;
	}

	/**
	 * Returns the json tree representation of 
	 * the schema.
	 * 
	 * @return json
	 */
	public JsonNode asJson() {
		if (tree == null) {
			try {
				tree = jsonMapper.readTree(SwaggerSchema.class.getResourceAsStream("schema.json"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return tree;
	}

	/**
	 * Returns the proposal that matches the given path in the document.
	 * 
	 * @param path
	 * @param document
	 * @return proposal
	 */
	public JsonNode getProposals(String path, JsonNode document) {
		final Pair<JsonNode, JsonNode> dataAndDefinition = findDataAndDefintion(path, document); 
		JsonNode node = proposalProvider.get(asJson(), dataAndDefinition.getKey(), dataAndDefinition.getValue());
		return node;
	}

	private Pair<JsonNode, JsonNode> findDataAndDefintion(String path, JsonNode document) {
		if (path.startsWith(":")) {
			path = path.substring(1);
		}

		String[] paths = path.split(":");
		JsonNode node = document;
		JsonNode definition = asJson();

		for (String current : paths) {
			if (node.isArray() && current.startsWith("@")) {
				try {
					node = node.get(Integer.valueOf(current.substring(1)));
				} catch (NumberFormatException e) {
					node = null;
				}
			} else {
				node = node.path(current);
			}

			JsonNode next = getDefinition(definition, current);
			if (next != null) {
				definition = next;
			}
		}

		return new ImmutablePair<>(node, definition);
	}

	private JsonNode getDefinition(JsonNode parent, String path) {
		if (parent == null) {
			return null;
		}

		JsonNode definition = null;

		if (path.startsWith("@") && "array".equals(JsonUtil.getType(asJson(), parent))) {
			return JsonUtil.getRef(asJson(), parent.get("items"));
		}

		if (parent.has("properties")) {
			definition = parent.get("properties").get(path);
		}

		if (definition == null) {
			if (parent.has("patternProperties")) {
				JsonNode properties = parent.get("patternProperties");
				Iterator<String> it = properties.fieldNames();
				while (definition == null && it.hasNext()) {
					String key = it.next();
					if (key.startsWith("^")) {
						if (path.startsWith(key.substring(1))) {
							definition = properties.get(key);
						}
					}

					if (path.matches(key)) {
						definition = properties.get(key);
					}
				}
			} else if (parent.has("additionalProperties")) {
				JsonNode properties = parent.get("additionalProperties");
				if (properties.isObject()) {
					definition = JsonUtil.getRef(asJson(), properties);
				}
			}
		}
		
		if (definition == null && parent.has("oneOf")) {
			Iterator<JsonNode> it = parent.get("oneOf").elements();
			while (definition == null && it.hasNext()) {
				definition = getDefinition(JsonUtil.getRef(asJson(), it.next()), path);
			}
		}

		if (definition != null) {
			definition = JsonUtil.getRef(asJson(), definition);
		}

		return definition;
	}

}
