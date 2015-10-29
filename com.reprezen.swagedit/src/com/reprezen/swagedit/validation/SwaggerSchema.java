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
	private final Set<String> keywords = new LinkedHashSet<>();
	private final Set<String> rootKeywords = new LinkedHashSet<>();

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

	public Set<String> getKeywords(boolean isRoot) {
		if (keywords.isEmpty()) {
			rootKeywords.addAll(collectKeywords(getTree()));
			keywords.addAll(rootKeywords);

			final JsonNode definitions = getTree().get("definitions");
			for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
				keywords.addAll(collectKeywords(it.next().getValue()));
			}
		}

		return isRoot ? rootKeywords : keywords;
	}

	private Set<String> collectKeywords(JsonNode node) {
		Set<String> result = new LinkedHashSet<>();
		if (node != null && node.has("properties") && node.get("properties").isObject()) {
			for (Iterator<String> it = node.get("properties").fieldNames(); it.hasNext();) {
				result.add(it.next());
			}
		}
		
		return result;
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
