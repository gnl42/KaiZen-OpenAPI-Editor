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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ValidationUtil {
    
    public static boolean isInDefinition(String pointerString) {
        return pointerString.startsWith("/definitions") || pointerString.endsWith("/schema");
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
    public static int getLine(JsonNode error, Node yamlTree) {
        String path = getInstancePointer(error);

        if (path == null || path.isEmpty())
            return 1;

        path = path.substring(1, path.length());
        String[] strings = path.split("/");

        if (yamlTree instanceof MappingNode) {
            MappingNode mn = (MappingNode) yamlTree;

            Node findNode = findNode(mn, Arrays.asList(strings));
            if (findNode != null) {
                return findNode.getStartMark().getLine() + 1;
            }
        }

        return 1;
    }

    /*
     * Returns the yaml node that matches the given path.
     * 
     * The path is given as a list of String. The Node matching the path is found by traversing the children of the node
     * pass as first parameter.
     */
    private static Node findNode(MappingNode root, List<String> paths) {
        if (paths.isEmpty())
            return root;

        String path = paths.get(0);
        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }

        final List<String> next = paths.subList(1, paths.size());
        // ~1 is use to escape /
        if (path.contains("~1")) {
            path = path.replaceAll("~1", "/");
        }

        for (NodeTuple child : root.getValue()) {
            if (child.getKeyNode() instanceof ScalarNode) {
                ScalarNode scalar = (ScalarNode) child.getKeyNode();

                if (scalar.getValue().equals(path)) {
                    return findNode(child, next);
                }
            }
        }

        return root;
    }

    private static Node findNode(NodeTuple child, List<String> paths) {
        if (child.getValueNode() instanceof MappingNode) {
            return findNode((MappingNode) child.getValueNode(), paths);
        }
        return child.getKeyNode();
    }
    
    public static JsonNode findNode(String path, JsonNode root) {
    	return findNode(Lists.newLinkedList(Arrays.asList(path.split("/"))), root);
    }
    
    private static JsonNode findNode(LinkedList<String> path, JsonNode root) {
        if (root == null) {
            return null;
        }
        // retrieves the first element, and also *removes* it
        String firstSegment = path.pop();
        if (Strings.isNullOrEmpty(firstSegment)) {
            return findNode(path, root);
        }
        int firstSegmentAsNumber = -1;
        try {
            firstSegmentAsNumber = Integer.parseInt(firstSegment);
        } catch (NumberFormatException e) {
            // ignore
        }
        JsonNode nodeForSegment = firstSegmentAsNumber == -1 ? root.get(firstSegment) : root
                .get(firstSegmentAsNumber);
        if (path.isEmpty()) {
            return nodeForSegment;
        }
        return findNode(path, nodeForSegment);
    }

}
