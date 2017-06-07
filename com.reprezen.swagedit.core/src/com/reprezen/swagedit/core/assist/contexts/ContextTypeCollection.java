package com.reprezen.swagedit.core.assist.contexts;

import com.fasterxml.jackson.core.JsonPointer;

public class ContextTypeCollection {

    private final Iterable<ContextType> contextTypes;

    protected ContextTypeCollection(Iterable<ContextType> contextTypes) {
        this.contextTypes = contextTypes;
    }

    public ContextType get(JsonPointer pointer) {
        for (ContextType next : contextTypes) {
            if (next.canProvideProposal(pointer)) {
                return next;
            }
        }
        return ContextType.UNKNOWN;
    }
}