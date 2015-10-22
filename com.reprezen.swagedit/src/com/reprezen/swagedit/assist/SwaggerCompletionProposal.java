package com.reprezen.swagedit.assist;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.validation.SwaggerSchema;

public class SwaggerCompletionProposal {

	private SwaggerProposal proposal;

	public SwaggerProposal get() {
		if (proposal == null) {
			final SwaggerSchema schema = new SwaggerSchema();
			final JsonNode tree = schema.getTree();
			
			proposal = new SwaggerProposal.Builder(tree).build();
		}

		return proposal;
	}

}