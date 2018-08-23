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
package com.reprezen.swagedit.core.model;

import static com.reprezen.swagedit.core.model.NodeDeserializer.ATTRIBUTE_MODEL;
import static com.reprezen.swagedit.core.model.NodeDeserializer.ATTRIBUTE_PARENT;
import static com.reprezen.swagedit.core.model.NodeDeserializer.ATTRIBUTE_POINTER;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.reprezen.swagedit.core.schema.CompositeSchema;

/**
 * Represents the content of a YAML/JSON document.
 *
 */
public class Model {

    private final Map<JsonPointer, AbstractNode> nodes = new LinkedHashMap<>();
    private final CompositeSchema schema;
    private IPath path;

    private Model(CompositeSchema schema) {
        this(schema, null);
    }

    private Model(CompositeSchema schema, IPath path) {
        this.schema = schema;
        this.path = path;
    }

    /**
     * Returns an empty model
     * 
     * @param schema
     * @return empty model
     */
    public static Model empty(CompositeSchema schema) {
        Model model = new Model(schema);
        ObjectNode root = new ObjectNode(model, null, JsonPointer.compile(""));
        root.setType(model.schema.getType(root));
        model.add(root);

        return model;
    }

    /**
     * Returns a model build by parsing a YAML content.
     * 
     * @param text
     * @return model
     */
    public static Model parseYaml(CompositeSchema schema, String text) {
        if (Strings.emptyToNull(text) == null) {
            return empty(schema);
        }

        Model model = new Model(schema);
        try {
            reader(model).readValue(text);
        } catch (IllegalArgumentException | IOException e) {
            return null;
        }

        for (AbstractNode node : model.allNodes()) {
            node.setType(model.schema.getType(node));
        }

        return model;
    }

    public static Model parse(CompositeSchema schema, JsonNode document) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String text = null;
        try {
            text = mapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return parseYaml(schema, text);
    }

    /**
     * Parses all files into a list of models.
     * 
     * @param files
     * @return list of models
     */
    public static Iterable<Model> parseYaml(Iterable<IFile> files, final CompositeSchema schema) {
        if (files == null || Iterables.isEmpty(files)) {
            return Arrays.asList();
        }

        final List<Model> models = new ArrayList<>();
        for (IFile file : files) {
            Model model = new Model(schema, file.getFullPath());
            try {
                reader(model).readValue(file.getLocationURI().toURL());
            } catch (IllegalArgumentException | IOException e) {
                e.printStackTrace();
                continue;
            }

            models.add(model);
        }
        return models;
    }

