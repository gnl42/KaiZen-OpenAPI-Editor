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
package com.reprezen.swagedit.openapi3.validation;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.json.references.JsonReference;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.ValueNode;

public class OpenApi3ReferenceValidator extends JsonReferenceValidator {

    private final JsonPointer schemaPointer = JsonPointer.compile("/definitions/linkOrReference");
    private final JsonPointer operationPointer = JsonPointer.compile("/definitions/operation");

    public OpenApi3ReferenceValidator() {
        super(new OpenApi3ReferenceFactory());
    }

    OpenApi3ReferenceValidator(OpenApi3ReferenceFactory factory) {
        super(factory);
    }

    @Override
    protected boolean isValidType(AbstractNode source, AbstractNode target, JsonReference reference) {
        if (schemaPointer.equals(source.getType().getPointer())) {
            return target != null && target.getType() != null
                    && Objects.equals(operationPointer, target.getType().getPointer());
        }
        return super.isValidType(source, target, reference);
    }

    public static class OpenApi3ReferenceFactory extends JsonReferenceFactory {

        private static final String OPERATION_REF = "operationRef";

        @Override
        protected Boolean isReference(AbstractNode node) {
            return super.isReference(node) || node.get(OPERATION_REF) != null;
        }

        @Override
        protected ValueNode getReferenceValue(AbstractNode node) {
            ValueNode valueNode = super.getReferenceValue(node);
            if (valueNode == null) {
                AbstractNode other = node.get(OPERATION_REF);
                if (other != null && other.isValue()) {
                    valueNode = other.asValue();
                }
            }
            return valueNode;
        }
    }
}