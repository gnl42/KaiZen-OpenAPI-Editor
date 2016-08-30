package com.reprezen.swagedit.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import com.reprezen.swagedit.json.JsonType;
import com.reprezen.swagedit.json.JsonType2;
import com.reprezen.swagedit.json.SchemaDefinition;
import com.reprezen.swagedit.json.SchemaDefinitionProvider;

public abstract class AbstractNode {

    private static SchemaDefinitionProvider provider = new SchemaDefinitionProvider();

    private final JsonPointer pointer;
    private String property;
    private AbstractNode parent;
    private JsonType type;
    private JsonNode schema;

    public JsonType2 type2;

    public final Set<SchemaDefinition> definitions;

    private JsonLocation location;

    private JsonLocation start;

    private JsonLocation end;

    AbstractNode(AbstractNode parent, JsonPointer ptr) {
        this(parent, ptr, null);
    }

    AbstractNode(AbstractNode parent, JsonPointer ptr, JsonLocation location) {
        this.parent = parent;
        this.pointer = ptr;
        this.location = location;
        this.definitions = provider.getDefinitions(pointer(ptr));
        this.schema = definitions.isEmpty() ? null : Iterables.getFirst(definitions, null).definition;
        this.type = schema != null ? JsonType.valueOf(this.schema) : null;
    }

    public AbstractNode get(int pos) {
        return null;
    }

    public AbstractNode get(String property) {
        return null;
    }

    public abstract boolean isObject();

    public abstract boolean isArray();

    public ObjectNode asObject() {
        return (ObjectNode) this;
    }

    public ArrayNode asArray() {
        return (ArrayNode) this;
    }

    public ValueNode asValue() {
        return (ValueNode) this;
    }

    public JsonLocation getLocation() {
        return location;
    }

    protected static String pointer(JsonPointer pointer) {
        return pointer.toString().replaceAll("/", ":").replaceAll("~1", "/");
    }

    public JsonPointer getPointer() {
        return pointer;
    }

    public JsonType getType() {
        return type;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public Set<SchemaDefinition> getDefinitions() {
        return definitions;
    }

    public AbstractNode getParent() {
        return parent;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String name) {
        this.property = name;
    }

    public Iterable<AbstractNode> elements() {
        return Collections.emptyList();
    }

    public abstract String getText();

    public Position getPosition(IDocument document) {
        JsonLocation location = getStart() != null ? getStart() : getLocation();
        if (location != null) {

            int startLine = location.getLineNr() - 1;
            int offset = 0;
            int length = 0;
            try {
                offset = document.getLineOffset(startLine);
                length = document.getLineOffset(startLine + 1) - offset;
            } catch (BadLocationException e) {
                return new Position(0);
            }

            return new Position(Math.max(0, offset), length);
        }
        return new Position(0);
    }

    public void setStartLocation(JsonLocation start) {
        this.start = start;
    }

    public void setEndLocation(JsonLocation location) {
        this.end = location;
    }

    public JsonLocation getStart() {
        return start;
    }

    public JsonLocation getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((pointer == null) ? 0 : pointer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractNode other = (AbstractNode) obj;
        return Objects.equals(getPointer(), other.getPointer());
    }

}
