package com.reprezen.swagedit.openapi3.assist.ext;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class CallbacksContentAssistExt implements ContentAssistExt {

    private final JsonPointer pointer = JsonPointer.compile("/definitions/callbacks");

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        return Lists.newArrayList( //
                new Proposal("_key_:", "_key_", null, "callbackOrReference"), //
                new Proposal("x-:", "x-", null, "specificationExtension"));
    }

}
