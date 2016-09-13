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
package com.reprezen.swagedit.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

public class JsonUtil {

    public static boolean isPointer(String ptr) {
        String sanitized = sanitize(ptr);
        if (sanitized == null) {
            return false;
        }
        return sanitized.startsWith("#");
    }

    public static JsonPointer asPointer(String ptr) {
        String sanitized = sanitize(ptr);
        if (sanitized == null) {
            return JsonPointer.compile("");
        }
        if (sanitized.startsWith("#")) {
            sanitized = sanitized.substring(1);
        }
        return JsonPointer.compile(sanitized);
    }

    public static JsonPointer getPointer(JsonNode ref) {
        String asText = ref.get("$ref").asText();
        if (asText.startsWith("#")) {
            asText = asText.substring(1);
        }

        return JsonPointer.compile(asText);
    }

    /*
     * remove quotes
     */
    protected static String sanitize(String s) {
        if (Strings.emptyToNull(s) == null) {
            return null;
        }
        return s.trim().replaceAll("'|\"", "");
    }

}
