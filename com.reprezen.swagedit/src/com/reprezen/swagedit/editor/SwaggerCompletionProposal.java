package com.reprezen.swagedit.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.validation.Schema;

public class SwaggerCompletionProposal {
	
	private final Map<String, List<SwaggerProposal>> proposals = new LinkedHashMap<>();

	public static SwaggerCompletionProposal create() {
		final Schema schema = new Schema();
		final JsonNode tree = schema.getTree();
		final SwaggerCompletionProposal proposal = new SwaggerCompletionProposal();
		final JsonNode properties = tree.get("properties");
		final JsonNode definitions = tree.get("definitions");

		for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
			String key = it.next();
			List<SwaggerProposal> list = new ArrayList<SwaggerProposal>();

			JsonNode jsonNode = definitions.get(key);
			if (jsonNode != null && jsonNode.has("properties")) {
				for (Iterator<String> it2 = jsonNode.get("properties").fieldNames(); it2.hasNext();) {
					list.add(new SwaggerProposal(it2.next()));
				}
			}
			proposal.proposals.put(key, list);
		}

		return proposal;
	}

	public Map<String, List<SwaggerProposal>>  getProposals() {
		return proposals;
	}

}