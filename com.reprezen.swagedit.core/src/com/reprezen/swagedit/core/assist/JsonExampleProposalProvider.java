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
package com.reprezen.swagedit.core.assist;

import static com.reprezen.swagedit.core.json.references.JsonReference.isReference;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.assist.contexts.ContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextTypeCollection;
import com.reprezen.swagedit.core.assist.exampleprovider.ExampleProvider;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.utils.DocumentUtils;
import com.reprezen.swagedit.core.utils.ExtensionUtils;
import com.reprezen.swagedit.core.utils.SwaggerFileFinder.Scope;

/**
 * Completion proposal provider for JSON examples.
 */
public class JsonExampleProposalProvider {

	private static final JsonReferenceFactory REFERENCE_FACTORY = new JsonReferenceFactory();
	protected static final String EXTENSION_POINT_NAME = "com.reprezen.swagedit.core.exampleprovider";
	protected static final String EXTENSION_PROPERTY_NAME = "class";
	protected static final String SCHEMA_FIELD_NAME = "schema";
	private static final String REFERENCE_KEY = "$ref";
	private final ContextTypeCollection contextTypes;

	public JsonExampleProposalProvider(ContextTypeCollection contextTypes) {
		this.contextTypes = contextTypes;
	}

	public boolean canProvideProposal(Model model, JsonPointer pointer) {
		return pointer != null && contextTypes.get(model, pointer) != ContextType.UNKNOWN;
	}

	public Collection<ProposalDescriptor> getProposals(JsonPointer pointer, JsonDocument document,
			Scope scope) {
		return Collections.emptyList();
	}

	private boolean isSchemaReference(JsonNode jsonNode) {
		return jsonNode.has(REFERENCE_KEY);
	}

	protected JsonNode normalize(AbstractNode node, JsonDocument document) {
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

	protected ExampleProvider getExampleDataProvider() {
		final ExampleProvider defaultDataProvider = (JsonNode jsonNode) -> "example:";
	
		try {
			final ExampleProvider exampleDataProvider = (ExampleProvider) ExtensionUtils
					.createExecutableExtension(EXTENSION_POINT_NAME, EXTENSION_PROPERTY_NAME);
			return exampleDataProvider != null ? exampleDataProvider : defaultDataProvider;
		} catch (CoreException e) {
			return defaultDataProvider;
		}
	}

	protected String getProposal(JsonNode normalized) {
		final ExampleProvider exampleDataProvider = (ExampleProvider) getExampleDataProvider();
		final String exampleData = exampleDataProvider.getData(normalized);
		return exampleData;
	}

}
