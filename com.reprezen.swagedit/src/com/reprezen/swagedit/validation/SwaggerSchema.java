package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();
	
	private JsonNode tree;
	private JsonSchema schema;
	private Set<String> keywords;

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

	public List<String> getProperties(String key) {
		return new ArrayList<>();
	}

	public Set<String> getKeywords() {
		if (keywords == null) {
			keywords = new LinkedHashSet<>();
			collectKeywords(getTree());

			final JsonNode definitions = getTree().get("definitions");
			for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
				final Entry<String, JsonNode> entry = it.next();
				collectKeywords(entry.getValue());
			}
		}

		return keywords;
	}

	private void collectKeywords(JsonNode node) {
		if (node != null && node.has("properties") && node.get("properties").isObject()) {
			for (Iterator<String> it = node.get("properties").fieldNames(); it.hasNext();) {
				keywords.add(it.next());
			}
		}
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
