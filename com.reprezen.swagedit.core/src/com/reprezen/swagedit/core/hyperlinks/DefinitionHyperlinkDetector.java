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

import static com.google.common.base.Strings.emptyToNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.editor.JsonDocument;

/**
 * Hyperlink detector that detects links to and inside schema definition elements.
 * 
 */
public class DefinitionHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    public static final String TAGS_PATTERN = "^[/\\W+|\\w+]*/tags([/\\W+|\\w+]+)";
    public static final String REQUIRED_PATTERN = "^([/\\W+|\\w+]+)(/required[/\\W+|\\w+]+)";

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null
                && (pointer.toString().matches(REQUIRED_PATTERN) || pointer.toString().matches(TAGS_PATTERN));
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        JsonPointer targetPath;
        if (pointer.toString().matches(REQUIRED_PATTERN)) {
            targetPath = getRequiredPropertyPath(doc, info, pointer);
        } else {
            targetPath = getTagDefinitionPath(doc, info, pointer);
        }

        if (targetPath == null) {
            return null;
        }


        IRegion target = doc.getRegion(targetPath);
        if (target == null) {
            return null;
        }

        return new IHyperlink[] { new SwaggerHyperlink(info.text, viewer, info.region, target) };
    }

    protected JsonPointer getRequiredPropertyPath(JsonDocument doc, HyperlinkInfo info, JsonPointer pointer) {
        Matcher matcher = Pattern.compile(REQUIRED_PATTERN).matcher(pointer.toString());
        String containerPath = null;
        if (matcher.find()) {
            containerPath = matcher.group(1);
        }

        if (emptyToNull(containerPath) == null) {
            return null;
        }

        JsonNode container = doc.asJson().at(JsonPointer.compile(containerPath));
        if (container.get("properties") != null && container.get("properties").get(info.text) != null) {
            return JsonPointer.compile(containerPath)
                    .append(JsonPointer.compile("/" + container.get("properties").get(info.text).asText()));
        } else {
            return null;
        }
    }

    protected JsonPointer getTagDefinitionPath(JsonDocument doc, HyperlinkInfo info, JsonPointer pointer) {
        JsonPointer ptr = JsonPointer.compile("/definitions/" + info.text);
        JsonNode node = doc.asJson().at(ptr);

        return node != null ? ptr : null;
    }

}
