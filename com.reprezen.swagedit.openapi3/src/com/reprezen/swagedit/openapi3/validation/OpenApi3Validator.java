/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.validation;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.core.json.JsonModel;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.validation.ModelValidator;
import com.reprezen.swagedit.core.validation.Validator;

public class OpenApi3Validator extends Validator {

    public OpenApi3Validator(Map<String, JsonNode> preloadedSchemas) {
        super(new OpenApi3ReferenceValidator(), preloadedSchemas);
    }

    OpenApi3Validator(JsonReferenceValidator referenceValidator, Map<String, JsonNode> preloadedSchemas) {
        super(referenceValidator, preloadedSchemas);
    }

    @Override
    public ModelValidator getModelValidator(JsonModel model) {
        return new OpenApi3ModelValidator(model);
    }

}
