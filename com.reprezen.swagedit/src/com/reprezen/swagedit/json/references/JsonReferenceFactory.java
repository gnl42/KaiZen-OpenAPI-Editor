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

import static com.reprezen.swagedit.json.references.JsonReference.PROPERTY;

import java.net.URI;

import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.reprezen.swagedit.model.AbstractNode;
import com.reprezen.swagedit.model.Model;
import com.reprezen.swagedit.utils.URLUtils;


/**
 * JSON Reference Factory
 * 
 * This class should be used to instantiate JSONReferences.
 * 
 */
public class JsonReferenceFactory {

    public JsonReference create(AbstractNode node) {
        if (node == null) {
            return new JsonReference(null, null, false, false, false, node);
        }

        if (node.isObject() && node.get(JsonReference.PROPERTY) != null) {
            node = node.get(JsonReference.PROPERTY);
        }

        return doCreate((String) node.asValue().getValue(), node);
    }

    public JsonReference create(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return new JsonReference(null, null, false, false, false, node);
        }

        String text = node.isTextual() ? node.asText() : node.get(PROPERTY).asText();

        return doCreate(text, node);
    }

    public JsonReference create(ScalarNode node) {
        if (node == null) {
            return new JsonReference(null, null, false, false, false, node);
        }

        return doCreate(node.getValue(), node);
    }

    /**
     * Returns a simple reference if the value node points to a definition inside the same document.
     * 
     * @param baseURI
     * @param value
     * @return reference
     */
    public JsonReference createSimpleReference(URI baseURI, AbstractNode valueNode) {
        if (valueNode.isArray() || valueNode.isObject()) {
            return null;
        }

        final Object value = valueNode.asValue().getValue();
        if (!(value instanceof String)) {
            return null;
        }

        String stringValue = (String) value;
        if (Strings.emptyToNull(stringValue) == null || stringValue.startsWith("#") || stringValue.contains("/")) {
            return null;
        }

        final Model model = valueNode.getModel();
        if (model != null) {
            JsonPointer ptr = JsonPointer.compile("/definitions/" + value);
            AbstractNode target = model.find(ptr);
            if (target != null) {
                return new JsonReference.SimpleReference(baseURI, ptr, valueNode);
            }
        }

        return null;
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
