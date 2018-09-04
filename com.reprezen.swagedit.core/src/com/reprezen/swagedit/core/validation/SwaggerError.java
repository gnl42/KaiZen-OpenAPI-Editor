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

import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SwaggerError {
    
    private static YamlErrorProcessor processor = new YamlErrorProcessor();

    public static SwaggerError newYamlError(YAMLException exception) {
        int line = (exception instanceof MarkedYAMLException)
                ? ((MarkedYAMLException) exception).getProblemMark().getLine() + 1 : 1;
        return new SwaggerError(line, IMarker.SEVERITY_ERROR, 0, processor.rewriteMessage(exception));
    }

    public static SwaggerError newJsonError(JsonProcessingException exception) {
        int line = (exception.getLocation() != null) ? exception.getLocation().getLineNr() : 1;
        return new SwaggerError(line, IMarker.SEVERITY_ERROR, 0, exception.getMessage());
    }

    private final String message;

    private final int level;
    private final int line;
    private final int indent;

    public SwaggerError(int line, int level, int indent, String message) {
        this.line = line;
        this.level = level;
        this.indent = indent;
        this.message = message;
    }

    public SwaggerError(int line, int level, String message) {
        this(line, level, 0, message);
    }

    public String getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }

    public int getLine() {
        return line;
    }
    
    @Override
    public java.lang.String toString() {
        return getMessage();
    }

    String getIndentedMessage() {
        final StringBuilder builder = new StringBuilder();
        IntStream.range(0, indent).forEach(i->builder.append("\t"));
        builder.append(" - ");
        builder.append(message);
        builder.append("\n");

        return builder.toString();
    }
    
    protected int getIndent() {
        return indent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + line;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
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
        SwaggerError other = (SwaggerError) obj;
        if (level != other.level)
            return false;
        if (line != other.line)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

    public static class MultipleSwaggerError extends SwaggerError {

        private final Map<String, Set<SwaggerError>> errors;

        public MultipleSwaggerError(int line, int level, int indent, String message, Map<String, Set<SwaggerError>> errors) {
            super(line, level, indent, message);
            this.errors = errors;
        }

        @Override
        String getIndentedMessage() {
            return getMessage();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((errors == null) ? 0 : errors.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultipleSwaggerError other = (MultipleSwaggerError) obj;
            if (errors == null) {
                if (other.errors != null)
                    return false;
            } else if (!errors.equals(other.errors))
                return false;
            return true;
        }

    }

}
