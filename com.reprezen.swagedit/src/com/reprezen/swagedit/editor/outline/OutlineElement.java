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
import java.util.Collections;
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
        int startColumn = node.getStartMark().getColumn();
        int endLine = node.getEndMark().getLine();
        int endColumn = node.getEndMark().getColumn();

        int startOffset = 0;
        try {
            startOffset = document.getLineOffset(startLine) + startColumn;
        } catch (BadLocationException e) {
            return position;
        }

        int endOffset;
        try {
            endOffset = document.getLineOffset(endLine) + endColumn;
        } catch (BadLocationException e) {
            endOffset = 0;
        }

        return new Position(Math.max(0, startOffset), Math.max(0, endOffset - startOffset));
    }

    public List<OutlineElement> getChildren() {
        return children;
    }

    public OutlineElement getParent() {
        return parent;
    }

    protected List<OutlineElement> computeChildren() {
        List<OutlineElement> children = new ArrayList<>();

        switch (node.getNodeId()) {
        case mapping:
            MappingNode mn = (MappingNode) node;
            children = new ArrayList<>();
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
            children = new ArrayList<>();
            for (Node n : sq.getValue()) {
                children.add(new OutlineElement(n, this));
            }
            break;
        default:
            children = Collections.emptyList();
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

}