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
import java.util.Map;
import java.util.Set;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.parser.ParserException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.JsonSchemaManager;

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
			errors.add(new SwaggerError(
					IMarker.SEVERITY_ERROR, 
					"Unable to read content. It may be invalid YAML"));
		} else {
			final Node yaml = document.getYaml();
			final ErrorProcessor processor = new ErrorProcessor(yaml);

			errors.addAll(validateAgainstSchema(processor, jsonContent));
			errors.addAll(checkDuplicateKeys(yaml));
		}

		return errors;
	}

	protected Set<SwaggerError> validateAgainstSchema(ErrorProcessor processor, JsonNode jsonContent) {
		final Set<SwaggerError> errors = Sets.newHashSet();

		try {
			ProcessingReport report = 
					schemaManager.getSwaggerSchema()
					.getSchema()
					.validate(jsonContent, true);

			errors.addAll(processor.processReport(report));
		} catch (ProcessingException e) {
			errors.addAll(processor.processMessage(e.getProcessingMessage()));
		}

		return errors;
	}

	protected Set<SwaggerError> checkDuplicateKeys(Node document) {
		Set<SwaggerError> errors = Sets.newHashSet();
		
		if (document.getNodeId() == NodeId.mapping) {
			MappingNode n = (MappingNode) document;

			Map<String, Set<Node>> duplicates = Maps.newHashMap();
			for (NodeTuple tuple: n.getValue()) {
				Node keyNode = tuple.getKeyNode();

				if (keyNode.getNodeId() == NodeId.scalar) {
					String key = ((ScalarNode) keyNode).getValue();
					if (duplicates.containsKey(key)) {
						duplicates.get(key).add(keyNode);
					} else {
						duplicates.put(key, Sets.newHashSet(keyNode));
					}
				}

				errors.addAll(checkDuplicateKeys(tuple.getValueNode()));
			}

			for (String key: duplicates.keySet()) {
				Set<Node> values = duplicates.get(key);
				if (values.size() > 1) {
					for (Node duplicate: values) {
						errors.add(new SwaggerError(
								duplicate.getStartMark().getLine() + 1, 
								IMarker.SEVERITY_WARNING, 
								String.format("Object has a duplicate key %s", key)));
					}
				}
			}
		}

		return errors;
	}

}
