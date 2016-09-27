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
package com.reprezen.swagedit.editor;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;
import com.reprezen.swagedit.schema.SwaggerSchema;

/**
 * SwaggerDocument
 * 
 */
public class SwaggerDocument extends Document {

    private final Yaml yaml = new Yaml();
    private final ObjectMapper mapper = io.swagger.util.Yaml.mapper();

    private JsonNode jsonContent;
    private Node yamlContent;

    private Exception yamlError;
    private Exception jsonError;
    private Model model;
    private SwaggerSchema schema;

    public SwaggerDocument() {
        this.schema = new SwaggerSchema();
    }

    public SwaggerSchema getSchema() {
        return schema;
    }

    public Exception getYamlError() {
        return yamlError;
    }

    public Exception getJsonError() {
        return jsonError;
    }

    /**
     * Returns YAML abstract representation of the document.
     * 
     * @return Node
     */
    public Node getYaml() {
        if (yamlContent == null) {
            parseYaml(get());
        }

        return yamlContent;
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
        if (jsonContent == null) {
            parseJson(get());
        }

        return jsonContent;
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
        final String content = get();

        parseModel();
        parseYaml(content);

        // No need to parse json if
        // there is already a yaml parsing error.
        if (yamlError != null) {
            jsonContent = null;
        } else {
            parseJson(content);
        }
    }

    private void parseYaml(String content) {
        try {
            yamlContent = yaml.compose(new StringReader(content));
            yamlError = null;
        } catch (Exception e) {
            yamlContent = null;
            yamlError = e;
        }
    }

    private void parseJson(String content) {
        try {
            Object expandedYamlObject = new com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml().load(content);
            jsonContent = mapper.valueToTree(expandedYamlObject);
            jsonError = null;
        } catch (Exception e) {
            jsonContent = null;
            jsonError = e;
        }
    }

    private void parseModel() {
        try {
            model = Model.parseYaml(schema, get());
        } catch (Exception e) {
            // e.printStackTrace();
            model = null;
        }
    }

    public Model getModel() {
        if (model == null) {
            parseModel();
        }
        return model;
    }

    public Model getModel(int offset) {
        try {
            if (0 > offset || offset > getLength()) {
                return Model.parseYaml(schema, get());
            } else {
                return Model.parseYaml(schema, get(0, offset));
            }
        } catch (BadLocationException e) {
            return null;
        }
    }

    public JsonPointer getPath(int line, int column) {
        return getModel().getPath(line, column);
    }

    public JsonPointer getPath(IRegion region) {
        int lineOfOffset;
        try {
            lineOfOffset = getLineOfOffset(region.getOffset());
        } catch (BadLocationException e) {
            return null;
        }

        return getModel().getPath(lineOfOffset, getColumnOfOffset(lineOfOffset, region));
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
        Model model = getModel();
        if (model == null) {
            return null;
        }

        AbstractNode node = model.find(pointer);
        if (node == null) {
            return new Region(0, 0);
        }

        Position position = node.getPosition(this);

        return new Region(position.getOffset(), position.getLength());
    }

}
