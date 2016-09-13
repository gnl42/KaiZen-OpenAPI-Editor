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
import com.reprezen.swagedit.json.SwaggerSchema;

public class Model {

    private final Map<JsonPointer, AbstractNode> nodes = new LinkedHashMap<>();
    private final SwaggerSchema schema;

    Model(SwaggerSchema schema) {
        this.schema = schema;
    }

    /**
     * 
     * 
     * @param text
     * @return model
     */
    public static Model parseYaml(SwaggerSchema schema, String text) {
        Model model = new Model(schema);

        if (Strings.emptyToNull(text) == null) {
            model.add(new ObjectNode(null, JsonPointer.compile("")));
        } else {
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
        }

        for (AbstractNode node : model.allNodes()) {
            node.setType(model.schema.getType(node));
        }

        return model;
    }

    public static ObjectMapper createMapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(AbstractNode.class, new NodeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    public SwaggerSchema getSchema() {
        return schema;
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

            int c = startColumn(found);
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

    protected AbstractNode findCorrectNode(AbstractNode current, int column) {
        if (startColumn(current) < column) {
            return current;
        } else {
            return findCorrectNode(current.getParent(), column);
        }
    }

    protected AbstractNode forLine(int line) {
        final AbstractNode root = getRoot();
        for (AbstractNode node : allNodes()) {
            if (node != root && startLine(node) == line) {
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

            if (startLine(current) < line) {
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

    protected int startLine(AbstractNode n) {
        return n.getStart().getLineNr() - 1;
    }

    protected int startColumn(AbstractNode n) {
        return n.getStart().getColumnNr() - 1;
    }

    protected int endLine(AbstractNode n) {
        return n.getEnd().getLineNr() - 1;
    }

    protected int endColumn(AbstractNode n) {
        return n.getEnd().getColumnNr() - 1;
    }

    protected int contentLine(AbstractNode n) {
        return n.getLocation().getLineNr() - 1;
    }

    protected int contentColumn(AbstractNode n) {
        return n.getLocation().getColumnNr() - 1;
    }

    public Iterable<AbstractNode> allNodes() {
        return nodes.values();
    }

}
