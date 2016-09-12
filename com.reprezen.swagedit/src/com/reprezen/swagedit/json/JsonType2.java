package com.reprezen.swagedit.json;

import static com.google.common.collect.Iterators.transform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.model.AbstractNode;

public class JsonType2 {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected JsonNode schema;
    protected JsonNode definition;
    protected JsonType2 parent;

    public Map<String, JsonType2> properties = new HashMap<>();

    protected String containingProperty;

    public JsonNode getDefinition() {
        return definition;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public JsonType2 getParent() {
        return parent;
    }

    public String getContainingProperty() {
        return containingProperty;
    }

    public static class ComplexType extends JsonType2 {
        public ComplexType(JsonNode schema, JsonNode definition) {
            super(schema, definition);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Set<JsonNode> getProposals(AbstractNode node) {
            JsonType t = JsonType.valueOf(definition);

            if (t == JsonType.ONE_OF) {
                return Sets.newHashSet(
                        transform(transform(definition.get("oneOf").elements(), new Function<JsonNode, JsonNode>() {
                            @Override
                            public JsonNode apply(JsonNode n) {
                                return resolve(schema, n);
                            }
                        }), new Function<JsonNode, JsonNode>() {
                            @Override
                            public JsonNode apply(JsonNode n) {
                                System.out.println(n);
                                return n;
                            }
                        }));
            }

            return super.getProposals(node);
        }
    }

    public static class ObjectType extends JsonType2 {

        public Map<String, JsonType2> patternProperties = new HashMap<>();

        public ObjectType(JsonNode schema, JsonNode definition) {
            super(schema, definition);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Set<JsonNode> getProposals(AbstractNode element) {
            return createObjectProposal(definition, element);
        }

    }

    public static class ArrayType extends JsonType2 {
        public ArrayType(JsonNode schema, JsonNode definition) {
            super(schema, definition);
            // TODO Auto-generated constructor stub
        }

        public JsonNode items;
        public JsonType itemsType;

        @Override
        public Set<JsonNode> getProposals(AbstractNode node) {
            final Set<JsonNode> proposals = new LinkedHashSet<>();
            proposals.add(mapper.createObjectNode() //
                    .put("value", "-") //
                    .put("label", "-") //
                    .put("type", "array item"));

            return proposals;
        }
    }

    public JsonType2(JsonNode schema, JsonNode definition) {
        this.schema = schema;
        this.definition = definition;
    }

    @Override
    public String toString() {
        return definition.toString();
    }

    protected JsonNode createPropertyProposal(String key, JsonNode value) {
        final JsonNode resolved = resolve(schema, value);
        final JsonType type = JsonType.valueOf(resolved);
        System.out.println(resolved);

        String description = resolved.has("description") ? resolved.get("description").asText() : "";

        // String des = type == JsonType.UNDEFINED && resolvedDefinition.descriptor != null
        // ? resolvedDefinition.descriptor : type.getValue();

        return mapper.createObjectNode() //
                .put("value", key + ":") //
                .put("label", key) //
                .put("description", description) //
                .put("type", type.getValue());
    }

    protected Set<JsonNode> createObjectProposal(JsonNode definition, AbstractNode element) {
        Set<JsonNode> proposals = new LinkedHashSet<>();

        if (definition.has("properties")) {
            final JsonNode properties = definition.get("properties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                final String key = it.next();

                if (element.get(key) == null) {
                    proposals.add(createPropertyProposal(key, properties.get(key)));
                }
            }
        }

        if (definition.has("patternProperties")) {
            final JsonNode properties = definition.get("patternProperties");

            for (Iterator<String> it = properties.fieldNames(); it.hasNext();) {
                String key = it.next();
                final JsonNode value = properties.get(key);

                if (key.startsWith("^")) {
                    key = key.substring(1);
                }

                proposals.add(createPropertyProposal(key, value));
            }
        }

        if (proposals.isEmpty()) {
            proposals.add(mapper.createObjectNode() //
                    .put("value", "_key_" + ":") //
                    .put("label", "_key_"));
        }

        return proposals;
    }

    public Set<JsonNode> getProposals(AbstractNode node) {
        Set<JsonNode> proposals = new LinkedHashSet<>();
        JsonType type = JsonType.valueOf(definition);
        System.out.println(definition);
        if (type == JsonType.STRING || type == JsonType.INTEGER) {

            proposals.add(mapper.createObjectNode().put("value", "") //
                    .put("label", "") //
                    .put("type", "string"));

        } else if (type == JsonType.BOOLEAN) {
            proposals.add(mapper.createObjectNode().put("value", "true").put("label", "true"));
            proposals.add(mapper.createObjectNode().put("value", "false").put("label", "false"));

        } else if (type == JsonType.ENUM) {
            final String subType = definition.has("type") ? definition.get("type").asText() : null;
            for (JsonNode literal : definition.get("enum")) {
                String value = literal.asText();

                // if the type of array is string and
                // current value is a number, it should be put
                // into quotes to avoid validation issues
                if (NumberUtils.isNumber(value) && "string".equals(subType)) {
                    value = "\"" + value + "\"";
                }

                proposals.add(mapper.createObjectNode() //
                        .put("value", value) //
                        .put("label", literal.asText()));
            }
        }

        return proposals;
    }

    public static JsonType2 create(JsonNode schema, JsonPointer pointer) {
        JsonNode definition = schema.at(pointer);
        JsonType type = JsonType.valueOf(definition);

        if (type == JsonType.OBJECT) {

            ObjectType t = new ObjectType(schema, definition);
            if (definition.has("properties")) {
                for (Iterator<Entry<String, JsonNode>> it = definition.get("properties").fields(); it.hasNext();) {
                    Entry<String, JsonNode> e = it.next();
                    JsonNode node = e.getValue();

                    String p;
                    if (node.isObject() && node.has("$ref")) {
                        p = node.get("$ref").asText().substring(1);
                    } else {
                        p = "/properties/" + e.getKey();
                    }

                    t.properties.put(e.getKey(), JsonType2.create(schema, JsonPointer.compile(p)));
                }
            }

            if (definition.has("patternProperties")) {
                for (Iterator<Entry<String, JsonNode>> it = definition.get("patternProperties").fields(); it
                        .hasNext();) {
                    Entry<String, JsonNode> e = it.next();
                    JsonNode node = e.getValue();

                    String p;
                    if (node.isObject() && node.has("$ref")) {
                        p = node.get("$ref").asText().substring(1);
                    } else {
                        p = "/patternProperties/" + e.getKey();
                    }

                    t.patternProperties.put(e.getKey(), JsonType2.create(schema, JsonPointer.compile(p)));
                }
            }

            return t;
        } else if (type == JsonType.ARRAY) {
            return new ArrayType(schema, definition);
        } else {
            return new JsonType2(schema, definition);
        }
    }

    protected static String getProperty(JsonPointer pointer) {
        String s = pointer.toString();
        return s.substring(s.lastIndexOf("/") + 1).replaceAll("~1", "/");
    }

    protected static JsonNode resolve(JsonNode schema, JsonNode node) {
        if (node.isObject() && node.has("$ref")) {
            JsonPointer p = JsonPointer.compile(node.get("$ref").asText().substring(1));
            return schema.at(p);
        }

        return node;
    }

}
