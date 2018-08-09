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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.dadacoalition.yedit.YEditLog;
import org.eclipse.ui.IFileEditorInput;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

    private final JsonReferenceValidator referenceValidator;
    private final LoadingConfiguration loadingConfiguration;
    private final JsonSchemaFactory factory;

    public Validator() {
        this(new JsonReferenceValidator(new JsonReferenceFactory()));
    }

    public Validator(JsonReferenceValidator referenceValidator) {
        this(referenceValidator, Maps.<String, JsonNode> newHashMap());
    }

    public Validator(JsonReferenceValidator referenceValidator, Map<String, JsonNode> preloadSchemas) {
        this.referenceValidator = referenceValidator;
        this.loadingConfiguration = getLoadingConfiguration(preloadSchemas);
        this.factory = JsonSchemaFactory.newBuilder() //
                .setLoadingConfiguration(loadingConfiguration) //
                .freeze();
        this.referenceValidator.setFactory(factory);
    }

    private LoadingConfiguration getLoadingConfiguration(Map<String, JsonNode> preloadSchemas) {
        LoadingConfigurationBuilder loadingConfigurationBuilder = LoadingConfiguration.newBuilder();
        for (String nextSchemaUri : preloadSchemas.keySet()) {
            loadingConfigurationBuilder.preloadSchema(nextSchemaUri, preloadSchemas.get(nextSchemaUri));
        }
        return loadingConfigurationBuilder.freeze();
    }

    public JsonSchemaFactory getFactory() {
        return factory;
    }

    public ModelValidator getModelValidator(JsonModel model) {
        return new ModelValidator(model);
    }

    /**
     * Returns a list or errors if validation fails.
     * 
     * This method accepts as input a swagger YAML document and validates it against the swagger JSON Schema.
     * 
     * @param content
     * @param editorInput
     *            current input
     * @return list or errors
     * @throws IOException
     * @throws ParserException
     */
    public Set<SwaggerError> validate(JsonDocument document, IFileEditorInput editorInput) {
        URI baseURI = editorInput != null ? editorInput.getFile().getLocationURI() : null;
        return validate(document, baseURI);
    }

    public Set<SwaggerError> validate(JsonDocument document, URI baseURI) {
        Set<SwaggerError> errors = Sets.newHashSet();

        JsonModel model;
        try {
            model = document.getContent();
        } catch (Exception e) {
            model = null;
        }

        if (model != null) {
            ErrorProcessor processor = new ErrorProcessor(model, document.getSchema().getRootType().getContent());

            errors.addAll(model.getErrors());
            errors.addAll(validateAgainstSchema(processor, document));
            errors.addAll(getModelValidator(model).validateModel());
            errors.addAll(referenceValidator.validate(baseURI, document));
        }

        return errors;
    }

    /**
     * Validates the YAML document against the Swagger schema
     * 
     * @param processor
     * @param document
     * @return error
     */
    protected Set<SwaggerError> validateAgainstSchema(ErrorProcessor processor, JsonDocument document) {
        return validateAgainstSchema(processor, document.getSchema().asJson(), document.asJson());
    }

    public Set<SwaggerError> validateAgainstSchema(ErrorProcessor processor, JsonNode schemaAsJson,
            JsonNode documentAsJson) {
        final Set<SwaggerError> errors = Sets.newHashSet();

        JsonSchema schema = null;
        try {
            schema = factory.getJsonSchema(schemaAsJson);
        } catch (ProcessingException e) {
            YEditLog.logException(e);
            return errors;
        }

        try {
            ProcessingReport report = schema.validate(documentAsJson, true);

            errors.addAll(processor.processReport(report));
        } catch (ProcessingException e) {
            errors.addAll(processor.processMessage(e.getProcessingMessage()));
        }

        return errors;
    }

}
