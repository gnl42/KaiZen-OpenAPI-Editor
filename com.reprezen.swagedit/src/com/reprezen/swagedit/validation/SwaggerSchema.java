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
import com.reprezen.swagedit.assist.JsonUtil;
import com.reprezen.swagedit.assist.SwaggerProposalProvider;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();
	private final SwaggerProposalProvider proposalProvider = new SwaggerProposalProvider();

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
			final JsonNode schemaObject = asJson();

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

	public JsonNode asJson() {
		if (tree == null) {
			try {
				tree = jsonMapper.readTree(SwaggerSchema.class.getResourceAsStream("schema.json"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return tree;
	}

	public List<String> getProperties(String key) {
		return new ArrayList<>();
	}

	public Set<String> getKeywords(boolean isRoot) {
		if (keywords.isEmpty()) {
			rootKeywords.addAll(collectKeywords(asJson()));
			keywords.addAll(rootKeywords);

			final JsonNode definitions = asJson().get("definitions");
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

	public JsonNode getProposals(String path, JsonNode document) {
		if (path.startsWith(":"))
			  path = path.substring(1);

		  String[] paths = path.split(":");
		  JsonNode node = document;
		  JsonNode definition = asJson();

		  for (String current: paths) {
			  if (node.isArray() && current.startsWith("@")) {
				  node = node.get(Integer.valueOf(current.substring(1)));
			  } else {
				  node = node.path(current);
			  }

			  JsonNode next = getDefinition(definition, current); 
			  if (next != null) {
				  definition = next;
			  }
		  }

		  return proposalProvider.get(asJson(), node, definition);
	}

	private JsonNode getDefinition(JsonNode parent, String path) {
		if (parent == null) {
			return null;
		}

		JsonNode definition = null;

		if (path.startsWith("@") && "array".equals( JsonUtil.getType(asJson(), parent) )) {
			return JsonUtil.getRef(asJson(), parent.get("items"));
		}

		if (parent.has("properties")) {
			definition = parent.get("properties").get(path);
		}

		if (definition == null) {
			if (parent.has("patternProperties")) {
				JsonNode properties = parent.get("patternProperties");
				Iterator<String> it = properties.fieldNames();
				while (definition == null && it.hasNext()) {
					String key = it.next();
					if (path.matches(key)) {
						definition = properties.get(key);
					}
				}
			} else if (parent.has("additionalProperties")) {
				JsonNode properties = parent.get("additionalProperties");
				if (properties.isObject()) {
					definition = JsonUtil.getRef(asJson(), properties);
				}
			}
		}

		if (definition != null) {
			definition = JsonUtil.getRef(asJson(), definition);
		}

		return definition;
	}

}
