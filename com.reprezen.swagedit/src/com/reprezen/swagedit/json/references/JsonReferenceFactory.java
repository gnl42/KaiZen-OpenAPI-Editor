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
import java.net.URISyntaxException;

import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

/**
 * JSON Reference Factory 
 * 
 * This class should be used to instantiate JSONReferences.
 *
 */
public class JsonReferenceFactory {

	public JsonReference create(JsonNode node) {
		if (node == null || node.isMissingNode()) {
			return new JsonReference(null, null, false, false, node);
		}

		String text = node.isTextual() ? node.asText() :
			node.get("$ref").asText();

		return doCreate(text, node);
	}

	public JsonReference create(ScalarNode node) {
		if (node == null) {
			return new JsonReference(null, null, false, false, node);
		}

		return doCreate(node.getValue(), node);
	}

	protected JsonReference doCreate(String value, Object source) {
		String notNull = Strings.nullToEmpty(value);

		URI uri;		
		if (notNull.startsWith("#")) {
			try {
				uri = new URI(null, null, notNull.substring(1));
			} catch (URISyntaxException e) {
				return new JsonReference(null, null, false, false, source);
			}
		} else {
			try {
				uri = URI.create(notNull);
			} catch(NullPointerException | IllegalArgumentException e) {
				// invalid reference
				return new JsonReference(null, null, false, false, source);
			}
		}

		String fragment = uri.getFragment();
		JsonPointer pointer = JsonPointer.compile(Strings.emptyToNull(fragment));

		uri = uri.normalize();
		boolean absolute = uri.isAbsolute();
		boolean local = !absolute && uri.getPath().isEmpty();

		return new JsonReference(uri, pointer, absolute, local, source);
	}

}
