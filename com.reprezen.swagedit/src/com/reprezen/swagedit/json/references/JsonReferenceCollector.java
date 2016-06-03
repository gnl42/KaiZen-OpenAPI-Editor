package com.reprezen.swagedit.json.references;

import java.util.Set;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class JsonReferenceCollector {

	private final JsonReferenceFactory factory;

	public JsonReferenceCollector(JsonReferenceFactory factory) {
		this.factory = factory;
	}

	public Iterable<JsonReference> collect(JsonNode document) {
		return Lists.newArrayList();
	}

	public Iterable<JsonReference> collect(Node document) {
		Set<JsonReference> acc = Sets.newHashSet();
		collectReferences(document, acc);
		return acc;
	}

	protected void collectReferences(Node parent, Set<JsonReference> acc) {
		switch (parent.getNodeId()) {
		case mapping:
			for (NodeTuple tuple: ((MappingNode) parent).getValue()) {
				Node keyNode = tuple.getKeyNode();
				if (keyNode.getNodeId() == NodeId.scalar) {

					if ("$ref".equals(((ScalarNode) keyNode).getValue())) {
						acc.add(factory.create((ScalarNode)tuple.getValueNode()));
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
