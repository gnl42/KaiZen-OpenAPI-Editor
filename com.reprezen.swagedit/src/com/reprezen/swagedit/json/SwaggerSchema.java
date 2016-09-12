package com.reprezen.swagedit.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.swagedit.model.AbstractNode;

public class SwaggerSchema {

    private ObjectMapper mapper = new ObjectMapper();
    protected JsonNode content;
    protected JsonType2 rootType;

    public SwaggerSchema() {
        init();
    }

    protected void init() {
        try {
            content = mapper.readTree(getClass().getResourceAsStream("schema.json"));
        } catch (IOException e) {
            return;
        }

        JsonNode schema = content;
        rootType = JsonType2.create(schema, JsonPointer.compile(""));
    }

    public JsonNode asJson() {
        return content;
    }

    public JsonType2 getType(AbstractNode node) {
        JsonPointer pointer = node.getPointer();

        if (JsonPointer.compile("").equals(pointer)) {
            return rootType;
        }

        String[] paths = pointer.toString().substring(1).split("/");
        JsonType2 current = rootType;

        for (String path : paths) {
            System.out.println(path + " " + current.properties);
            current = current.properties.get(path);
        }

        return current;
    }

}
