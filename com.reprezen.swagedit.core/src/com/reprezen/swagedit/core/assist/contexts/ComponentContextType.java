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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.schema.ComplexTypeDefinition;
import com.reprezen.swagedit.core.schema.MultipleTypeDefinition;
import com.reprezen.swagedit.core.schema.TypeDefinition;

public class ComponentContextType extends ContextType {

    private final ObjectNode componentRef;
    
    public ComponentContextType(String value, String label, String componentSchemaPath) {
        super(value, label, null);
        componentRef = new ObjectMapper().createObjectNode().put("$ref", "#/definitions/" + componentSchemaPath);
    }
    
    protected String getReferencePointerString() {
        return "/definitions/reference/properties/$ref";
    }

    @Override
    public boolean canProvideProposal(Model model, JsonPointer pointer) {
        if (model == null) {
            // model can be null when initTextMessages called in new JsonContentAssistProcessor()
            return false;
        }
        return isReference(model, pointer) && isReferenceToComponent(model, pointer);
    }

    protected boolean isReference(Model model, JsonPointer pointer) {
        TypeDefinition type = model.find(pointer).getType();
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

    protected boolean isReferenceToComponent(Model model, JsonPointer pointer) {
        TypeDefinition parentType = model.find(pointer.head()).getType();
        if (parentType instanceof ComplexTypeDefinition) {
            Collection<TypeDefinition> types = ((ComplexTypeDefinition) parentType).getComplexTypes();
            for (TypeDefinition type : types) {
                if (componentRef.equals(type.getContent())) {
                    return true;
                }
            }
        }
        return componentRef.equals(parentType.getContent());
    }

}
