package com.reprezen.swagedit.core.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;

public class ExampleValidator {

    private final JsonNode document;

    public ExampleValidator(JsonDocument document) {
        this.document = document.asJson();
    }

    public static class ExampleSchemaValidator extends JsonSchemaValidator {

        public ExampleSchemaValidator(JsonNode schema) {
            super(schema, Collections.emptyMap());
        }

    }

    public Set<SwaggerError> validate(AbstractNode node) {
        if (node.getPointerString().matches(".*/example")) {
            if (node.getParent().get("schema") != null) {
                JsonNode example = document.at(node.getPointer());
                return doValidate(node, example);
            }
        }

        if (node.getPointerString().matches(".*/examples")) {
            final Set<SwaggerError> errors = new HashSet<>();
            final JsonNode examples = document.at(node.getPointer());

            examples.fields().forEachRemaining(entry -> {
                JsonNode example = entry.getValue();
                // OpenAPI3 example is under value
                if (example.has("value")) {
                    example = example.get("value");
                }

                errors.addAll(doValidate(node, example));
            });

            return errors;
        }

        return Collections.emptySet();
    }

    private Set<SwaggerError> doValidate(AbstractNode node, JsonNode example) {
        final Set<SwaggerError> errors = new HashSet<>();

        JsonNode schema = document.at(node.getParent().getPointer()).get("schema");
        if (schema.has("$ref")) {
            schema = document.at(schema.get("$ref").asText().substring(1));
        }

        schema = resolve(schema);

        new ExampleSchemaValidator(schema).validateSubSchema(example, "").forEach(message -> {
            int line = node.getStart().getLine() + 1;

            errors.add(new SwaggerError(line, getLevel(message.asJson()), message.getMessage()));
        });

        return errors;
    }

    private int getLevel(JsonNode message) {
        if (message == null || !message.has("level")) {
            return IMarker.SEVERITY_INFO;
        }

        switch (message.get("level").asText()) {
        case "error":
        case "fatal":
            return IMarker.SEVERITY_ERROR;
        case "warning":
            return IMarker.SEVERITY_WARNING;
        default:
            return IMarker.SEVERITY_INFO;
        }
    }

    private JsonNode resolve(JsonNode node) {
        if (node.has("type")) {
            String type = node.get("type").asText();
            if ("array".equalsIgnoreCase(type)) {
                if (node.has("items") && node.get("items").has("$ref")) {
                    JsonNode items = node.get("items");
                    JsonNode resolved = document.at(items.get("$ref").asText().substring(1));
                    ((ObjectNode) node).set("items", resolved);
                }
            }
        }
        return node;
    }
}
