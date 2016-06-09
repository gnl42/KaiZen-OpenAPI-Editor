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

import com.fasterxml.jackson.databind.JsonNode;

public class JsonReferenceResolver {

	private final JsonReferenceFactory factory;
	private final JsonReferenceCollector collector;

	public JsonReferenceResolver() {
		this.factory = new JsonReferenceFactory();
		this.collector = new JsonReferenceCollector(factory);
	}

	/**
	 * Resolves all references inside the JSON document.
	 * 
	 * @param document
	 * @return document without reference nodes
	 */
	public JsonNode resolve(URI baseURI, JsonNode document) {
		Iterable<JsonReference> references = collector.collect(document);

		for (JsonReference reference: references) {
			JsonNode resolved = reference.resolve(baseURI);
			if (resolved != null && !resolved.isMissingNode()) {
				// TODO
				// replace node in document
			}
		}

		return document;
	}

}
