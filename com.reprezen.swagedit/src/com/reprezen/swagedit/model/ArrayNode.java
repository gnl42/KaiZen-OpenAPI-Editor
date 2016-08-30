package com.reprezen.swagedit.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;

public class ArrayNode extends AbstractNode {

    private final List<AbstractNode> elements = new ArrayList<>();

    public ArrayNode(AbstractNode parent, JsonPointer ptr) {
        super(parent, ptr);
    }

    public ArrayNode(AbstractNode parent, JsonPointer ptr, JsonLocation location) {
        super(parent, ptr, location);
    }

    @Override
    public AbstractNode get(int pos) {
        return elements.get(pos);
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public void add(AbstractNode model) {
        this.elements.add(model);
    }

    @Override
    public Iterable<AbstractNode> elements() {
        return elements;
    }

    @Override
    public String getText() {
        return getProperty() == null ? "" : getProperty();
    }

    @Override
    public String toString() {
        return "[ " + elements + " ]";
    }
}