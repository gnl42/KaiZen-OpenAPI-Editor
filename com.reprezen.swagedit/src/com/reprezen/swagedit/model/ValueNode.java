package com.reprezen.swagedit.model;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;

public class ValueNode extends AbstractNode {

    private final Object value;

    public ValueNode(AbstractNode parent, JsonPointer ptr, Object value) {
        super(parent, ptr);

        this.value = value;
    }

    public ValueNode(AbstractNode parent, JsonPointer ptr, Object value, JsonLocation location) {
        super(parent, ptr, location);

        this.value = value;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String getText() {
        String text = getProperty() != null ? getProperty() + ": " : "";
        return text + (value != null ? getValue().toString() : "");
    }
}