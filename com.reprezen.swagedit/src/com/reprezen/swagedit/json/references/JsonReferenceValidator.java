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
			if (reference.isInvalid()) {
				errors.add(createReferenceError(IMarker.SEVERITY_ERROR, reference));
			} else if (reference.isMissing(baseURI)) {
				errors.add(createReferenceError(IMarker.SEVERITY_WARNING, reference));
			}
		}
		return errors;
	}

	protected SwaggerError createReferenceError(int severity, JsonReference reference) {
		Object source = reference.getSource();
		int line;
		if (source instanceof Node) {
			line = ((Node) source).getStartMark().getLine() + 1;
		} else {
			line = 1;
		}

		return new SwaggerError(line, severity, Messages.error_invalid_reference);
	}

}
