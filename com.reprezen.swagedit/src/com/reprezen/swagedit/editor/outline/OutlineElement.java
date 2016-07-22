package com.reprezen.swagedit.editor.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

public class OutlineElement {

    private final Node node;
    private final IDocument document;
    private final OutlineElement parent;
    private final String text;
    private final String key;

    private List<OutlineElement> children;


    public OutlineElement(Node node, IDocument document) {
        this(node, document, null, null);
    }

    public OutlineElement(Node node, IDocument document, OutlineElement parent) {
        this(node, document, parent, null);
    }

    public OutlineElement(Node node, IDocument document, OutlineElement parent, String key) {
        this.node = node;
        this.document = document;
        this.parent = parent;
        this.key = key;
        this.text = elementText();
    }

    private String elementText() {
        return (key != null ? key : "") + node.getTag().toString();
    }

    public String getText() {
        return text;
    }

    public List<OutlineElement> getChildren() {
        if (children == null) {
            switch (node.getNodeId()) {
            case mapping:
                MappingNode mn = (MappingNode) node;
                children = new ArrayList<>();
                for (NodeTuple tn : mn.getValue()) {
                    String key = null;
                    if (tn.getKeyNode() instanceof ScalarNode) {
                        key = ((ScalarNode) tn.getKeyNode()).getValue();
                    }

                    children.add(new OutlineElement(tn.getValueNode(), document, this, key));
                }
                break;
            case sequence:
                SequenceNode sq = (SequenceNode) node;
                children = new ArrayList<>();
                for (Node n : sq.getValue()) {
                    children.add(new OutlineElement(n, document, this));
                }
                break;
            default:
                children = Collections.emptyList();
                break;
            }
        }
        return children;
    }

    public OutlineElement getParent() {
        return parent;
    }

}