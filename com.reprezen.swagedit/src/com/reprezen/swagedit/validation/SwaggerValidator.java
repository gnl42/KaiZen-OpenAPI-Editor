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
package com.reprezen.swagedit.validation;

import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.validation.JsonSchemaValidator;
import com.reprezen.swagedit.core.validation.Validator;
import com.reprezen.swagedit.schema.SwaggerSchema;

public class SwaggerValidator extends Validator {

    public SwaggerValidator(IPreferenceStore preferenceStore) {
        super(preferenceStore);
    }

    private JsonReferenceValidator referenceValidator;
    private JsonSchemaValidator schemaValidator;

    @Override
    public JsonReferenceValidator getReferenceValidator() {
        if (referenceValidator == null) {
            referenceValidator = new JsonReferenceValidator(getSchemaValidator(), new JsonReferenceFactory());
        }
        return referenceValidator;
    }

    @Override
    public JsonSchemaValidator getSchemaValidator() {
        if (schemaValidator == null) {
            schemaValidator = new SwaggerSchemaValidator();
        }
        return schemaValidator;
    }

    public static class SwaggerSchemaValidator extends JsonSchemaValidator {

        public static final SwaggerSchema schema = Activator.getDefault() != null ? //
                Activator.getDefault().getSchema() : new SwaggerSchema();

        public SwaggerSchemaValidator() {
            super(schema.asJson(), new HashMap<>());
        }
    }
}
