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
        AbstractJsonOverlay<?> element = Overlay.of(document.getModel()).find(pointer);

        List<Proposal> proposals = Lists.newArrayList();
        Overlay<?> overlay = Overlay.of(element);
        for (String property : overlay.getPropertyNames()) {
            if (overlay.toJson().get(property) == null) {
                proposals.add(new Proposal(property, property, "", ""));
            }
        }

        return proposals;
    }

}
