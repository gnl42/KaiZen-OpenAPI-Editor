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
package com.reprezen.swagedit.json.references;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

/**
 * Collector of JSON references present in a JSON or YAML document.
 * 
 * This class can be used to obtain all JSONReference present inside a JSON 
 * or YAML document.
 * 
 */
public class JsonReferenceCollector {

	private final JsonReferenceFactory factory;

	public JsonReferenceCollector(JsonReferenceFactory factory) {
		this.factory = factory;
	}

	/**
	 * Returns all reference nodes that can be found in the JSON document.
	 * 
	 * @param document
	 * @return all reference nodes
	 */
	public Iterable<JsonReference> collect(JsonNode document) {
		Set<JsonReference> acc = Sets.newHashSet();
		collectReferences(document, acc);
		return acc;
	}

	protected void collectReferences(final JsonNode parent, Set<JsonReference> acc) {
		if (parent.isObject()) {
			for (Iterator<Map.Entry<String, JsonNode>> it = parent.fields(); it.hasNext(); ) {
				final Map.Entry<String, JsonNode> entry = it.next();
				final JsonNode value = entry.getValue();

				if (JsonReference.isReference(value)) {
					acc.add(factory.create(value));
				} else {
					collectReferences(entry.getValue(), acc);
				}
			}
		} else if (parent.isArray()) {
			for (Iterator<JsonNode> it = parent.elements(); it.hasNext(); ) {
				collectReferences(it.next(), acc);
			}
		}
	}

	/**
	 * Returns all reference nodes that can be found in the Yaml document.
	 * 
	 * @param document
	 * @return all reference nodes
	 */
	public Iterable<JsonReference> collect(Node document) {
		Set<JsonReference> acc = Sets.newHashSet();
		collectReferences(document, acc);
		return acc;
	}

	protected void collectReferences(Node parent, Set<JsonReference> acc) {
		switch (parent.getNodeId()) {
		case mapping:
			for (NodeTuple tuple: ((MappingNode) parent).getValue()) {
				if (JsonReference.isReference(tuple)) {
					acc.add(factory.create((ScalarNode) tuple.getValueNode()));
				} else {
					collectReferences(tuple.getValueNode(), acc);
				}
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
