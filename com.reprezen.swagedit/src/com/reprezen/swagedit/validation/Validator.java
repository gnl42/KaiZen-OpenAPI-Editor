/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.Messages;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonSchemaManager;
import com.reprezen.swagedit.json.JsonUtil;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

	private static final JsonSchemaManager schemaManager = new JsonSchemaManager();

	/**
	 * Returns a list or errors if validation fails.
	 * 
	 * This method accepts as input a swagger YAML document and validates it
	 * against the swagger JSON Schema.
	 * 
	 * @param content
	 * @return list or errors
	 * @throws IOException
	 * @throws ParserException
	 */
	public Set<SwaggerError> validate(SwaggerDocument document) {
		Set<SwaggerError> errors = Sets.newHashSet();

		JsonNode jsonContent = null;
		try {
			jsonContent = document.asJson();
		} catch (Exception e) {
			YEditLog.logException(e);
		}

		if (jsonContent == null) {
			errors.add(new SwaggerError(IMarker.SEVERITY_ERROR, Messages.error_cannot_read_content));
		} else {
			Node yaml = document.getYaml();
			if (yaml != null) {
				errors.addAll(validateAgainstSchema(new ErrorProcessor(yaml), jsonContent));
				errors.addAll(checkDuplicateKeys(yaml));
				errors.addAll(validateReferences(yaml, document));
			}
		}

		return errors;
	}

	/*
	 * Validates the YAML document against the Swagger schema
	 */
	protected Set<SwaggerError> validateAgainstSchema(ErrorProcessor processor, JsonNode jsonContent) {
		final Set<SwaggerError> errors = Sets.newHashSet();

		try {
			ProcessingReport report = schemaManager.getSwaggerSchema().getSchema().validate(jsonContent, true);

			errors.addAll(processor.processReport(report));
		} catch (ProcessingException e) {
			errors.addAll(processor.processMessage(e.getProcessingMessage()));
		}

		return errors;
	}

	/*
	 * Finds all duplicate keys in all objects present in the YAML document.
	 */
	protected Set<SwaggerError> checkDuplicateKeys(Node document) {
		HashMultimap<Pair<Node, String>, Node> acc = 
				HashMultimap.<Pair<Node, String>, Node>create();

		collectDuplicates(document, acc);

		Set<SwaggerError> errors = Sets.newHashSet();
		for (Pair<Node, String> key: acc.keys()) {
			Set<Node> duplicates = acc.get(key);

			if (duplicates.size() > 1) {
				for (Node duplicate : duplicates) {
					errors.add(createDuplicateError(key.getValue(), duplicate));
				}
			}
		}

		return errors;
	}

	/*
	 * This method iterates through the YAML tree to collect the pairs of Node x String 
	 * representing an object and one of it's keys. Each pair is associated to a Set of 
	 * Nodes which contains all nodes being a key to the pair's Node and having for value 
	 * the pair's key.
	 * Once the iteration is done, the resulting map should be traversed. Each pair having 
	 * more than one element in its associated Set are duplicate keys. 
	 */
	protected void collectDuplicates(Node parent, Multimap<Pair<Node, String>, Node> acc) {
		switch (parent.getNodeId()) {
		case mapping: {
			for (NodeTuple value: ((MappingNode) parent).getValue()) {
				Node keyNode = value.getKeyNode();

				if (keyNode.getNodeId() == NodeId.scalar) {
					acc.put(Pair.of(parent, ((ScalarNode) keyNode).getValue()), keyNode);
				}

				collectDuplicates(value.getValueNode(), acc);
			}
		}
			break;
		case sequence: {
			for (Node value: ((SequenceNode) parent).getValue()) {
				collectDuplicates(value, acc);
			}
		}
			break;
		default:
			break;
		}
	}

	protected SwaggerError createDuplicateError(String key, Node node) {
		return new SwaggerError(
				node.getStartMark().getLine() + 1, 
				IMarker.SEVERITY_WARNING,
				String.format(Messages.error_duplicate_keys, key));
	}

	protected Set<SwaggerError> validateReferences(Node document, SwaggerDocument swagDoc) {
		Set<SwaggerError> errors = Sets.newHashSet();
		Set<NodeTuple> references = Sets.newHashSet();
		collectReferences(document, references);

		for (NodeTuple tuple: references) {
			ScalarNode value = (ScalarNode) tuple.getValueNode();
			String text = value.getValue();
			JsonNode pointed = JsonUtil.at(swagDoc, text);

			if (pointed == null || pointed instanceof MissingNode) {
				errors.add(createReferenceError(value));
			}
		}

		return errors;
	}

	private SwaggerError createReferenceError(Node node) {
		return new SwaggerError(
				node.getStartMark().getLine() + 1,
				IMarker.SEVERITY_WARNING,
				Messages.error_invalid_reference);
	}

	protected void collectReferences(Node parent, Set<NodeTuple> acc) {
		switch (parent.getNodeId()) {
		case mapping:
			for (NodeTuple tuple: ((MappingNode) parent).getValue()) {
				Node keyNode = tuple.getKeyNode();
				if (keyNode.getNodeId() == NodeId.scalar) {

					if ("$ref".equals(((ScalarNode) keyNode).getValue())) {
						acc.add(tuple);
					}
				}
				collectReferences(tuple.getValueNode(), acc);
			}
			break;
		case sequence:
			for (Node value: ((SequenceNode) parent).getValue()) {
				collectReferences(value, acc);
			}
			break;
		default:
			break;
		}
	}
}
