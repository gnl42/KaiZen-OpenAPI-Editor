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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.hyperlinks.AbstractJsonHyperlinkDetector;

public class LinkOperationHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().matches(".*/links/(\\w+)/operationId");
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        JsonNode node = doc.asJson().at(pointer);
        // List<JsonNode> nodes = doc.getContent().findByType(JsonPointer.compile("/definitions/operation"));
        List<JsonNode> nodes = Lists.newArrayList();
        Iterator<JsonNode> it = nodes.iterator();

        JsonNode found = null;
        while (it.hasNext() && found == null) {
            JsonNode current = it.next();
            JsonNode value = current.get("operationId");

            if (value != null && Objects.equals(node.asText(), value.asText())) {
                found = value;
            }
        }

        if (found != null) {
            // IRegion target = doc.getRegion(found.getPointer());
            // if (target != null) {
            // return new IHyperlink[] { new SwaggerHyperlink(info.text, viewer, info.region, target) };
            // }
        }

        return null;
    }

}
