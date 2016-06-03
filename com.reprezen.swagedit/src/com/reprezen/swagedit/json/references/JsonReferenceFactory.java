package com.reprezen.swagedit.json.references;

import java.net.URI;

import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

public class JsonReferenceFactory {

	public JsonReference create(JsonNode node) {
		if (node == null) {
			return new JsonReference(null, null, false, false, node);
		}

		String text = node.isTextual() ? node.asText() :
			node.get("$ref").asText();
			
		return doCreate(text, node);
	}

	public JsonReference create(ScalarNode node) {
		return doCreate(node.getValue(), node);
	}

	protected JsonReference doCreate(String value, Object source) {
		URI uri;
		try {
			uri = URI.create(value);
		} catch(NullPointerException | IllegalArgumentException e) {
			// invalid reference
			return new JsonReference(null, null, false, false, source);
		}

		String fragment = uri.getFragment();
		JsonPointer pointer = JsonPointer.compile(Strings.emptyToNull(fragment));

		uri = uri.normalize();
        boolean absolute = uri.isAbsolute();
        boolean local = !absolute && uri.getPath().isEmpty();

        return new JsonReference(uri, pointer, absolute, local, source);
	}

}
