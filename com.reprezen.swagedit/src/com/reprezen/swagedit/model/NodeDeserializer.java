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

    @Override
    public AbstractNode deserialize(JsonParser p, DeserializationContext context)
            throws IOException, JsonProcessingException {

        final Model model = (Model) context.getAttribute("model");
        final AbstractNode parent = (AbstractNode) context.getAttribute("parent");
        final JsonPointer ptr = (JsonPointer) context.getAttribute("pointer");

        JsonLocation startLocation = p.getTokenLocation();
        if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
        }

        if (p.getCurrentToken() == JsonToken.START_OBJECT) {

            ObjectNode node = new ObjectNode(parent, ptr, p.getCurrentLocation());
            node.setStartLocation(startLocation);
            model.add(node);

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.getCurrentName();

                JsonPointer pp = JsonPointer.compile(ptr.toString() + "/" + name.replaceAll("/", "~1"));

                context.setAttribute("parent", node);
                context.setAttribute("pointer", pp);

                AbstractNode v = deserialize(p, context);
                v.setProperty(name);
                node.put(name, v);
            }

            node.setEndLocation(p.getCurrentLocation());
            return node;

        } else if (p.getCurrentToken() == JsonToken.START_ARRAY) {
            ArrayNode node = new ArrayNode(parent, ptr, p.getCurrentLocation());
            model.add(node);

            int i = 0;
            while (p.nextToken() != JsonToken.END_ARRAY) {
                JsonPointer pp = JsonPointer.compile(ptr.toString() + "/" + i);

                context.setAttribute("parent", node);
                context.setAttribute("pointer", pp);

                AbstractNode v = deserialize(p, context);
                node.add(v);
                i++;
            }

            node.setStartLocation(startLocation);
            node.setEndLocation(p.getCurrentLocation());
            return node;

        } else {

            JsonLocation location = p.getCurrentLocation();
            Object v = context.readValue(p, Object.class);

            ValueNode node = new ValueNode(parent, ptr, v, location);
            node.setStartLocation(startLocation);
            node.setEndLocation(p.getCurrentLocation());
            model.add(node);
            return node;

        }
    }

}
