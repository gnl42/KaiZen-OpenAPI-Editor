package com.reprezen.swagedit.model;

import java.util.Collections;
import java.util.Objects;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Iterables;
import com.reprezen.swagedit.schema.TypeDefinition;

/**
 * Represents a Node inside a YAML/JSON document.
 * 
 * <br/>
 * 
 * Nodes can be either Values, Objects or Arrays. They can contain other elements or simply a value. They contain also
 * information about their location inside a document. A node can be given a type from a JSON schema.
 *
 */
public abstract class AbstractNode {

    private final JsonPointer pointer;
    private final AbstractNode parent;
    private String property;

    private TypeDefinition type;
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
    }

    /**
     * Returns the child node that is contained at the given index.
     * 
     * @param pos
     * @return node
     */
    public AbstractNode get(int index) {
        return null;
    }

    /**
     * Returns the child node that is contained by the given property.
     * 
     * @param property
     * @return node
     */
    public AbstractNode get(String property) {
        return null;
    }

    /**
     * Returns true if the node is an object.
     * 
     * @return true if object
     */
    public abstract boolean isObject();

    /**
     * Returns true if the node is an array.
     * 
     * @return true if array
     */
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

    /**
     * Returns the JSON pointer that identifies this node.
     * 
     * @return JSON pointer
     */
    public JsonPointer getPointer() {
        return pointer;
    }

    public void setType(TypeDefinition type) {
        this.type = type;
    }

    public TypeDefinition getType() {
        return type;
    }

    /**
     * Returns the parent node that contains this node, or null if the node is the root node.
     * 
     * @return parent node
     */
    public AbstractNode getParent() {
        return parent;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String name) {
        this.property = name;
    }

    /**
     * Returns the children elements of this node.
     * 
     * @return node's children
     */
    public Iterable<AbstractNode> elements() {
        return Collections.emptyList();
    }

    /**
     * Returns the number of elements contained by this node.
     * 
     * @return size of children
     */
    public int size() {
        return Iterables.size(elements());
    }

    public abstract String getText();

    /**
     * Returns the position of the node inside the given document. <br/>
     * The position matches the area that contains all the node's content.
     * 
     * @param document
     * @return position inside the document
     */
    public Position getPosition(IDocument document) {
        int startLine = getStart().getLineNr() - 1;
        int offset = 0;
        int length = 0;

        int endLine = getEnd().getLineNr() - 1;
        int endCol = getEnd().getColumnNr() - 1;
        try {
            offset = document.getLineOffset(startLine);
            length = (document.getLineOffset(endLine) + endCol) - offset;
        } catch (BadLocationException e) {
            return new Position(0);
        }

        return new Position(Math.max(0, offset), length);
    }

    public void setStartLocation(JsonLocation start) {
        this.start = start;
    }

    public void setEndLocation(JsonLocation location) {
        this.end = location;
    }

    /**
     * Returns the start location of this node.
     * 
     * @return start location
     */
    public JsonLocation getStart() {
        return start;
    }

    /**
     * Returns the end location of this node.
     * 
     * @return end location
     */
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
