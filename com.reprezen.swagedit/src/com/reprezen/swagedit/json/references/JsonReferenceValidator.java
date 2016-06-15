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
package com.reprezen.swagedit.json.references;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.yaml.snakeyaml.nodes.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.reprezen.swagedit.Messages;
import com.reprezen.swagedit.validation.SwaggerError;

/**
 * JSON Reference Validator
 */
public class JsonReferenceValidator {

	private final JsonReferenceCollector collector;

	public JsonReferenceValidator(JsonReferenceFactory factory) {
		this.collector = new JsonReferenceCollector(factory);
	}

	/**
	 * Returns a collection containing all errors being invalid JSON references 
	 * present in the JSON document.
	 * 
	 * @param baseURI
	 * @param document
	 * @return collection of errors
	 */
	public Collection<? extends SwaggerError> validate(URI baseURI, JsonNode document) {
		return doValidate(baseURI, collector.collect(document));
	}

	/**
	 * Returns a collection containing all errors being invalid JSON references 
	 * present in the YAML document. 
	 * 
	 * @param baseURI
	 * @param document
	 * @return collection of errors
	 */
	public Collection<? extends SwaggerError> validate(URI baseURI, Node document) {
		return doValidate(baseURI, collector.collect(document));
	}

	protected Collection<? extends SwaggerError> doValidate(URI baseURI, Iterable<JsonReference> references) {
		Set<SwaggerError> errors = Sets.newHashSet();
		for (JsonReference reference: references) {
			if (!reference.isValid(baseURI)) {
				Object source = reference.getSource();

				if (source instanceof Node) {
					errors.add(createReferenceError((Node) source));
				} else {
					errors.add(createReferenceError());
				}
			}
		}
		return errors;
	}

	protected SwaggerError createReferenceError(Node node) {
		int line = node.getStartMark().getLine() + 1;

		return new SwaggerError(line, IMarker.SEVERITY_WARNING, Messages.error_invalid_reference);
	}

	protected SwaggerError createReferenceError() {
		return new SwaggerError(1, IMarker.SEVERITY_WARNING, Messages.error_invalid_reference);
	}

}
