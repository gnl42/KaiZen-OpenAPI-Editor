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
package com.reprezen.swagedit.core.validation;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.RangeNode;

public class ValidationUtil {

    public static boolean isInDefinition(String pointerString) {
        return isInOpenApi3Definition(pointerString) //
                || isInSwagger2Definition(pointerString);
    }

    private static boolean isInSwagger2Definition(String pointerString) {
        return pointerString.startsWith("/definitions") //
                || pointerString.endsWith("/schema"); //
    }

    private static boolean isInOpenApi3Definition(String pointerString) {
        return pointerString.startsWith("/components/schemas"); // OAS v3
    }

    public static String getInstancePointer(JsonNode error) {
        if (!error.has("instance") || !error.get("instance").has("pointer"))
            return null;
        return error.get("instance").get("pointer").asText();
    }

    /*
     * Returns the line for which an error message has been produced.
     * 
     * The error message is a JSON object that contains the path to the invalid node. The path is accessible via
     * instance.pointer. The path is in the forms: - /{id} - /{id}/~{nb} - /{id}/~{id2}
     * 
     * The line number is computed by after parsing the yaml content with the yaml parser. This latter returns a tree of
     * Node, each corresponding to a yaml construct and including a position.
     * 
     * The Node matching the path is found by the methods findNode().
     */
    public static int getLine(JsonNode error, JsonModel model) {
        String path = getInstancePointer(error);

        if (path == null || path.isEmpty())
            return 1;

        RangeNode node = model.getRanges().get(JsonPointer.compile(path));

        int line = 1;
        if (node != null) {
            if (node.getFieldLocation() != null) {
                line = node.getFieldLocation().startLine;
            } else {
                line = node.getContentLocation().startLine;
            }
        }

        return line;
    }

}
