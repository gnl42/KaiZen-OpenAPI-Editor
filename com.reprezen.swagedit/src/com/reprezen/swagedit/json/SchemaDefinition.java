package com.reprezen.swagedit.json;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaDefinition {
	public final JsonType type;
	public final JsonNode schema;
	public final JsonNode definition;

	public SchemaDefinition(JsonNode schema, JsonNode definition) {
		this.schema = schema;
		this.definition = definition;
		this.type = JsonType.valueOf(definition);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SchemaDefinition) {
			return Objects.equals(schema, ((SchemaDefinition) obj).schema) &&
					Objects.equals(definition, ((SchemaDefinition) obj).definition) &&
					Objects.equals(type, ((SchemaDefinition) obj).type);
		}

		return super.equals(obj);
	}
}