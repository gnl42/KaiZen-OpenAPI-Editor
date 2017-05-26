package com.reprezen.swagedit.core.assist.contexts;

import com.google.common.base.Strings;

public class ContextTypeCollection {

    private final Iterable<ContextType> contextTypes;

    protected ContextTypeCollection(Iterable<ContextType> contextTypes) {
        this.contextTypes = contextTypes;
    }

    public ContextType get(String path) {
        if (Strings.emptyToNull(path) == null) {
            return ContextType.UNKNOWN;
        }
        for (ContextType next : contextTypes) {
            if (path.matches(next.regex)) {
                return next;
            }
        }
        return ContextType.UNKNOWN;
    }
}