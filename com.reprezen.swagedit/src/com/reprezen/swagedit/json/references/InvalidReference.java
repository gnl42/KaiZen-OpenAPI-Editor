package com.reprezen.swagedit.json.references;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

public class InvalidReference extends JsonReference {

	InvalidReference() {
		super(JsonPointer.compile(null));
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public JsonNode get() {
		return null;
	}
	
}