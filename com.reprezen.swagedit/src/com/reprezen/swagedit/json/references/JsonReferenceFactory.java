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

import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.reprezen.swagedit.utils.URLUtils;

/**
 * JSON Reference Factory
 * 
 * This class should be used to instantiate JSONReferences.
 * 
 */
public class JsonReferenceFactory {

    public JsonReference create(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return new JsonReference(null, null, false, false, false, node);
        }

        String text = node.isTextual() ? node.asText() : node.get("$ref").asText();

        return doCreate(text, node);
    }

    public JsonReference create(ScalarNode node) {
        if (node == null) {
            return new JsonReference(null, null, false, false, false, node);
        }

        return doCreate(node.getValue(), node);
    }

    protected JsonReference doCreate(String value, Object source) {
        String notNull = Strings.nullToEmpty(value);

        URI uri;
        try {
            uri = URI.create(notNull);
        } catch (NullPointerException | IllegalArgumentException e) {
            // try to encode illegal characters, e.g. curly braces
            try {
                uri = URI.create(URLUtils.encodeURL(notNull));
            } catch (NullPointerException | IllegalArgumentException e2) {
                return new JsonReference(null, null, false, false, false, source);
            }
        }

        String fragment = uri.getFragment();
        JsonPointer pointer = null;
        try {
            pointer = JsonPointer.compile(Strings.emptyToNull(fragment));
        } catch (IllegalArgumentException e) {
            // let the pointer be null
        }

        uri = uri.normalize();
        boolean absolute = uri.isAbsolute();
        boolean local = !absolute && uri.getPath().isEmpty();
        // should warn when using curly braces
        boolean warnings = notNull.contains("{") || uri.toString().contains("}");

        return new JsonReference(uri, pointer, absolute, local, warnings, source);
    }

}
