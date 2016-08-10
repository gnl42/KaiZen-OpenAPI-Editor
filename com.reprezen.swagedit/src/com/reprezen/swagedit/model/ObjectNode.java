package com.reprezen.swagedit.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Iterables;

public class ObjectNode extends AbstractNode {

    public final Map<String, AbstractNode> elements = new LinkedHashMap<>();

    public ObjectNode(AbstractNode parent, JsonPointer ptr) {
        super(parent, ptr);
    }

    public ObjectNode(AbstractNode parent, JsonPointer ptr, JsonLocation location) {
        super(parent, ptr, location);
    }

    @Override
    public AbstractNode get(int pos) {
        return Iterables.get(elements.values(), pos);
    }

    @Override
    public AbstractNode get(String property) {
        return elements.get(property);
    }

    public AbstractNode put(String property, AbstractNode value) {
        this.elements.put(property, value);
        return this;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public Iterable<AbstractNode> elements() {
        return elements.values();
    }

    @Override
    public String getText() {
        return getProperty() == null ? "" : getProperty();
    }
}