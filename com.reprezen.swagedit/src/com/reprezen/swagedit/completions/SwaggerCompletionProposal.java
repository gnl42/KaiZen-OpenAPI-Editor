package com.reprezen.swagedit.completions;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.validation.Schema;

public class SwaggerCompletionProposal {

	private SwaggerProposal proposal;

	public SwaggerProposal get() {
		if (proposal == null) {
			final Schema schema = new Schema();
			final JsonNode tree = schema.getTree();
			
			proposal = new SwaggerProposal.Builder(tree).build();
		}

		return proposal;
	}

}