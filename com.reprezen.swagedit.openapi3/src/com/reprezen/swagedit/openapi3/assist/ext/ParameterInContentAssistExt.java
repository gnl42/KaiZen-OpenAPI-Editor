package com.reprezen.swagedit.openapi3.assist.ext;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.Proposal;
import com.reprezen.swagedit.core.assist.ext.ContentAssistExt;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ParameterInContentAssistExt implements ContentAssistExt {

    private static final String description = "The location of the parameter. Possible values are \"query\", \"header\", \"path\" or \"cookie\"";
    private static final JsonPointer pointer = JsonPointer.compile("/definitions/parameter/properties/in");

    @Override
    public boolean canProvideContentAssist(TypeDefinition type) {
        return type != null && pointer.equals(type.getPointer());
    }

    @Override
    public Collection<Proposal> getProposals(TypeDefinition type, AbstractNode node, String prefix) {
        return Lists.newArrayList( //
                new Proposal("query", "query", description, "string"),
                new Proposal("header", "header", description, "string"),
                new Proposal("path", "path", description, "string"),
                new Proposal("cookie", "cookie", description, "string"));
    }

}
