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
package com.reprezen.swagedit.model;

import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_MODEL;
import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_PARENT;
import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_POINTER;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.reprezen.swagedit.schema.SwaggerSchema;

/**
 * Represents the content of a YAML/JSON document.
 *
 */
public class Model {

    private final Map<JsonPointer, AbstractNode> nodes = new LinkedHashMap<>();
    private final SwaggerSchema schema;

    Model(SwaggerSchema schema) {
        this.schema = schema;
    }

    /**
     * Returns an empty model
     * 
     * @param schema
     * @return empty model
     */
    public static Model empty(SwaggerSchema schema) {
        Model model = new Model(schema);
        ObjectNode root = new ObjectNode(null, JsonPointer.compile(""));
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
    public static Model parseYaml(SwaggerSchema schema, String text) {
        if (Strings.emptyToNull(text) == null) {
            return empty(schema);
        }

        Model model = new Model(schema);
        try {
            createMapper().reader() //
                    .withAttribute(ATTRIBUTE_MODEL, model) //
                    .withAttribute(ATTRIBUTE_PARENT, null) //
                    .withAttribute(ATTRIBUTE_POINTER, JsonPointer.compile("")) //
                    .withType(AbstractNode.class) //
                    .readValue(text);
        } catch (IllegalArgumentException | IOException e) {
            // model.addError(e);
        }

        for (AbstractNode node : model.allNodes()) {
            node.setType(model.schema.getType(node));
        }

        return model;
    }

    protected static ObjectMapper createMapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(AbstractNode.class, new NodeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    public AbstractNode find(JsonPointer pointer) {
        return nodes.get(pointer);
    }

    public void add(AbstractNode node) {
        if (node != null && node.getPointer() != null) {
            nodes.put(node.getPointer(), node);
        }
    }

    public AbstractNode getRoot() {
        return nodes.get(JsonPointer.compile(""));
    }

    public JsonPointer getPath(int line, int column) {
        AbstractNode node = getNode(line, column);
        if (node != null) {
            return node.getPointer();
        }
        return JsonPointer.compile("");
    }

    public AbstractNode getNode(int line, int column) {
        if (column == 0) {
            return getRoot();
        }

        AbstractNode found = forLine(line);
        if (found != null) {
            found = findChildren(found, line, column);

            int c = found.getStart().getColumn();
            if (column >= c) {
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

    public Iterable<AbstractNode> allNodes() {
        return nodes.values();
    }

}
