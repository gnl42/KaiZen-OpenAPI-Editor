package com.reprezen.swagedit.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;

public class ObjectTypeDefinition extends TypeDefinition {

    private final Map<String, TypeDefinition> properties = new HashMap<>();
    private final Map<String, TypeDefinition> patternProperties = new HashMap<>();

    public ObjectTypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);
        init();
    }

    private void init() {
        initProperties("properties", properties);
        initProperties("patternProperties", patternProperties);

        if (definition.has("definitions")) {
            JsonNode definitions = definition.get("definitions");

            for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();
                JsonPointer pointer = JsonPointer.compile(getPointer().toString() + "/definitions/" + e.getKey());

                if (pointer != null && !pointer.equals(getPointer()) && schema.getType(pointer) == null) {
                    TypeDefinition.create(schema, pointer);
                }
            }
        }
    }

    private void initProperties(String container, Map<String, TypeDefinition> properties) {
        if (definition.has(container)) {
            for (Iterator<Entry<String, JsonNode>> it = definition.get(container).fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();
                JsonPointer pointer = getPropertyPointer(container, e.getKey(), e.getValue());

                if (pointer != null) {
                    if (pointer.equals(getPointer())) {
                        properties.put(e.getKey(), this);
                    } else if (schema.getType(pointer) != null) {
                        properties.put(e.getKey(), schema.getType(pointer));
                    } else {
                        properties.put(e.getKey(), TypeDefinition.create(schema, pointer));
                    }
                }
            }
        }
    }

    private JsonPointer getPropertyPointer(String container, String key, JsonNode node) {
        String p = getPointer().toString();

        if (node.isObject() && node.has(JsonReference.PROPERTY)) {
            String ref = node.get(JsonReference.PROPERTY).asText();

            if (ref.startsWith("http") || ref.startsWith("https")) {
                return null;
            }

            p = ref.substring(1);
        } else {
            p += "/" + container + "/" + key;
        }

        return JsonPointer.compile(p);
    }

    @Override
    public TypeDefinition getPropertyType(String property) {
        TypeDefinition type = getProperties().get(property);

        if (type == null) {
            type = findMatchingPattern(property);
        }
        return type;
    }

    public Map<String, TypeDefinition> getProperties() {
        return properties;
    }

    public Map<String, TypeDefinition> getPatternProperties() {
        return patternProperties;
    }

    public TypeDefinition findMatchingPattern(String path) {
        TypeDefinition found = null;
        Iterator<String> patterns = patternProperties.keySet().iterator();

        path = path.replaceAll("~1", "/");

        while (patterns.hasNext() && found == null) {
            String pattern = patterns.next();
            Matcher matcher = Pattern.compile(pattern).matcher(path);
            if (matcher.find() || matcher.matches()) {
                found = patternProperties.get(pattern);

            }
        }

        return found;
    }

}