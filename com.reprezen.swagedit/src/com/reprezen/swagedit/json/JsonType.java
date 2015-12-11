package com.reprezen.swagedit.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enumeration of JSON types found in a JSON schema.
 */
public enum JsonType {
	OBJECT("object"),
	ARRAY("array"),
	STRING("string"),
	NUMBER("number"),
	INTEGER("integer"),
	BOOLEAN("boolean"),
	ONE_OF("oneOf"),
	ENUM("enum"),
	ANY_OF("anyOf"),
	ALL_OF("allOf"),
	UNDEFINED("undefined");
	
	private final String value;

	private JsonType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public static JsonType valueOf(JsonNode node) {
		if (node == null) {
			return JsonType.UNDEFINED;
		}
		else if (node.has("oneOf")) {
			return JsonType.ONE_OF;
		}
		else if (node.has("enum")) {
			return JsonType.ENUM;
		}
		else if (node.has("type")) {
			String type = node.has("type") ? node.get("type").asText() : null;

			if (type != null) {
				for (JsonType jsonType: JsonType.values()) {
					if (type.equals( jsonType.getValue() )) {
						return jsonType;
					}
				}
			}
		}
		else if (node.has("properties")) {
			return JsonType.OBJECT;
		}
		else if (node.has("anyOf")) {
			return JsonType.ANY_OF;
		}
 
		return JsonType.UNDEFINED;
	}

}
