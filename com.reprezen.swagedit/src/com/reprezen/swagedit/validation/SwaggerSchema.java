package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	 * Returns the set of definitions that matches the given path.
	 * 
	 * @param path
	 * @return set of definitions for the path
	 */
	public Set<JsonNode> getDefinitions(String path) {
		if (path.startsWith(":")) {
			path = path.substring(1);
		}

		final String[] segments = path.split(":");
		Set<JsonNode> definitions = Collections.emptySet();
		Set<JsonNode> toTraverse = Collections.singleton(asJson());

		// we iterate over all segments that form the path, 
		// each segment is use to traverse the schema, until
		// we find the definitions that correspond to the latest segment
		for (String segment: segments) {
			// we reset the definitions we found until now
			// only the latest are of interest.
			definitions = new HashSet<>();
			for (JsonNode traverse: toTraverse) {
				definitions.addAll( traverse(traverse, segment) );
			}
			// we keep the definitions we will 
			// need to process in the next iteration
			toTraverse = definitions;
		}

		return definitions;
	}

	/*
	 * This method takes for parameter the current JSON object, and the segment of 
	 * a path that should be use to traverse the current JSON object.
	 * 
	 * Returns the set of nodes that matches the current segment, after applying 
	 * the segment to the current node.
	 */
	private Set<JsonNode> traverse(JsonNode current, String segment) {
		final Set<JsonNode> definitions = new HashSet<>();

		// make sure the current node is not a ref.
		current = JsonUtil.getRef(asJson(), current);

		if (segment.isEmpty()) {
			return Collections.singleton(current);
		}

		// if it's an array, collect definitions 
		// from the property items.
		if (isArray(current, segment)) {
			definitions.add( JsonUtil.getRef(asJson(), current.get("items")) );
		}

		// if the node has properties, lookup for properties that
		// matches the segment
		if (current.has("properties") && current.get("properties").has(segment)) {
			definitions.add( JsonUtil.getRef(asJson(), current.get("properties").get(segment)) );
		}

		// if nothing found yet, collect pattern properties 
		// that match with the segment
		if (definitions.isEmpty() && current.has("patternProperties")) {
			definitions.addAll( traversePatternProperties(current, segment) );
		}

		// same with additional properties
		if (definitions.isEmpty() && current.has("additionalProperties")) {
			definitions.addAll( traverseAdditionalProperties(current, segment) );
		}

		// finally if the node is of type oneOf, traverse and collect
		// all definitions from the oneOf
		if (definitions.isEmpty() && current.has("oneOf")) {
			definitions.addAll( traverseOneOf(current, segment) );
		}

		return definitions;
	}

	/*
	 * Returns true if the segment matches a position in an array and 
	 * the current node is itself an array.
	 */
	private boolean isArray(JsonNode current, String segment) {
		return segment.startsWith("@") && JsonType.ARRAY == getType(current);
	}

	private Set<JsonNode> traversePatternProperties(JsonNode current, String path) {
		final Set<JsonNode> definitions = new HashSet<>();
		final JsonNode properties = current.get("patternProperties");
		final Iterator<String> it = properties.fieldNames();

		while (it.hasNext()) {
			String key = it.next();
			if (key.startsWith("^")) {
				if (path.startsWith(key.substring(1))) {
					if (properties.has(key)) {
						definitions.add( JsonUtil.getRef(asJson(), properties.get(key)) );
					}
				}
			}

			if (path.matches(key) && properties.has(key)) {
				definitions.add( JsonUtil.getRef(asJson(), properties.get(key)) );
			}
		}

		return definitions;
	}

	private Set<JsonNode> traverseAdditionalProperties(JsonNode current, String path) {
		final JsonNode properties = current.get("additionalProperties");
		if (properties.isObject()) {
			return Collections.singleton( JsonUtil.getRef(asJson(), properties) );
		}

		return Collections.emptySet();
	}

	private Set<JsonNode> traverseOneOf(JsonNode current, String path) {
		final Iterator<JsonNode> it = current.get("oneOf").elements();
		final Set<JsonNode> definitions = new HashSet<>();
		while (it.hasNext()) {
			JsonNode next = it.next();
			Set<JsonNode> found = traverse(JsonUtil.getRef(asJson(), next), path);
			definitions.addAll(found);
		}

		return definitions;
	}

}
