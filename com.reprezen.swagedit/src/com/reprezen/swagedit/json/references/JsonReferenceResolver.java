package com.reprezen.swagedit.json.references;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonReferenceResolver {

	private final JsonReferenceFactory factory;
	private final JsonReferenceCollector collector;

	public JsonReferenceResolver() {
		this.factory = new JsonReferenceFactory();
		this.collector = new JsonReferenceCollector(factory);
	}

	/**
	 * Resolves all references inside the JSON document.
	 * 
	 * @param document
	 * @return document without reference nodes
	 */
	public JsonNode resolve(URI baseURI, JsonNode document) {
		Iterable<JsonReference> references = collector.collect(document);

		for (JsonReference reference: references) {
			JsonNode resolved = reference.resolve(baseURI);
			if (resolved != null && !resolved.isMissingNode()) {
				// TODO
				// replace node in document
			}
		}

		return document;
	}

}
