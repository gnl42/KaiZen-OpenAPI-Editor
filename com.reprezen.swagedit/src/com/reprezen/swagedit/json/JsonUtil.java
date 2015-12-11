package com.reprezen.swagedit.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.JsonSchemaManager.JSONSchema;

public class JsonUtil {

	private static final JsonSchemaManager schemaManager = new JsonSchemaManager();

	/**
	 * Returns true if the node is a reference to another node.
	 * 
	 * @param node
	 * @return true if is reference
	 */
	public static boolean isRef(JsonNode node) {
		return node.isObject() && node.has("$ref");
	}

	/**
	 * Returns the node that is referenced by the refNode.
	 * 
	 * @param document
	 * @param refNode
	 * @return referenced node
	 */
	public static SchemaDefinition getReference(JsonNode document, JsonNode refNode) {
		if (!isRef(refNode) || document == null)
			return new SchemaDefinition(document, refNode);

		JsonNode found = null;
		String ref = refNode.get("$ref").asText();

		if (ref.startsWith("http") || ref.startsWith("https")) {
			JSONSchema schema = schemaManager.getSchema(ref);
			if (schema != null) {
				document = schema.asJson();
			}
			ref = ref.substring(ref.indexOf("#"));
		}

		final String[] keys = (ref.startsWith("#/") ? ref.substring(2) : ref).split("/");

		found = document;
		for (String key : keys) {
			JsonNode value = found.get(key);
			if (value != null)
				found = value;
		}

		return new SchemaDefinition(document, found != null ? found : refNode);
	}

	

}
