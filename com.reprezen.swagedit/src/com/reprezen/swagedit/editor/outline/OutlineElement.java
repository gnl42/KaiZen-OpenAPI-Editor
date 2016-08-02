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
package com.reprezen.swagedit.editor.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class OutlineElement {

    private final Node node;
    private final OutlineElement parent;
    private final String text;
    private final String key;

    private final List<OutlineElement> children;

    public OutlineElement(Node node) {
        this(node, null);
    }

    public OutlineElement(Node node, OutlineElement parent) {
        this(node, parent, null);
    }

    public OutlineElement(Node node, OutlineElement parent, String key) {
        this.node = node;
        this.parent = parent;
        this.key = key;
        this.children = computeChildren();
        this.text = elementText();
    }

    public Node getNode() {
        return node;
    }

    private String elementText() {
        OutlineElement parent = getParent();
        if (parent == null) {
            return "";
        }

        switch (parent.node.getNodeId()) {
        case sequence:
            if (getChildren().isEmpty()) {
                if (node instanceof ScalarNode) {
                    return ((ScalarNode) node).getValue();
                } else {
                    return node.toString();
                }
            } else {
                return "";
            }
        case mapping:
            if (getChildren().isEmpty()) {
                if (node instanceof ScalarNode) {
                    return key + ": " + ((ScalarNode) node).getValue();
                } else {
                    return key + ": " + node.toString();
                }
            } else {
                return key;
            }
        default:
            return node.toString();
        }
    }

    public String getText() {
        return text;
    }

    public Position getPosition(IDocument document) {
        Position position = new Position(0, 0);
        if (document == null) {
            return position;
        }

        int startLine = node.getStartMark().getLine();

        int offset = 0;
        try {
            offset = document.getLineOffset(startLine);
            offset += document.getLineLength(startLine) - 1;
        } catch (BadLocationException e) {
            return position;
        }

        return new Position(Math.max(0, offset), 0);
    }

    public List<OutlineElement> getChildren() {
        return children;
    }

    public OutlineElement getParent() {
        return parent;
    }

    protected List<OutlineElement> computeChildren() {
        final List<OutlineElement> children = new ArrayList<>();

        switch (node.getNodeId()) {
        case mapping:
            MappingNode mn = (MappingNode) node;
            for (NodeTuple tn : mn.getValue()) {
                String key = null;
                if (tn.getKeyNode() instanceof ScalarNode) {
                    key = ((ScalarNode) tn.getKeyNode()).getValue();
                }

                children.add(new OutlineElement(tn.getValueNode(), this, key));
            }
            break;
        case sequence:
            SequenceNode sq = (SequenceNode) node;
            for (Node n : sq.getValue()) {
                children.add(new OutlineElement(n, this));
            }
            break;
        default:
            break;
        }

        return children;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OutlineElement [node=");
        builder.append(node);
        builder.append("]");
        return builder.toString();
    }

    public static List<OutlineElement> create(Node yaml) {
        List<OutlineElement> nodes = new ArrayList<>();

        if (yaml instanceof MappingNode) {
            OutlineElement parent = new OutlineElement(yaml);

            String key;
            for (NodeTuple tuple : ((MappingNode) yaml).getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode) {
                    key = ((ScalarNode) tuple.getKeyNode()).getValue();
                    nodes.add(new OutlineElement(tuple.getValueNode(), parent, key));
                }
            }
        }
        return nodes;
    }

}