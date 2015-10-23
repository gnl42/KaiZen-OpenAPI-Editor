package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.assist.SwaggerProposal.ObjectProposal;
import com.reprezen.swagedit.validation.SwaggerSchema;

public class SwaggerCompletionProposal {

	private ObjectProposal proposal;

	public ObjectProposal get() {
		if (proposal == null) {
			final SwaggerSchema schema = new SwaggerSchema();
			final JsonNode tree = schema.getTree();
			
			proposal = (ObjectProposal) new SwaggerProposal.Builder(tree).build();
		}

		return proposal;
	}

}