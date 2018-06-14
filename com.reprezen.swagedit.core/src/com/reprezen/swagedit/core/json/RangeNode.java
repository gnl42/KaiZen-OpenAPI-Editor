package com.reprezen.swagedit.core.json;

import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Sets;

public class RangeNode {

    public JsonPointer pointer;

    public static final class Location {
        final public int startLine;
        final public int startColumn;
        final public int endLine;
        final public int endColumn;

        Location(int startLine, int startColumn, int endLine, int endColumn) {
            this.startLine = startLine;
            this.startColumn = startColumn;
            this.endLine = endLine;
            this.endColumn = endColumn;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Location (");
            builder.append(startLine);
            builder.append(", ");
            builder.append(startColumn);
            builder.append("), (");
            builder.append(endLine);
            builder.append(", ");
            builder.append(endColumn);
            builder.append(")");
            return builder.toString();
        }

    }

    private Location fieldLocation = null;
    private Location contentLocation = new Location(1, 1, 1, 1);

    private final Set<RangeNode> children = Sets.newHashSet();

    public RangeNode(JsonPointer pointer) {
        this.pointer = pointer;
    }

    public Location getFieldLocation() {
        return fieldLocation;
    }

    public void setFieldLocation(Location location) {
        this.fieldLocation = location;
    }

    public Location getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(Location location) {
        this.contentLocation = location;
    }

    public Set<RangeNode> getChildren() {
        return children;
    }

    public Position getPosition(IDocument document) {
        boolean selectEntireElement = false;
        int startLine = contentLocation.startLine;
        int offset = 0;
        int length = 0;

        int endLine = contentLocation.endLine;
        int endCol = contentLocation.endColumn;
        try {
            offset = document.getLineOffset(startLine);
            if (selectEntireElement) {
                length = (document.getLineOffset(endLine) + endCol) - offset;
            } else if (startLine < document.getNumberOfLines() - 1) {
                length = document.getLineOffset(startLine + 1) - offset;
            } else {
                length = document.getLineLength(startLine);
            }
        } catch (BadLocationException e) {
            return new Position(0);
        }

        return new Position(Math.max(0, offset), length);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RangeNode [pointer=");
        builder.append(pointer);
        builder.append(", fieldLocation=");
        builder.append(fieldLocation);
        builder.append(", contentLocation=");
        builder.append(contentLocation);
        builder.append(", children=");
        builder.append(children);
        builder.append("]");
        return builder.toString();
    }

}
