package com.reprezen.swagedit.core.assist;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.jsonoverlay.AbstractJsonOverlay;
import com.reprezen.jsonoverlay.Overlay;
import com.reprezen.swagedit.core.editor.JsonDocument;

public class OpenApiProposalProvider {

    public Collection<Proposal> getProposals(JsonPointer pointer, JsonDocument document, String prefix) {
        AbstractJsonOverlay<?> element = document.findElement(pointer);

        List<Proposal> proposals = Lists.newArrayList();
        for (String property : Overlay.of(element).getPropertyNames()) {
            proposals.add(new Proposal(property, property, "", ""));
        }

        return proposals;
    }

}
