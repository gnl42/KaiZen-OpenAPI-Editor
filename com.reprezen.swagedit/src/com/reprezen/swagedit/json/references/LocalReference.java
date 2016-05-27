package com.reprezen.swagedit.json.references;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class LocalReference extends JsonReference {

	private final JsonNode document;

	LocalReference(JsonNode document, JsonPointer pointer) {
		super(pointer);
		this.document = document;
	}

	@Override
	public boolean isValid() {
		return !(get() instanceof MissingNode);
	}

	@Override
	public JsonNode get() {
		return document.at(pointer);
	}
}