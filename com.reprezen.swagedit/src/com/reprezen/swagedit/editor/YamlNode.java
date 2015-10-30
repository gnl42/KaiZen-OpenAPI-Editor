package com.reprezen.swagedit.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.validation.SwaggerSchema;

public class YamlNode {
	public YamlNode parent;
	public NodeId type;
	public Node delegate;
	public YamlNode cursor;
	public List<YamlNode> children = new LinkedList<>();
	public JsonNode definition;

	YamlNode(YamlNode parent) {
		this.parent = parent;
	}

	public ICompletionProposal[] getProposals() {
		return null;
	}

	public String getPath() {
		if (parent == null) {
			return "/" + getId();
		} else {
			return parent.getPath() + "/" + getId();
		}
	}

	public String getId() {
		switch (delegate.getNodeId()) {
		case scalar:
			return ((ScalarNode) delegate).getValue();
		case mapping:
			return "";
		default:
			return "";
		}
	}

	public static YamlNode create(int line, NodeTuple tuple, SwaggerSchema schema) {
//		System.out.println("create for " + tuple);
		final Node key = tuple.getKeyNode();
		key.getStartMark().getIndex();

		YamlNode current = new YamlNode(null);
		current.type = key.getNodeId();
		current.delegate = key;

		System.out.println(current.getPath());

		if (key.getStartMark().getLine() != line) {			
			Node value = tuple.getValueNode();
			
			if (value instanceof MappingNode) {
				{
					MappingNode map = (MappingNode) value;
					List<NodeTuple> values = map.getValue();
					for (NodeTuple t: values) {
						YamlNode node = create(line, t, schema);
						node.parent = current;
						current.children.add(node);
						System.out.println(node.getPath());
					}
				}
				System.out.println(current);
			} else {
				System.out.println("not " + value);
				current = new YamlNode(current);
				current.type = value.getNodeId();
				current.delegate = value;
				System.out.println(current.type);
				System.out.println(current.getPath());
			}
		}

		return current;
	}

	
	public static class YamlTupleNode {
		public YamlNode key;
		public YamlNode value;
	}

}
