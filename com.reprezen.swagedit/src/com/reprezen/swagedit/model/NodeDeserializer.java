package com.reprezen.swagedit.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class NodeDeserializer extends JsonDeserializer<AbstractNode> {

    public static final String ATTRIBUTE_MODEL = "model";
    public static final String ATTRIBUTE_PARENT = "parent";
    public static final String ATTRIBUTE_POINTER = "pointer";

    @Override
    public AbstractNode deserialize(JsonParser p, DeserializationContext context)
            throws IOException, JsonProcessingException {

        JsonLocation startLocation = p.getTokenLocation();
        if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
        }

        switch (p.getCurrentToken()) {
        case START_OBJECT:
            return deserializeObjectNode(p, context, startLocation);
        case START_ARRAY:
            return deserializeArrayNode(p, context, startLocation);
        default:
            return deserializeValueNode(p, context, startLocation);
        }
    }

    protected ObjectNode deserializeObjectNode(JsonParser p, DeserializationContext context, JsonLocation startLocation)
            throws IllegalArgumentException, IOException {

        final Model model = (Model) context.getAttribute(ATTRIBUTE_MODEL);
        final AbstractNode parent = (AbstractNode) context.getAttribute(ATTRIBUTE_PARENT);
        final JsonPointer ptr = (JsonPointer) context.getAttribute(ATTRIBUTE_POINTER);

        final ObjectNode node = new ObjectNode(parent, ptr, p.getCurrentLocation());
        node.setStartLocation(startLocation);
        model.add(node);

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String name = p.getCurrentName();

            JsonPointer pp = JsonPointer.compile(ptr.toString() + "/" + name.replaceAll("/", "~1"));
            context.setAttribute(ATTRIBUTE_PARENT, node);
            context.setAttribute(ATTRIBUTE_POINTER, pp);

            AbstractNode v = deserialize(p, context);
            v.setProperty(name);
            node.put(name, v);
        }

        node.setEndLocation(p.getCurrentLocation());
        return node;
    }

    protected ArrayNode deserializeArrayNode(JsonParser p, DeserializationContext context, JsonLocation startLocation)
            throws IOException {
        final Model model = (Model) context.getAttribute(ATTRIBUTE_MODEL);
        final AbstractNode parent = (AbstractNode) context.getAttribute(ATTRIBUTE_PARENT);
        final JsonPointer ptr = (JsonPointer) context.getAttribute(ATTRIBUTE_POINTER);

        ArrayNode node = new ArrayNode(parent, ptr, p.getCurrentLocation());
        model.add(node);

        int i = 0;
        while (p.nextToken() != JsonToken.END_ARRAY) {
            JsonPointer pp = JsonPointer.compile(ptr.toString() + "/" + i);

            context.setAttribute(ATTRIBUTE_PARENT, node);
            context.setAttribute(ATTRIBUTE_POINTER, pp);

            AbstractNode v = deserialize(p, context);

            node.add(v);
            i++;
        }

        node.setStartLocation(startLocation);
        node.setEndLocation(p.getCurrentLocation());
        return node;
    }

    protected ValueNode deserializeValueNode(JsonParser p, DeserializationContext context, JsonLocation startLocation)
            throws IOException {
        final Model model = (Model) context.getAttribute(ATTRIBUTE_MODEL);
        final AbstractNode parent = (AbstractNode) context.getAttribute(ATTRIBUTE_PARENT);
        final JsonPointer ptr = (JsonPointer) context.getAttribute(ATTRIBUTE_POINTER);

        JsonLocation location = p.getCurrentLocation();
        Object v = context.readValue(p, Object.class);

        ValueNode node = new ValueNode(parent, ptr, v, location);
        node.setStartLocation(startLocation);
        node.setEndLocation(p.getCurrentLocation());
        model.add(node);
        return node;
    }
}
