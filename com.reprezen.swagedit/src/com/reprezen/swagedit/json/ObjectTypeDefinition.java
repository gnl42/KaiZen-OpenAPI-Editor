package com.reprezen.swagedit.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;
import com.reprezen.swagedit.model.AbstractNode;

public class ObjectTypeDefinition extends TypeDefinition {

    public Map<String, TypeDefinition> patternProperties = new HashMap<>();

    public ObjectTypeDefinition(JsonNode schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);
        init();
    }

    @Override
    public Set<JsonNode> getProposals(AbstractNode element) {
        return createObjectProposal(definition, element);
    }

    private void init() {
        initProperties("properties", properties);
        initProperties("patternProperties", patternProperties);
    }

    private void initProperties(String container, Map<String, TypeDefinition> properties) {
        if (definition.has(container)) {
            for (Iterator<Entry<String, JsonNode>> it = definition.get(container).fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();
                JsonPointer pointer = getPropertyPointer(container, e.getKey(), e.getValue());

                properties.put(e.getKey(), TypeDefinition.create(schema, pointer));
            }
        }
    }

    private JsonPointer getPropertyPointer(String container, String key, JsonNode node) {
        String p = getPointer().toString();
        if (node.isObject() && node.has(JsonReference.PROPERTY)) {
            p = node.get(JsonReference.PROPERTY).asText().substring(1);
        } else {
            p += "/" + container + "/" + key;
        }

        return JsonPointer.compile(p);
    }

    public TypeDefinition findMatchingPattern(String path) {
        path = path.replaceAll("~1", "/");
        TypeDefinition found = null;
        Iterator<String> patterns = patternProperties.keySet().iterator();

        while (patterns.hasNext() && found == null) {
            String pattern = patterns.next();
            if (path.matches(pattern)) {
                found = patternProperties.get(pattern);
            }
        }

        return found;
    }

}