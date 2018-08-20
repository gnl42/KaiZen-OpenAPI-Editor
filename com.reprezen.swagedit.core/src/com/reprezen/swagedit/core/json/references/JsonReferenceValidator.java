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
package com.reprezen.swagedit.core.json.references;

import static com.reprezen.swagedit.core.validation.Messages.error_invalid_reference;
import static com.reprezen.swagedit.core.validation.Messages.error_invalid_reference_type;
import static com.reprezen.swagedit.core.validation.Messages.error_missing_reference;
import static com.reprezen.swagedit.core.validation.Messages.warning_simple_reference;
import static org.eclipse.core.resources.IMarker.SEVERITY_ERROR;
import static org.eclipse.core.resources.IMarker.SEVERITY_WARNING;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.core.Activator;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Location;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.validation.SwaggerError;

/**
 * JSON Reference Validator
 */
public class JsonReferenceValidator {

    private final JsonReferenceCollector collector;
    private final JsonReferenceFactory referenceFactory;

    protected JsonSchemaFactory factory = null;

    public JsonReferenceValidator(JsonReferenceFactory factory) {
        this.referenceFactory = factory;
        this.collector = new JsonReferenceCollector(factory);
    }

    public void setFactory(JsonSchemaFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns a collection containing all errors being invalid JSON references present in the Swagger document.
     * 
     * @param baseURI
     * @param document
     * @return collection of errors
     */
    public Collection<? extends SwaggerError> validate(URI baseURI, JsonDocument doc, Model model) {
        return doValidate(baseURI, doc, collector.collect(baseURI, model));
    }

    /*
     * Runs various validation on a set of references. The set of references is given to us by the reference collector
     * in the form of a Map. The keys being the references and the values are a list of nodes that are sources of the
     * reference.
     * 
     * Having a set of reference that does not contain duplicates allow us to reduce the time validation takes. If a
     * validation fails, then an error is added to each of the reference sources.
     */
    protected Collection<? extends SwaggerError> doValidate(URI baseURI, JsonDocument doc,
            Map<JsonReference, List<AbstractNode>> references) {

        Set<SwaggerError> errors = Sets.newHashSet();
        // Cache of JSON schemas (subsets of the main schema). The key identifies a schema by it's pointer
        // in the main schema.
        Map<String, JsonSchema> schemas = Maps.newHashMap();

        for (JsonReference reference : references.keySet()) {
            if (reference instanceof JsonReference.SimpleReference) {
                errors.addAll(
                        createReferenceError(SEVERITY_WARNING, warning_simple_reference, references.get(reference)));
            } else if (reference.isInvalid()) {
                errors.addAll(createReferenceError(SEVERITY_ERROR, error_invalid_reference, references.get(reference)));
            } else if (reference.isMissing(doc, baseURI)) {
                errors.addAll(
                        createReferenceError(SEVERITY_WARNING, error_missing_reference, references.get(reference)));
            } else if (reference.containsWarning()) {
                errors.addAll(
                        createReferenceError(SEVERITY_WARNING, error_invalid_reference, references.get(reference)));
            } else {
                errors.addAll(validateType(doc, baseURI, reference, references.get(reference), schemas));
            }
        }

        return errors;
    }

    /**
     * This method checks that referenced objects are of expected type as defined in the schema.
     * 
     * @param doc
     *            current document
     * @param baseURI
     *            document base URI
     * @param reference
     *            actual reference
     * @param sources
     *            list of node being source of the reference
     * @param schemas
     *            cache of JSON schemas
     * @param errors
     *            current set of errors
     */
    protected Set<SwaggerError> validateType(JsonDocument doc, URI baseURI, JsonReference reference,
            Collection<AbstractNode> sources, Map<String, JsonSchema> schemas) {

        Set<SwaggerError> errors = Sets.newHashSet();

        JsonNode target = findTarget(doc, baseURI, reference);
        // To avoid performing even more cycles, the sources are grouped by their type.
        // Validation is done only once for each sources having same type.
        Map<String, List<AbstractNode>> sourceTypes = groupSourcesByType(sources);

        for (String type : sourceTypes.keySet()) {
            JsonSchema jsonSchema = getSchema(doc, type, schemas);
            errors.addAll(validate(jsonSchema, target, error_invalid_reference_type, sourceTypes.get(type)));
        }

        return errors;
    }

    protected JsonSchema getSchema(JsonDocument doc, String type, Map<String, JsonSchema> schemas) {
        JsonSchema schema = schemas.get(type);
        if (schema == null) {
            try {
                schema = factory.getJsonSchema(doc.getSchema().asJson(), type);
                schemas.put(type, schema);
            } catch (ProcessingException e) {
                Activator.getDefault().logError(e.getLocalizedMessage(), e);
            }
        }
        return schema;
    }

    /*
     * Executes JSON schema validation against given target. If the target fails to be recognize as an instance of the
     * actual schema, an error is returned with the given message on all sources.
     * 
     */
    protected Set<SwaggerError> validate(JsonSchema schema, JsonNode target, String message,
            Collection<AbstractNode> sources) {

        Set<SwaggerError> errors = Sets.newHashSet();
        try {
            ProcessingReport report = schema.validate(target);
            if (!report.isSuccess()) {
                errors = createReferenceError(SEVERITY_WARNING, message, sources);
            }
        } catch (ProcessingException e) {
            Activator.getDefault().logError(e.getLocalizedMessage(), e);
        }
        return errors;
    }

    /*
     * Groups all source nodes by their JSON type.
     */
    protected Map<String, List<AbstractNode>> groupSourcesByType(Collection<AbstractNode> sources) {
        Map<String, List<AbstractNode>> result = Maps.newHashMap();
        for (AbstractNode source : sources) {
            if (source.getType() != null && source.getType().getPointer() != null) {
                String type = source.getType().getPointer().toString();

                if (result.containsKey(type)) {
                    result.get(type).add(source);
                } else {
                    result.put(type, Lists.newArrayList(source));
                }
            }
        }

        return result;
    }

    protected JsonNode findTarget(JsonDocument doc, URI baseURI, JsonReference reference) {
        JsonNode valueNode = null;

        if (!reference.getUri().equals(baseURI)) {
            // Try to load the referenced node from an external document
            JsonNode externalDoc = reference.getDocument(doc, baseURI);

            if (externalDoc != null) {
                try {
                    valueNode = externalDoc.at(reference.getPointer());
                } catch (Exception e) {
                    // fail to parse the model or the pointer
                }
            }
        } else {
            valueNode = doc.asJson().at(reference.getPointer());
        }

        return valueNode;
    }

    protected Set<SwaggerError> createReferenceError(int severity, String message, Collection<AbstractNode> sources) {
        Set<SwaggerError> errors = Sets.newHashSet();
        for (AbstractNode source : sources) {
            errors.add(createReferenceError(severity, message, source));
        }
        return errors;
    }

    protected SwaggerError createReferenceError(int severity, String message, AbstractNode source) {
        int line = 1;
        if (source != null) {
            AbstractNode ref = referenceFactory.getReferenceValue(source);
            if (ref != null) {
                Location location = ref != null ? ref.getStart() : source.getStart();
                line = location.getLine() + 1;
            }
        }

        return new SwaggerError(line, severity, message);
    }

}
