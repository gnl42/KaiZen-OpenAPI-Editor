package com.reprezen.swagedit.validation;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.swagger.util.Json;

public class Schema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();
	private JsonSchema schema;

	/**
	 * Returns swagger 2.0 schema
	 * 
	 * @return swagger schema.
	 */
	public JsonSchema getSchema() {
		if (schema == null) {
			JsonNode schemaObject = null;

			try {
				schemaObject = jsonMapper.readTree(Schema.class.getResourceAsStream("schema.json"));
			} catch (IOException e) {
				e.printStackTrace();
			}

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

	public JsonNode getTree() {
		try {
			return jsonMapper.readTree(Schema.class.getResourceAsStream("schema.json"));
		} catch (IOException e) {
			return null;
		}
	}

}