    protected static ObjectMapper createMapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(AbstractNode.class, new NodeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    protected static ObjectReader reader(Model model) {
        return createMapper().reader() //
                .withAttribute(ATTRIBUTE_MODEL, model) //
                .withAttribute(ATTRIBUTE_PARENT, null) //
                .withAttribute(ATTRIBUTE_POINTER, JsonPointer.compile("")) //
                .forType(AbstractNode.class);
    }

    /**
     * Creates a new object node
     * 
     * @param node
     *            parent or null
     * @param node
     *            pointer
     * @return object node
     */
    public ObjectNode objectNode(AbstractNode parent, JsonPointer ptr) {
        return (ObjectNode) add(new ObjectNode(this, parent, ptr));
    }

    /**
     * Creates a new array node
     * 
     * @param node
     *            parent or null
     * @param node
     *            pointer
     * @return array node
     */
    public ArrayNode arrayNode(AbstractNode parent, JsonPointer ptr) {
        return (ArrayNode) add(new ArrayNode(this, parent, ptr));
    }

    /**
     * Creates a new value node
     * 
     * @param node
     *            parent or null
     * @param node
     *            pointer
     * @param node
     *            value
     * @return value node
     */
    public ValueNode valueNode(AbstractNode parent, JsonPointer ptr, Object value) {
        return (ValueNode) add(new ValueNode(this, parent, ptr, value));
    }

    /**
     * Returns the path of the file that contains the model content.
     * 
     * @param path
     */
    public IPath getPath() {
        return path;
    }

    public void setPath(IPath path) {
        this.path = path;
    }

    /**
     * Returns the node inside the model that can be
     * 
     * @param pointer
     * @return node
     */
    public AbstractNode find(JsonPointer pointer) {
        return nodes.get(pointer);
    }

    public AbstractNode find(String pointer) {
        if (Strings.emptyToNull(pointer) == null) {
            return null;
        }
        if (pointer.startsWith("#")) {
            pointer = pointer.substring(1);
        }
        if (pointer.length() > 1 && pointer.endsWith("/")) {
            pointer = pointer.substring(0, pointer.length() - 1);
        }

        try {
            pointer = URLDecoder.decode(pointer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // leave the pointer as it is
        }

        try {
            return nodes.get(JsonPointer.valueOf(pointer));
        } catch (Exception e) {
            return null;
        }
    }

    private AbstractNode add(AbstractNode node) {
        if (node != null && node.getPointer() != null) {
            nodes.put(node.getPointer(), node);
        }
        return node;
    }

    /**
     * Returns the model's root node.
     * 
     * @return node
     */
    public AbstractNode getRoot() {
        return nodes.get(JsonPointer.compile(""));
    }

    /**
     * Returns the pointer for the node whose content is at the position specified by a line and column.
     * 
     * @param line
     * @param column
     * @return
     */
    public JsonPointer getPath(int line, int column) {
        AbstractNode node = getNode(line, column);
        if (node != null) {
            return node.getPointer();
        }
        return JsonPointer.compile("");
    }

    /**
     * Returns the node whose content is at the position specified by a line and column.
     * 
     * @param line
     * @param column
     * @return
     */
    public AbstractNode getNode(int line, int column) {
        if (column == 0) {
            return getRoot();
        }

        AbstractNode found = forLine(line);
        if (found != null) {
            found = findChildren(found, line, column);
            int c = found.getStart().getColumn();
            if (column > c || (column == c && found.getParent().isArray())) {
                return found;
            } else {
                return found.getParent();
            }
        } else {
            found = findBeforeLine(line, column);
            if (found != null) {
                return findCorrectNode(found, column);
            }
        }

        return found;
    }

    /**
     * Returns all nodes within this model
     * 
     * @return iterable of nodes
     */
    public Iterable<AbstractNode> allNodes() {
        return nodes.values();
    }

    protected AbstractNode findChildren(AbstractNode current, int line, int column) {
        for (AbstractNode el : current.elements()) {
            if (el.getStart().getLine() == line) {
                if (el instanceof ValueNode) {
                    if (column >= contentColumn(el)) {
                        return el;
                    }
                } else {
                    if (column >= el.getStart().getColumn()) {
                        return el;
                    }
                }
            }
        }
        return current;
    }

    protected AbstractNode findCorrectNode(AbstractNode current, int column) {
        if (current.getStart().getColumn() == column) {
            if (current.getParent() instanceof ObjectNode) {
                return current.getParent();
            }
        }

        if (current.getStart().getColumn() < column) {
            return current;
        } else {
            return findCorrectNode(current.getParent(), column);
        }
    }

    protected AbstractNode forLine(int line) {
        final AbstractNode root = getRoot();
        for (AbstractNode node : allNodes()) {
            if (node != root && node.getStart().getLine() == line) {
                return node;
            }
        }
        return null;
    }

    protected AbstractNode findBeforeLine(int line, int column) {
        AbstractNode root = getRoot();
        AbstractNode found = null, before = null;
        Iterator<AbstractNode> it = allNodes().iterator();

        while (found == null && it.hasNext()) {
            AbstractNode current = it.next();
            if (root == current) {
                continue;
            }

            if (current.getStart().getLine() < line) {
                before = current;
            } else {
                found = before;
            }
        }

        if (found == null && before != null) {
            found = before;
        }

        return found;
    }

    protected int contentColumn(AbstractNode n) {
        String property = Strings.emptyToNull(n.getProperty());
        if (property == null) {
            return n.getStart().getColumn();
        }

        return (property.length() + 1) + n.getStart().getColumn();
    }

    /**
     * Returns all the nodes whose type match the given pointer.
     * 
     * @param typePointer
     *            pointer of a type present in the schema
     * @return list of nodes being instance of the type
     */
    public List<AbstractNode> findByType(JsonPointer typePointer) {
        List<AbstractNode> instances = new ArrayList<>();
        for (AbstractNode node : allNodes()) {
            if (node.getType() != null && typePointer.equals(node.getType().getPointer())) {
                instances.add(node);
            }
        }
        return instances;
    }

    /**
     * Returns the schema associated to this model.
     * 
     * @return schema
     */
    public CompositeSchema getSchema() {
        return schema;
    }

}
