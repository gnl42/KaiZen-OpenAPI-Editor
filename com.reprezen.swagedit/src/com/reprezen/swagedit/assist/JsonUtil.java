package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {

	public static boolean isRef(JsonNode node) {
		return node.isObject() && node.has("$ref");
	}

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

	public static String getType(JsonNode document, JsonNode node) {		
		if (document == null || node == null)
			return null;

		if (node.has("oneOf")) {
			return "oneOf";
		}
		else if (node.has("enum")) {
			return "enum";
		}
		else if (node.has("type")) {
			return node.get("type").asText();
		} 
		else if (isRef(node)) {
			return getType(document, getRef(document, node));
		} else if (node.has("properties")) {
			return "object";
		}

		return null;
	}

}
