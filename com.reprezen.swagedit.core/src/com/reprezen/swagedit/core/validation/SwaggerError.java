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
package com.reprezen.swagedit.core.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.reprezen.swagedit.core.Activator;

public class SwaggerError {

    private final String message;
    private final Map<String, Object> markerAttributes = new HashMap<>();

    private int level = IMarker.SEVERITY_WARNING;
    // private final int line;
    // private final int indent;

    private int offset = 0;
    private int length = 0;

    public SwaggerError(int level, int offet, int length, String message, Map<String, Object> markerAttributes) {
        this.level = level;
        this.offset = offet;
        this.length = length;
        this.message = message;
        this.markerAttributes.putAll(markerAttributes != null ? markerAttributes : Collections.emptyMap());
    }

    public SwaggerError(int level, int offset, int length, String message) {
        this(level, offset, length, message, Collections.emptyMap());
    }

    // public SwaggerError(int line, int level, String message) {
    // this(line, level, 0, message);
    // }

    public String getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }

    // public int getLine() {
    // return line;
    // }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public Map<String, Object> getMarkerAttributes() {
        return markerAttributes;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    public IMarker asMarker(IMarker marker) {
        try {
            marker.setAttribute(IMarker.SEVERITY, getLevel());
            marker.setAttribute(IMarker.MESSAGE, getMessage());
            marker.setAttribute(IMarker.CHAR_START, getOffset());
            marker.setAttribute(IMarker.CHAR_END, getOffset() + getLength());
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }

        return marker;
    }

    // String getIndentedMessage() {
    // final StringBuilder builder = new StringBuilder();
    // IntStream.range(0, indent).forEach(i -> builder.append("\t"));
    // builder.append(" - ");
    // builder.append(message);
    // builder.append("\n");
    //
    // return builder.toString();
    // }
    //
    // protected int getIndent() {
    // return indent;
    // }

    @Override
    public int hashCode() {
        return Objects.hash(level, offset, length, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SwaggerError other = (SwaggerError) obj;
        return level == other.level && offset == other.offset && length == other.length
                && Objects.equals(message, other.message);
    }

    // public static class MultipleSwaggerError extends SwaggerError {
    //
    // private final Map<String, Set<SwaggerError>> errors;
    //
    // public MultipleSwaggerError(int line, int level, int indent, String message,
    // Map<String, Set<SwaggerError>> errors) {
    // super(line, level, indent, message);
    // this.errors = errors;
    // }
    //
    // @Override
    // String getIndentedMessage() {
    // return getMessage();
    // }
    //
    // @Override
    // public int hashCode() {
    // final int prime = 31;
    // int result = super.hashCode();
    // result = prime * result + ((errors == null) ? 0 : errors.hashCode());
    // return result;
    // }
    //
    // @Override
    // public boolean equals(Object obj) {
    // if (this == obj)
    // return true;
    // if (!super.equals(obj))
    // return false;
    // if (getClass() != obj.getClass())
    // return false;
    // MultipleSwaggerError other = (MultipleSwaggerError) obj;
    // if (errors == null) {
    // if (other.errors != null)
    // return false;
    // } else if (!errors.equals(other.errors))
    // return false;
    // return true;
    // }
    //
    // }

    public void setLevel(int level) {
        this.level = level;
    }

}
