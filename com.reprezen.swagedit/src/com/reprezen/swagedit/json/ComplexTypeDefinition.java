package com.reprezen.swagedit.json;

import static com.google.common.collect.Iterators.transform;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.reprezen.swagedit.model.AbstractNode;

public class ComplexTypeDefinition extends TypeDefinition {

    public ComplexTypeDefinition(JsonNode schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);
    }

    @Override
    public Set<JsonNode> getProposals(AbstractNode node) {
        return newHashSet(
                transform(transform(definition.get(type.getValue()).elements(), new Function<JsonNode, JsonNode>() {
                    @Override
                    public JsonNode apply(JsonNode n) {
                        return resolve(schema, n);
                    }
                }), new Function<JsonNode, JsonNode>() {
                    @Override
                    public JsonNode apply(JsonNode n) {
                        // TODO
                        return n;
                    }
                }));

    }
}