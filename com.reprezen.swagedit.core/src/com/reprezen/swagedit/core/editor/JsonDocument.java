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
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicReference;

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
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * SwaggerDocument
 * 
 */
public class JsonDocument extends Document {
    
    private final ObjectMapper mapper;
    private CompositeSchema schema;


    private final Yaml yaml = new Yaml();

    private AtomicReference<Result<JsonNode>> jsonContent = new AtomicReference<>(new Failure<>(null));
    private AtomicReference<Result<Node>> yamlContent = new AtomicReference<>(new Failure<>(null));
    private AtomicReference<Model> model = new AtomicReference<>();

    public JsonDocument(ObjectMapper mapper, CompositeSchema schema) {
        this.mapper = mapper;
        this.schema = schema;
    }

    public Exception getYamlError() {
        return yamlContent.get().getError();
    }

    public Exception getJsonError() {
        return jsonContent.get().getError();
    }

    /**
     * Returns YAML abstract representation of the document.
     * 
     * @return Node
     */
    public Node getYaml() {
        if (yamlContent.get() == null || yamlContent.get().getResult() == null) {
            updateYaml(get());
        }
        return yamlContent.get().getResult();
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
        if (jsonContent.get() == null || jsonContent.get().getResult() == null) {
            updateJson(get());
        }
        return jsonContent.get().getResult();
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
        final String content = get();

        updateModel();
        updateYaml(content);

        // No need to parse json if
        // there is already a yaml parsing error.
        if (!yamlContent.get().isSuccess()) {
            jsonContent.getAndSet(new Failure<>(null));
        } else {
            updateJson(content);
        }
    }

    private void updateYaml(String content) {
        yamlContent.getAndSet(parseYaml(content));
    }

    private Result<Node> parseYaml(String content) {
        try {
            return new Success<>(yaml.compose(new StringReader(content)));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    private void updateJson(String content) {
        jsonContent.getAndSet(parseJson(content));
    }
    
    private Result<JsonNode> parseJson(String content) {
        try {
            Object expandedYamlObject = new Yaml().load(content);
            return new Success<>(mapper.valueToTree(expandedYamlObject));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    private void updateModel() {
        model.getAndSet(parseModel());
    }
    
    private Model parseModel() {
        try {
            return Model.parseYaml(schema, get());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the Model, or null if the spec is invalid YAML
     */
    public Model getModel() {
        if (model.get() == null) {
            model.getAndSet(parseModel());
        }
        return model.get();
    }

    /*
     * Used by code-assist
     */
    public Model getModel(int offset) {
        // no parse errors
        final Model modelValue = model.get();
        if (modelValue != null) {
            return modelValue;
        }
        // parse errors -> return the model at offset, do NOT update the `model` field
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
    
    public interface Result<T> {
        boolean isSuccess();

        T getResult();

        Exception getError();
    }
    
    public final class Success<T> implements Result<T> {
        private final T result;

        public Success(T result) {
            this.result = result;
        }

        public boolean isSuccess() {
            return true;
        }

        @Override
        public Exception getError() {
            return null;
        }

        @Override
        public T getResult() {
            return result;
        }
    }

    public final class Failure<T> implements Result<T> {
        private final Exception error;

        public Failure(Exception error) {
            this.error = error;
        }

        public boolean isSuccess() {
            return false;
        }

        @Override
        public Exception getError() {
            return error;
        }

        @Override
        public T getResult() {
            return null;
        }
    }

}
