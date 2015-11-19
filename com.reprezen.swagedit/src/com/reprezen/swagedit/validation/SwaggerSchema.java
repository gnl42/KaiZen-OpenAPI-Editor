package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.reprezen.swagedit.assist.JsonType;
import com.reprezen.swagedit.assist.JsonUtil;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();

	private static JsonNode tree;
	private static JsonSchema schema;

	/**
	 * Returns swagger 2.0 schema
	 * 
	 * @return swagger schema.
	 */
	public synchronized JsonSchema getSchema() {
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
	public synchronized JsonNode asJson() {
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
	 * Returns the type of the node.
	 * 
	 * The node should be a schema definition that is part of 
	 * this schema.
	 * 
	 * @param node
	 * @return type
	 */
	public JsonType getType(JsonNode node) {		
		if (node == null)
			return null;

		if (JsonUtil.isRef(node)) {
			node = JsonUtil.getRef(asJson(), node);
		}

		if (node.has("oneOf")) {
			return JsonType.ONE_OF;
		}
		else if (node.has("enum")) {
			return JsonType.ENUM;
		}
		else if (node.has("type")) {
			return JsonType.valueOf(node);
		}
		else if (node.has("properties")) {
			return JsonType.OBJECT;
		}

		return JsonType.UNDEFINED;
	}

	/**
	 * Returns the schema definition that corresponds to the given path.
	 * 
	 * @param path
	 * @return schema definition
	 */
	public JsonNode getDefinitionForPath(String path) {
		if (path.startsWith(":")) {
			path = path.substring(1);
		}

		String[] paths = path.split(":");
		JsonNode definition = asJson();

		for (String current : paths) {
			JsonNode next = getDefinition(definition, current);
			if (next != null) {
				definition = next;
			}
		}

		return definition;
	}

	private JsonNode getDefinition(JsonNode parent, String path) {
		if (parent == null) {
			return null;
		}

		JsonNode definition = null;

		if (path.startsWith("@") && JsonType.ARRAY == getType(parent)) {
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
