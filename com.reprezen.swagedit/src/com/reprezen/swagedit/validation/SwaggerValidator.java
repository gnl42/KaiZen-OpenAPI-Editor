package com.reprezen.swagedit.validation;

import com.google.common.collect.Maps;
import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory;
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator;
import com.reprezen.swagedit.core.validation.JsonSchemaValidator;
import com.reprezen.swagedit.core.validation.Validator;
import com.reprezen.swagedit.schema.SwaggerSchema;

public class SwaggerValidator extends Validator {

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
            super(schema.asJson(), Maps.newHashMap());
        }
    }
}
