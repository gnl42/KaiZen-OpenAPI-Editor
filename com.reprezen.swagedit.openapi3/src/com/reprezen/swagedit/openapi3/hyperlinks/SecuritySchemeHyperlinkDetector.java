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
package com.reprezen.swagedit.openapi3.hyperlinks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.hyperlinks.AbstractJsonHyperlinkDetector;

public class SecuritySchemeHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    protected static final String REGEX = ".*/security/\\d+/(\\w+)";
    protected static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().matches(REGEX);
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        Matcher matcher = PATTERN.matcher(pointer.toString());
        String link = matcher.find() ? matcher.group(1) : null;

        if (link != null) {
            JsonNode securityScheme = doc.asJson().at("/components/securitySchemes/" + link);

            if (securityScheme != null) {
                // IRegion target = doc.getRegion(securityScheme.getPointer());
                // if (target != null) {
                // return new IHyperlink[] { new SwaggerHyperlink(info.text, viewer, info.region, target) };
                // }
            }
        }
        return null;
    }

}
