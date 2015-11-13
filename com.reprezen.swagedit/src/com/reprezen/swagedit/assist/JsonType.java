package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enumeration of JSON types found in a JSON schema.
 */
public enum JsonType {
	OBJECT("object"),
	ARRAY("array"),
	STRING("string"),
	NUMBER("number"),
	BOOLEAN("boolean"),
	ONE_OF("oneOf"),
	ENUM("enum"),
	UNDEFINED("undefined");
	
	private final String value;

	private JsonType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public static JsonType valueOf(JsonNode node) {
		String type = node.has("type") ? node.get("type").asText() :
			node.has("_type") ? node.get("_type").asText() : null;

		if (type != null) {
			for (JsonType jsonType: JsonType.values()) {
				if (type.equals( jsonType.getValue() )) {
					return jsonType;
				}
			}
		}

		return JsonType.UNDEFINED;
	}

}
