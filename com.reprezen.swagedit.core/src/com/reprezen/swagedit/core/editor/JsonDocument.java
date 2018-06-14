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
import java.net.URL;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.jsonoverlay.IJsonOverlay;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.val.ValidationResults;
import com.reprezen.swagedit.core.json.JsonRegion;
import com.reprezen.swagedit.core.json.JsonRegionLocator;
import com.reprezen.swagedit.core.json.OpenApi3LocationParser;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * SwaggerDocument
 * 
 */
public class JsonDocument extends Document {

    private CompositeSchema schema;

    private OpenApi3LocationParser parser = new OpenApi3LocationParser();
    private OpenApi3 model;

    private JsonRegionLocator locator;

    private URL documentURL;

    public JsonDocument(CompositeSchema schema) {
        this.schema = schema;
    }

    public void setDocumentURL(URL documentURL) {
        this.documentURL = documentURL;
    }

    public OpenApi3 getContent() {
        return model;
    }

    public ValidationResults validate(URL documentURL) {
        System.out.println("VALIDATE " + this.documentURL);
        try {
            return parser.parse(get(), this.documentURL, true).getValidationResults();
        } catch (Exception e) {
            ValidationResults results = new ValidationResults();
            results.addError(e.getMessage());
            return results;
        }
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
        return parser.getJSON(documentURL);
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
            model = parser.parse(get(), documentURL, false);
            locator = new JsonRegionLocator(parser.geRegions(), parser.getPaths());
        } catch (Exception e) {
            model = null;
            locator = null;
        }
    }

    public JsonPointer getPath(int line, int column) {
        JsonRegion region = null;
        if (locator != null) {
            region = locator.findRegion(line, column);
        }

        return getPointer(region);
    }

    private JsonPointer getPointer(JsonRegion region) {
        return JsonPointer.compile(region != null ? region.pointer.toString() : "");
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
        JsonRegion region = locator.get(pointer);
        if (region == null) {
            return new Region(0, 0);
        }

        Position position = region.getPosition(this);

        return new Region(position.getOffset(), position.getLength());
    }

    public JsonNode findNode(JsonPointer pointer) {
        JsonNode tree = asJson();
        if (tree != null) {
            return tree.at(pointer);
        }
        return null;
    }

    public JsonRegion findRegion(JsonPointer ptr) {
        if (locator != null) {
            return locator.get(ptr);
        }
        return null;
    }

    public IJsonOverlay<?> getModel() {
        return model;
    }

}
