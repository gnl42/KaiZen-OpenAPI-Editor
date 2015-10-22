package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Iterators;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();
	
	private JsonNode tree;
	private JsonSchema schema;
	private String[] keywords;

	/**
	 * Returns swagger 2.0 schema
	 * 
	 * @return swagger schema.
	 */
	public JsonSchema getSchema() {
		if (schema == null) {
			final JsonNode schemaObject = getTree();

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

	public String[] getKeywords() {
		if (keywords == null) {
			final List<String> listOfKeys = new LinkedList<>();
			final JsonNode tree = getTree();

			Iterators.addAll(listOfKeys, tree.get("properties").fieldNames());		
			keywords = listOfKeys.toArray(new String[listOfKeys.size()]);
		}

		return keywords;
	}

	public JsonNode getTree() {
		if (tree == null) {			
			try {
				tree = jsonMapper.readTree(SwaggerSchema.class.getResourceAsStream("schema.json"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return tree;
	}

}
