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
package com.reprezen.swagedit.validation;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
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
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.Messages;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;
import com.reprezen.swagedit.model.ValueNode;

/**
 * This class contains methods for validating a Swagger YAML document.
 * 
 * Validation is done against the Swagger JSON Schema.
 * 
 * @see SwaggerError
 */
public class Validator {

    private final JsonReferenceValidator referenceValidator;

    public Validator(JsonReferenceValidator referenceValidator) {
        this.referenceValidator = referenceValidator;
    }

    public Validator() {
        this.referenceValidator = new JsonReferenceValidator(new JsonReferenceFactory());
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
    public Set<SwaggerError> validate(SwaggerDocument document, IFileEditorInput editorInput) {
        Set<SwaggerError> errors = Sets.newHashSet();

        JsonNode jsonContent = null;
        try {
            jsonContent = document.asJson();
        } catch (Exception e) {
            YEditLog.logException(e);
        }

        if (jsonContent != null) {
            Node yaml = document.getYaml();
            if (yaml != null) {
                URI baseURI = editorInput != null ? editorInput.getFile().getLocationURI() : null;

                errors.addAll(validateAgainstSchema(new ErrorProcessor(yaml), document));
                errors.addAll(checkDuplicateKeys(yaml));
                errors.addAll(checkMissingItemsKeyInArrayType(document.getModel()));
                errors.addAll(referenceValidator.validate(baseURI, document.getModel()));
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
    protected Set<SwaggerError> validateAgainstSchema(ErrorProcessor processor, SwaggerDocument document) {
        final JsonSchemaFactory factory = JsonSchemaFactory.newBuilder().freeze();
        final Set<SwaggerError> errors = Sets.newHashSet();

        JsonSchema schema = null;
        try {
            schema = factory.getJsonSchema(document.getSchema().asJson());
        } catch (ProcessingException e) {
            YEditLog.logException(e);
            return errors;
        }

        try {
            ProcessingReport report = schema.validate(document.asJson(), true);

            errors.addAll(processor.processReport(report));
        } catch (ProcessingException e) {
            errors.addAll(processor.processMessage(e.getProcessingMessage()));
        }

        return errors;
    }

    /**
     * Validates schema definition of type array.
     * 
     * This method traverses the node present in the document and checks that nodes that are schema definitions of type
     * array have a field items.
     * 
     * @param model
     * @return error
     */
    protected Set<SwaggerError> checkMissingItemsKeyInArrayType(Model model) {
        Set<SwaggerError> errors = new HashSet<>();

        if (model != null && model.getRoot() != null) {
            for (AbstractNode node : model.allNodes()) {
                if (hasArrayType(node)) {
                    if (node.get("items") == null) {
                        errors.add(new SwaggerError(node.getStart().getLine() + 1, IMarker.SEVERITY_ERROR,
                                Messages.error_array_missing_items));
                    }
                }
            }
        }
        return errors;
    }

    protected boolean hasArrayType(AbstractNode node) {
        if (node.isObject() && node.get("type") instanceof ValueNode) {
            ValueNode typeValue = node.get("type").asValue();
            return "array".equalsIgnoreCase(typeValue.getValue().toString());
        }
        return false;
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
