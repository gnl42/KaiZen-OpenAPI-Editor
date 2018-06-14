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
package com.reprezen.swagedit.core.editor;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.RangeNode;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * SwaggerDocument
 * 
 */
public class JsonDocument extends Document {

    private CompositeSchema schema;
    private JsonModel model;

    public JsonDocument(CompositeSchema schema) {
        this.schema = schema;
    }

    public JsonModel getContent() {
        return model;
    }

    public List<Exception> getErrors() {
        return model != null ? model.getErrors() : Lists.<Exception> newArrayList();
    }

    /**
     * Returns the JSON representation of the document.
     * 
     * Will throw an exception if the content of the document is not valid YAML.
     * 
     * @return JsonNode
     * @throws ParserException
     * @throws IOException
     */
    public JsonNode asJson() {
        return model != null ? model.getContent() : null;
    }

    public CompositeSchema getSchema() {
        return schema;
    }

    /**
     * Returns position of the symbol ':' in respect to the given offset.
     * 
     * Will return -1 if reaches beginning of line of other symbol before finding ':'.
     * 
     * @param offset
     * @return position
     */
    public int getDelimiterPosition(int offset) {
        while (true) {
            try {
                char c = getChar(--offset);
                if (Character.isLetterOrDigit(c)) {
                    return -1;
                }
                if (c == ':') {
                    return offset;
                }
                if (Character.isWhitespace(c)) {
                    continue;
                }
                if (c != ':' && !Character.isLetterOrDigit(c)) {
                    return -1;
                }
            } catch (BadLocationException e) {
                return -1;
            }
        }
    }

    public void onChange() {
        try {
            model = new JsonModel(schema, get(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            model = null;
        }
    }

    public JsonPointer getPath(int line, int column) {
        System.out.println("GET PATH " + get());
        JsonModel model;
        try {
            model = new JsonModel(schema, get(), false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        RangeNode range = model.findRegion(line, column);

        return getPointer(range);
    }

    private JsonPointer getPointer(RangeNode range) {
        return JsonPointer.compile(range.pointer.toString());
    }

    public JsonPointer getPath(IRegion region) {
        int lineOfOffset;
        try {
            lineOfOffset = getLineOfOffset(region.getOffset());
        } catch (BadLocationException e) {
            return null;
        }

        return getPath(lineOfOffset + 1, getColumnOfOffset(lineOfOffset, region) + 1);
    }

    public int getColumnOfOffset(int line, IRegion region) {
        int lineOffset;
        try {
            lineOffset = getLineOffset(line);
        } catch (BadLocationException e) {
            lineOffset = 0;
        }

        return (region.getOffset() + region.getLength()) - lineOffset;
    }

    public IRegion getRegion(JsonPointer pointer) {
        String[] paths = pointer.toString().split("/");
        RangeNode range = model.getRanges().get(com.github.fge.jackson.jsonpointer.JsonPointer.of(paths));

        if (range == null) {
            return new Region(0, 0);
        }

        Position position = range.getPosition(this);

        return new Region(position.getOffset(), position.getLength());
    }

}
