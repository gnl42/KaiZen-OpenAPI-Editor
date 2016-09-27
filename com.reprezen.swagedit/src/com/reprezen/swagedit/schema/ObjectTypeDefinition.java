package com.reprezen.swagedit.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.json.references.JsonReference;

/**
 * Represents a JSON schema type definition for objects.
 * 
 * <br/>
 * 
 * Example of an object type definition:
 * 
 * <code>
 * <pre>
 * {
 *   "type": "object",
 *   "required": [
 *     "swagger",
 *     info",
 *     "paths"
 *   ],
 *   "properties": {
 *     "swagger": ...
 *   }
 * }
 * </pre>
 * </code>
 *
 */
public class ObjectTypeDefinition extends TypeDefinition {

    private final List<String> requiredProperties = new ArrayList<>();
    private final Map<String, TypeDefinition> properties = new LinkedHashMap<>();
    private final Map<String, TypeDefinition> patternProperties = new LinkedHashMap<>();
    private final Map<String, TypeDefinition> additionalProperties = new LinkedHashMap<>();

    public ObjectTypeDefinition(SwaggerSchema schema, JsonPointer pointer, JsonNode definition, JsonType type) {
        super(schema, pointer, definition, type);
        init();
    }

    private void init() {
        initRequired();
        initProperties("properties", properties);
        initProperties("patternProperties", patternProperties);
        initProperties("additionalProperties", additionalProperties);

        if (content.has("definitions") && content.get("definitions").isObject()) {
            JsonNode definitions = content.get("definitions");

            for (Iterator<Entry<String, JsonNode>> it = definitions.fields(); it.hasNext();) {
                Entry<String, JsonNode> e = it.next();
                JsonPointer pointer = JsonPointer.compile(getPointer().toString() + "/definitions/" + e.getKey());

                if (pointer != null && !pointer.equals(getPointer()) && schema.getType(pointer) == null) {
                    TypeDefinition.create(schema, pointer);
                }
            }
        }
    }

    private void initRequired() {
        JsonNode required = content.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode value : required) {
                requiredProperties.add(value.asText());
            }
        }
    }

    private void initProperties(String container, Map<String, TypeDefinition> properties) {
        if (content.has(container) && content.get(container).isObject()) {

            JsonNode node = content.get(container);
            if (JsonReference.isReference(node)) {
                node = schema.asJson().at(JsonReference.getPointer(node));
            }

            for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
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

        if (JsonReference.isReference(node)) {
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
            type = getPatternType(property);
        }
        return type;
    }

    /**
     * Returns the list of properties that must be present in an instance of this type.
     * 
     * @return list of required properties
     */
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Returns the list of properties that can be added to an instance of this type.
     * 
     * @return list of properties
     */
    public Map<String, TypeDefinition> getProperties() {
        return properties;
    }

    /**
     * Returns the list of pattern properties that can be added to an instance of this type.
     * 
     * @return list of pattern properties
     */
    public Map<String, TypeDefinition> getPatternProperties() {
        return patternProperties;
    }

    /**
     * Returns the list of additional properties that can be added to an instance of this type.
     * 
     * @return list of additional properties
     */
    public Map<String, TypeDefinition> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Returns the type definition associated to pattern property that matches the given property.
     * 
     * @param property
     * @return type
     */
    public TypeDefinition getPatternType(String property) {
        TypeDefinition found = null;
        Iterator<String> patterns = patternProperties.keySet().iterator();

        property = property.replaceAll("~1", "/");

        while (patterns.hasNext() && found == null) {
            String pattern = patterns.next();
            Matcher matcher = Pattern.compile(pattern).matcher(property);
            if (matcher.find() || matcher.matches()) {
                found = patternProperties.get(pattern);
            }
        }

        return found;
    }

}