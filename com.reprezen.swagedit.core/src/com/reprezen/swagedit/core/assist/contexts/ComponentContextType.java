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
package com.reprezen.swagedit.core.assist.contexts;

import java.util.Collection;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.schema.ComplexTypeDefinition;
import com.reprezen.swagedit.core.schema.MultipleTypeDefinition;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ComponentContextType extends ContextType {

    private final String componentRef;

    public ComponentContextType(String value, String label, String componentSchemaPath) {
        super(value, label);
        componentRef = "#/definitions/" + componentSchemaPath;
    }

    protected String getReferencePointerString() {
        return "/definitions/reference/properties/$ref";
    }

    @Override
    public boolean canProvideProposal(JsonModel document, JsonPointer pointer) {
        if (document == null) {
            // model can be null when initTextMessages called in new JsonContentAssistProcessor()
            return false;
        }
        return isReference(document, pointer) && isReferenceToComponent(document, pointer);
    }

    protected boolean isReference(JsonModel document, JsonPointer pointer) {
        JsonNode contextNode = document.getContent().at(pointer);
        if (contextNode == null) {
            return false;
        }
        TypeDefinition type = document.getTypes().get(pointer);
        if (type instanceof MultipleTypeDefinition) {
            // MultipleTypeDefinition is a special case, it happens when several properties match a property
            for (TypeDefinition nestedType : ((MultipleTypeDefinition) type).getMultipleTypes()) {
                if (getReferencePointerString().equals(nestedType.getPointer().toString())) {
                    return true;
                }
            }
        }
        JsonPointer pointerToType = type.getPointer();
        if (pointerToType == null) {
            return false;
        }
        return getReferencePointerString().equals(pointerToType.toString());
    }

    protected boolean isReferenceToComponent(JsonModel document, JsonPointer pointer) {
        TypeDefinition parentType = document.getTypes().get(pointer.head());
        if (parentType instanceof ComplexTypeDefinition) {
            Collection<TypeDefinition> types = ((ComplexTypeDefinition) parentType).getComplexTypes();
            for (TypeDefinition type : types) {
                if (hasRefToComponent(type.getContent())) {
                    return true;
                }
            }
        }
        return hasRefToComponent(parentType.getContent());
    }

    private boolean hasRefToComponent(JsonNode content) {
        return content.hasNonNull(JsonReference.PROPERTY)
                && componentRef.equals(content.get(JsonReference.PROPERTY).asText());
    }

}
