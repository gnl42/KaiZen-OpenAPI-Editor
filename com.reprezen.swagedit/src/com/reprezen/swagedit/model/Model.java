package com.reprezen.swagedit.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;

public class Model {

    private final Map<JsonPointer, AbstractNode> nodes = new LinkedHashMap<>();

    public static Model parseYaml(String text) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(AbstractNode.class, new NodeDeserializer());
        mapper.registerModule(module);

        Model model = new Model();
        mapper.reader() //
                .withAttribute("model", model) //
                .withAttribute("parent", null) //
                .withAttribute("pointer", JsonPointer.compile("")) //
                .withType(AbstractNode.class) //
                .readValue(text);

        return model;
    }

    public AbstractNode find(String path) {
        if (Strings.emptyToNull(path) == null || path.equals(":")) {
            return getRoot();
        }

        return nodes.get(pointer(path));
    }

    protected JsonPointer pointer(String pointer) {
        return JsonPointer.compile(pointer.replaceAll("/", "~1").replaceAll(":", "/"));
    }

    public void add(AbstractNode node) {
        nodes.put(node.getPointer(), node);
    }

    public AbstractNode getRoot() {
        return nodes.get(JsonPointer.compile(""));
    }

    public AbstractNode getNode(final int line, int column) {
        int i = 0;
        AbstractNode found = null;
        // find elements before line
        Object[] pointers = nodes.keySet().toArray();
        for (; i < pointers.length; i++) {
            AbstractNode node = nodes.get(pointers[i]);
            JsonLocation location = node.getStart() != null ? node.getStart() : node.getLocation();

            int nodeLine = location.getLineNr() - 1;
            if (nodeLine > line) {
                break;
            }
        }

        int j = 0;
        for (; j < i; j++) {
            AbstractNode node = nodes.get(pointers[j]);
            // the last one
            if (j + 1 >= i) {
                return node;
            }

            JsonLocation location = node.getLocation();
            int nodeLine = location.getLineNr() - 1;
            int nodeColumn = location.getColumnNr() - 1;

            if (nodeLine == line && nodeColumn >= column) {
                return node;
            }
        }

        return found;
    }
}
