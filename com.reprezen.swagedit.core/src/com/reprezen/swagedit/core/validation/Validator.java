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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.dadacoalition.yedit.YEditLog;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IFileEditorInput;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.reprezen.kaizen.oasparser.val.ValidationResults;
import com.reprezen.kaizen.oasparser.val.ValidationResults.ValidationItem;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.schema.TypeDefinition;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

    private final JsonReferenceValidator referenceValidator;
    private final JsonNode schemaRefTemplate = new ObjectMapper().createObjectNode() //
            .put("$ref", "#/definitions/schema");
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

    /**
     * Returns a list or errors if validation fails.
     * 
     * This method accepts as input a swagger YAML document and validates it against the swagger JSON Schema.
     * 
     * @param content
     * @param editorInput
     *            current input
     * @return list or errors
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserException
     */
    public Set<SwaggerError> validate(JsonDocument document, IFileEditorInput editorInput)
            throws MalformedURLException {
        URI baseURI = editorInput != null ? editorInput.getFile().getLocationURI() : null;
        return validate(document, baseURI.toURL());
    }

    public Set<SwaggerError> validate(JsonDocument document, URL baseURI) {
        Set<SwaggerError> errors = Sets.newHashSet();

        errors.addAll(validateAgainstSchema(document));

        ValidationResults validationResults = document.validate(baseURI);
        if (validationResults != null) {
            for (ValidationItem item : validationResults.getItems()) {
                System.out.println(item.getCrumbs());
                System.out.println(item.getMsg());
            }
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
    protected Set<SwaggerError> validateAgainstSchema(JsonDocument document) {
        JsonNode schema = document.getSchema().asJson();
        JsonNode model = document.asJson();

        System.out.println("DOC " + model);

        ErrorProcessor processor = new ErrorProcessor(document);

        return validateAgainstSchema(processor, schema, model);
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

    // /**
    // * Validates the model against with different rules that cannot be verified only by JSON schema validation.
    // *
    // * @param model
    // * @return errors
    // */
    // protected Set<SwaggerError> validateModel(JsonModel model) {
    // final Set<SwaggerError> errors = new HashSet<>();
    //
    // if (model != null) {
    // for (JsonPointer p : model.getTypes().keySet()) {
    // JsonNode node = model.getContent().at(p);
    // checkArrayTypeDefinition(errors, node);
    //
    // if (node != null && node.isObject()) {
    // if (ValidationUtil.isInDefinition(p.toString())) {
    // checkMissingType(errors, node);
    // checkMissingRequiredProperties(errors, node);
    // }
    // }
    // }
    // }
    // return errors;
    // }

    /**
     * This method checks that the node if an array type definitions includes an items field.
     * 
     * @param errors
     * @param model
     */
    protected void checkArrayTypeDefinition(Set<SwaggerError> errors, JsonNode node) {
        if (hasArrayType(node)) {
            JsonNode items = node.get("items");
            if (items == null) {
                errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_array_missing_items));
            } else {
                if (!items.isObject()) {
                    errors.add(error(items, IMarker.SEVERITY_ERROR, Messages.error_array_items_should_be_object));
                }
            }
        }
    }

    /**
     * Returns true if the node is an array type definition
     * 
     * @param node
     * @return true if array definition
     */
    protected boolean hasArrayType(JsonNode node) {
        if (node.isObject() && node.get("type") != null) {
            String typeValue = node.get("type").asText();
            return "array".equalsIgnoreCase(typeValue);
        }
        return false;
    }

    /**
     * This method checks that the node if an object definition includes a type field.
     * 
     * @param errors
     * @param node
     */
    protected void checkMissingType(Set<SwaggerError> errors, JsonNode node) {
        // object
        if (node.get("properties") != null) {
            // bypass this node, it is a property whose name is `properties`

            // if ("properties".equals(node.getProperty())) {
            // return;
            // }

            if (node.get("type") == null) {
                errors.add(error(node, IMarker.SEVERITY_WARNING, Messages.error_object_type_missing));
            } else {
                JsonNode typeValue = node.get("type");
                if (!(typeValue.isValueNode()) || !Objects.equals("object", typeValue.asText())) {
                    errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_wrong_type));
                }
            }
        } else if (isSchemaDefinition(node, null) && node.get("type") == null) {
            errors.add(error(node, IMarker.SEVERITY_WARNING, Messages.error_type_missing));
        }
    }

    private boolean isSchemaDefinition(JsonNode node, TypeDefinition type) {
        // need to use getContent() because asJson() returns resolvedValue is some subclasses
        return type != null && schemaRefTemplate.equals(type.getContent()) //
                && node.get(JsonReference.PROPERTY) == null //
                && node.get("allOf") == null;
    }

    /**
     * This method checks that the required values for the object type definition contains only valid properties.
     * 
     * @param errors
     * @param node
     */
    protected void checkMissingRequiredProperties(Set<SwaggerError> errors, JsonNode node) {
        if (node.get("required") != null && node.get("required").isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode required = (com.fasterxml.jackson.databind.node.ArrayNode) node
                    .get("required");

            JsonNode properties = node.get("properties");
            if (properties == null) {
                errors.add(error(node, IMarker.SEVERITY_ERROR, Messages.error_missing_properties));
            } else {
                for (Iterator<JsonNode> it = required.elements(); it.hasNext();) {
                    JsonNode prop = it.next();
                    if (prop.isValueNode()) {
                        String value = prop.asText();

                        if (properties.get(value) == null) {
                            errors.add(error(prop, IMarker.SEVERITY_ERROR,
                                    String.format(Messages.error_required_properties, value)));
                        }
                    }
                }
            }
        }
    }

    protected SwaggerError error(JsonNode node, int level, String message) {
        // return new SwaggerError(node.getStart().getLine() + 1, level, message);
        return new SwaggerError(1, level, message);
    }

    /*
     * Finds all duplicate keys in all objects present in the YAML document.
     */
    protected Set<SwaggerError> checkDuplicateKeys(Node document) {
        HashMultimap<Pair<Node, String>, Node> acc = HashMultimap.<Pair<Node, String>, Node> create();

        collectDuplicates(document, acc);

        Set<SwaggerError> errors = Sets.newHashSet();
        for (Pair<Node, String> key : acc.keys()) {
            Set<Node> duplicates = acc.get(key);

            if (duplicates.size() > 1) {
                for (Node duplicate : duplicates) {
                    errors.add(createDuplicateError(key.getValue(), duplicate));
                }
            }
        }

        return errors;
    }

    /*
     * This method iterates through the YAML tree to collect the pairs of Node x String representing an object and one
     * of it's keys. Each pair is associated to a Set of Nodes which contains all nodes being a key to the pair's Node
     * and having for value the pair's key. Once the iteration is done, the resulting map should be traversed. Each pair
     * having more than one element in its associated Set are duplicate keys.
     */
    protected void collectDuplicates(Node parent, Multimap<Pair<Node, String>, Node> acc) {
        switch (parent.getNodeId()) {
        case mapping: {
            for (NodeTuple value : ((MappingNode) parent).getValue()) {
                Node keyNode = value.getKeyNode();

                if (keyNode.getNodeId() == NodeId.scalar) {
                    acc.put(Pair.of(parent, ((ScalarNode) keyNode).getValue()), keyNode);
                }

                collectDuplicates(value.getValueNode(), acc);
            }
        }
            break;
        case sequence: {
            for (Node value : ((SequenceNode) parent).getValue()) {
                collectDuplicates(value, acc);
            }
        }
            break;
        default:
            break;
        }
    }

    protected SwaggerError createDuplicateError(String key, Node node) {
        return new SwaggerError(node.getStartMark().getLine() + 1, IMarker.SEVERITY_WARNING,
                String.format(Messages.error_duplicate_keys, key));
    }

}
