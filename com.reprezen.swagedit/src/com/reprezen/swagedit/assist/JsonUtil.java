package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {

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
	public static JsonNode getRef(JsonNode document, JsonNode refNode) {
		if (!isRef(refNode) || document == null)
			return refNode;

		JsonNode found = null;	
		final String ref = refNode.get("$ref").asText();
		final String[] keys = (ref.startsWith("#/") ? ref.substring(2) : ref).split("/");

		found = document;
		for (String key: keys) {
			JsonNode value = found.get(key);
			if (value != null)
				found = value;
		}

		return found != null ? found : refNode;
	}

}
