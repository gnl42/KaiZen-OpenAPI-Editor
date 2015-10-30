package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.reprezen.swagedit.assist.SwaggerProposal;
import com.reprezen.swagedit.assist.SwaggerProposal.ObjectProposal;

import io.swagger.util.Json;

public class SwaggerSchema {

	private final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	private final ObjectMapper jsonMapper = Json.mapper();

	private JsonNode tree;
	private JsonSchema schema;
	private final Set<String> keywords = new LinkedHashSet<>();
	private final Set<String> rootKeywords = new LinkedHashSet<>();
	private ObjectProposal proposal;

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

	public ObjectProposal get() {
		if (proposal == null) {
			proposal = (ObjectProposal) new SwaggerProposal.Builder(asJson()).build();
		}

		return proposal;
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

	public List<ICompletionProposal> getContentProposals(boolean startOfLine, String prefix, String indent, int position, int documentOffset) {
		final List<ICompletionProposal> proposals = new ArrayList<>();

		if (position > -1) {
			// get proposals for that keyword
			if (!prefix.isEmpty()) {
				SwaggerProposal found = proposal.getProperties().get(prefix);
				if (found != null) {
					proposals.addAll(found.asCompletionProposal(documentOffset));
				}
			}

		} else {
			if (!prefix.isEmpty()) {
				// look for keywords that match the input
				for (String keyword : getKeywords(false)) {
					if (keyword.startsWith(prefix)) {
						final String replacement = keyword.substring(prefix.length(), keyword.length());
						proposals.add(new CompletionProposal(replacement, documentOffset, 0, replacement.length(), null,
								keyword, null, null));
					}
				}
			}
		}

		// if nothing has been found, add list of keywords
		if (proposals.isEmpty()) {
			for (String current: getKeywords(startOfLine)) {
//				if (!(viewer.getDocument().get().contains(current))) {
					proposals.add(new CompletionProposal(current, documentOffset, 0, current.length()));
//				}
			}
		}

		return proposals;
	}

	public void getProposals(String path, JsonNode document) {
	  if (path.startsWith("/"))
		  path = path.substring(1);
	  
	  String[] paths = path.split("/");
	  JsonNode current = document.path(paths[0]);
	  JsonNode definition = getDefinition(paths[0]);
	  
	  System.out.println(current);
	  System.out.println(definition);
	}

	private JsonNode getDefinition(String path) {
		JsonNode schema = asJson();
		JsonNode definition = schema.get("properties").get(path);
		
		if (definition.has("$ref")) {
			String ref = definition.get("$ref").asText();
			ref = ref.substring("#/definitions/".length());
			definition = schema.get("definitions").get(ref);
		}

		return definition;
	}

}
