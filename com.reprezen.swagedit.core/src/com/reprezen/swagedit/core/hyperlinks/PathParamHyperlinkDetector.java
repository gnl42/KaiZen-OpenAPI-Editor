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
package com.reprezen.swagedit.core.hyperlinks;

import static com.reprezen.swagedit.core.utils.StringUtils.emptyToNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.model.AbstractNode;

/**
 * Hyperlink detector that detects links from path parameters.
 * 
 */
public class PathParamHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    public static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(\\w+)\\}");

    private final List<String> methods = Arrays.asList(//
            "get", "post", "put", "delete", "options", "head", "patch", "trace");

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().startsWith("/paths");
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        // find selected parameter
        Matcher matcher = PARAMETER_PATTERN.matcher(info.text);
        String parameter = null;
        int start = 0, end = 0;
        while (matcher.find() && parameter == null) {
            if (matcher.start() <= info.column && matcher.end() >= info.column) {
                parameter = matcher.group(1);
                start = matcher.start();
                end = matcher.end();
            }
        }

        // no parameter found
        if (emptyToNull(parameter) == null) {
            return null;
        }

        IRegion linkRegion = new Region(info.getOffset() + start, end - start);

        Map<String, JsonPointer> paths = findParameterPath(doc, pointer, parameter);
        List<IHyperlink> links = new ArrayList<>();
        for (String key : paths.keySet()) {
            IRegion target = doc.getRegion(paths.get(key));

            if (target != null) {
                links.add(new SwaggerHyperlink(parameter + " : " + key, viewer, linkRegion, target));
            }
        }

        return links.isEmpty() ? null : links.toArray(new IHyperlink[links.size()]);
    }

    /*
     * Returns a Map having for keys a method name and for value the pointer to the parameter.
     */
    private Map<String, JsonPointer> findParameterPath(JsonDocument doc, JsonPointer basePath, String parameter) {
        Map<String, JsonPointer> paths = new HashMap<>();

        AbstractNode parent = doc.getModel().find(basePath);
        if (parent == null || !parent.isObject()) {
            return paths;
        }

        for (String method : methods) {
            if (parent.get(method) == null) {
                continue;
            }

            AbstractNode parameters = parent.get(method).get("parameters");

            if (parameters != null && parameters.isArray()) {
                for (int i = 0; i < parameters.size(); i++) {
                    AbstractNode current = parameters.get(i);

                    if (JsonReference.isReference(current)) {
                        JsonPointer ptr = JsonReference.getPointer(current.asObject());
                        AbstractNode resolved = doc.getModel().find(ptr);

                        if (resolved != null && resolved.isObject() && resolved.get("name") != null) {
                            if (parameter.equals(resolved.get("name").asValue().getValue())) {
                                paths.put(method, ptr);
                            }
                        }

                    } else if (current.isObject() && current.get("name") != null) {

                        if (parameter.equals(current.get("name").asValue().getValue())) {
                            paths.put(method, JsonPointer.compile(basePath + "/" + method + "/parameters/" + i));
                        }
                    }
                }
            }
        }

        return paths;
    }

}
