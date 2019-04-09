/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.core.validation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.editor.JsonDocument;

public class JsonSchemaValidator {

    private final LoadingConfiguration loadingConfiguration;
    private final JsonSchemaFactory factory;
    private final JsonNode schema;

    public JsonSchemaValidator(JsonNode schema, Map<String, JsonNode> preloadSchemas) {
        this.schema = schema;
        this.loadingConfiguration = getLoadingConfiguration(preloadSchemas);
        this.factory = JsonSchemaFactory.newBuilder() //
                .setLoadingConfiguration(loadingConfiguration) //
                .freeze();
    }

    private LoadingConfiguration getLoadingConfiguration(Map<String, JsonNode> preloadSchemas) {
        LoadingConfigurationBuilder loadingConfigurationBuilder = LoadingConfiguration.newBuilder();
        for (String nextSchemaUri : preloadSchemas.keySet()) {
            loadingConfigurationBuilder.preloadSchema(nextSchemaUri, preloadSchemas.get(nextSchemaUri));
        }
        return loadingConfigurationBuilder.freeze();
    }

    public Set<SwaggerError> validate(JsonDocument document) {
        final ErrorProcessor processor = new ErrorProcessor(document.getYaml(), schema);
        final Set<SwaggerError> errors = new HashSet<>();

        JsonSchema jsonSchema = null;
        try {
            jsonSchema = factory.getJsonSchema(schema);
        } catch (ProcessingException e) {
            YEditLog.logException(e);
            return errors;
        }

        try {
            errors.addAll(processor.processReport(jsonSchema.validate(document.asJson(), true)));
        } catch (ProcessingException e) {
            errors.addAll(processor.processMessage(e.getProcessingMessage()));
        }

        return errors;
    }

    public Set<JsonNode> validate(JsonNode instance) {
        JsonSchema jsonSchema = null;
        try {
            jsonSchema = factory.getJsonSchema(schema);
        } catch (ProcessingException e) {
            Activator.getDefault().logError(e.getLocalizedMessage(), e);
            return null;
        }

        return doValidate(jsonSchema, instance);
    }

    public Set<JsonNode> validate(JsonNode instance, String schemaPointer) {
        JsonSchema jsonSchema = null;
        try {
            jsonSchema = factory.getJsonSchema(schema, schemaPointer);
        } catch (ProcessingException e) {
            Activator.getDefault().logError(e.getLocalizedMessage(), e);
            return null;
        }

        return doValidate(jsonSchema, instance);
    }

    private Set<JsonNode> doValidate(JsonSchema schema, JsonNode instance) {
        Set<JsonNode> errors = new HashSet<>();
        try {
            schema.validate(instance, true).forEach(message -> {
                errors.add(message.asJson());
            });
        } catch (ProcessingException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return errors;
    }

    public static int getLevel(JsonNode message) {
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
}
