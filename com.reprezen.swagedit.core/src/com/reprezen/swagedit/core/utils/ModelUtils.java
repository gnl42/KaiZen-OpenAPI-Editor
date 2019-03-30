package com.reprezen.swagedit.core.utils;

import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.ObjectNode;

public class ModelUtils {

	public static AbstractNode getParentNode(AbstractNode node, String name) {
		if (node instanceof ObjectNode) {
			final ObjectNode objectNode = (ObjectNode) node;
			if (objectNode.get(name) != null) {
				return objectNode;
			}
		}
		
		//TODO: Can node.getParent() be null????? Please check.
		
		return getParentNode(node.getParent(), name);
	}
}
