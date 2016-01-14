package com.reprezen.swagedit.json;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

public class SchemaDefinition {
	public final JsonType type;
	public final JsonNode schema;
	public final JsonNode definition;
	public final String descriptor;

	public SchemaDefinition(JsonNode schema, JsonNode definition) {
		this(schema, definition, null);
	}

	public SchemaDefinition(JsonNode schema, JsonNode definition, String descriptor) {
		this.schema = schema;
		this.definition = definition;
		this.type = JsonType.valueOf(definition);
		this.descriptor = Strings.emptyToNull(descriptor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SchemaDefinition) {
			return Objects.equals(schema, ((SchemaDefinition) obj).schema) &&
					Objects.equals(definition, ((SchemaDefinition) obj).definition) &&
					Objects.equals(type, ((SchemaDefinition) obj).type) &&
					Objects.equals(descriptor, ((SchemaDefinition) obj).descriptor);
		}

		return super.equals(obj);
	}
}