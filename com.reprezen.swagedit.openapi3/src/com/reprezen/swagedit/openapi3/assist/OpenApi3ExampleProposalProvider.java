/*******************************************************************************
 * Copyright (c) 2019 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.assist;

import static com.reprezen.swagedit.core.json.references.JsonReference.isReference;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.reprezen.swagedit.core.assist.JsonReferenceProposalProvider;
import com.reprezen.swagedit.core.assist.ProposalDescriptor;
import com.reprezen.swagedit.core.assist.contexts.ContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextTypeCollection;
import com.reprezen.swagedit.core.assist.contexts.RegexContextType;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.utils.DocumentUtils;
import com.reprezen.swagedit.core.utils.ExtensionUtils;
import com.reprezen.swagedit.core.utils.ModelUtils;
import com.reprezen.swagedit.core.utils.SwaggerFileFinder.Scope;
import com.reprezen.swagedit.openapi3.ExampleDataProvider;
import com.reprezen.swagedit.openapi3.editor.OpenApi3ContentDescriber;

public class OpenApi3ExampleProposalProvider extends JsonReferenceProposalProvider {

	private static final JsonReferenceFactory REFERENCE_FACTORY = new JsonReferenceFactory();

	private static final String EXTENSION_POINT_NAME = "com.reprezen.swagedit.openapi3.exampleprovider";

	private static final String EXTENSION_PROPERTY_NAME = "class";

	private static final String SCHEMA_FIELD_NAME = "schema";

	private static final String REFERENCE_KEY = "$ref";

	private static final JsonPointer COMPONENTS_NODE_POINTER = JsonPointer.compile("/definitions/components");

	private static enum PoiterRegEx {

		REQUEST_BODY_EXAMPLE_POINTER_REGEX(".*/content(/\\S+)/example$"),
		REQUEST_BODY_EXAMPLES_EXAMPLE_VALUE_FIELD_POINTER_REGEX(".*/content(/\\S+)/examples(/\\S+)/value$"),
		RESPONSE_BODY_EXAMPLE_POINTER_REGEX(".*/content(/\\S+)/example$"),
		RESPONSE_BODY_EXAMPLES_EXAMPLE_VALUE_FIELD_POINTER_REGEX(".*/content(/\\S+)/examples(/\\S+)/value$"),
		PARAMETER_EXAMPLE_POINTER_REGEX(".*/parameters(/\\S+)/example$"),
		PARAMETER_EXAMPLES_EXAMPLE_VALUE_FIELD_POINTER_REGEX(".*/parameters(/\\S+)/examples(/\\S+)/value$"),
		MODEL_EXAMPLE_POINTER_REGEX("/content(/\\S+)/schema/example$"),
		HEADERS_EXAMPLE_POINTER_REGEX(".*/headers(/\\S+)/example$"),
		HEADERS_EXAMPLES_POINTER_REGEX(".*/headers(/\\S+)/examples(/\\S+)/value$"),
		COMPONENTS_SCHEMA_EXAMPLE_POINTER_REGEX(".*/components/schemas/\\S+/example$");

		private final String regEx;

		private PoiterRegEx(String regEx) {
			this.regEx = regEx;
		}

		public String getRegEx() {
			return regEx;
		}

	}

	private static final ContextTypeCollection EXAMPLE_CONTEXT_TYPES;

	static {
		final List<ContextType> contextTypes = Arrays.asList(PoiterRegEx.values()).stream().map(//
				poiterRegEx -> new RegexContextType("Generate Example", "Example", poiterRegEx.getRegEx())//
		).collect(Collectors.toList());
		EXAMPLE_CONTEXT_TYPES = ContextType.newContentTypeCollection(contextTypes);
	}

	public OpenApi3ExampleProposalProvider() {
		super(EXAMPLE_CONTEXT_TYPES, OpenApi3ContentDescriber.CONTENT_TYPE_ID);
	}

	@Override
	public Collection<ProposalDescriptor> getProposals(JsonPointer pointer, JsonDocument document, Scope scope) {
		final AbstractNode nodeAtPointer = document.getModel().find(pointer);

		JsonNode normalized = null;
		if (insideComponentsNode(nodeAtPointer)) {
			final AbstractNode schemaNode = nodeAtPointer.getParent();
			normalized = normalize(schemaNode, document);
		} else {
			final Optional<AbstractNode> parentNode = ModelUtils.findParentContainingField(//
					nodeAtPointer, SCHEMA_FIELD_NAME);
			if (parentNode.isPresent()) {
				final AbstractNode schemaNode = parentNode.get().get(SCHEMA_FIELD_NAME);
				normalized = normalize(schemaNode, document);
			} else {
				normalized = NullNode.instance; // schema filed not found in parent
			}
		}

		final ExampleDataProvider exampleDataProvider = (ExampleDataProvider) getExampleDataProvider();
		final String exampleData = exampleDataProvider.getData(normalized);
		return Arrays.asList(new ProposalDescriptor("Generate Example:").replacementString(exampleData).type("string"));

	}

	private boolean isSchemaReference(JsonNode jsonNode) {
		return jsonNode.get(REFERENCE_KEY) != null;
	}

	private ExampleDataProvider getExampleDataProvider() {
		final ExampleDataProvider defaultDataProvider = (JsonNode jsonNode) -> "example:";

		try {
			final ExampleDataProvider exampleDataProvider = (ExampleDataProvider) ExtensionUtils
					.createExecutableExtension(EXTENSION_POINT_NAME, EXTENSION_PROPERTY_NAME);
			return exampleDataProvider != null ? exampleDataProvider : defaultDataProvider;
		} catch (CoreException e) {
			return defaultDataProvider;
		}
	}

	private boolean insideComponentsNode(AbstractNode node) {
		return ModelUtils.findParent(node, COMPONENTS_NODE_POINTER).isPresent();
	}

	private JsonNode normalize(AbstractNode node, JsonDocument document) {
		final URI filePath = DocumentUtils.getActiveEditorInputURI();
		final JsonNode schemaNode = document.asJson().at(node.getPointer());
		if (isSchemaReference(schemaNode)) {
			final JsonReference schemaReference = REFERENCE_FACTORY.create(schemaNode);
			final JsonNode resolve = schemaReference.resolve(document, filePath);
			normalize(resolve, document, filePath);
			return resolve;
		} else {
			normalize(schemaNode, document, filePath);
			return schemaNode;
		}
	}

	private void normalize(JsonNode node, JsonDocument document, URI baseURI) {
		node.fields().forEachRemaining(entry -> {
			JsonNode value = entry.getValue();
			if (isReference(value)) {
				final JsonReference reference = REFERENCE_FACTORY.create(value);
				value = reference.resolve(document, baseURI);

				if (value != null && !value.isMissingNode()) {
					((com.fasterxml.jackson.databind.node.ObjectNode) node).set(entry.getKey(), value);
				}
			}
			normalize(value, document, baseURI);
		});
	}

}
