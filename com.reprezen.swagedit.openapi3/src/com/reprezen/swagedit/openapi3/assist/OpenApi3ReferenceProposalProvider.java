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
package com.reprezen.swagedit.openapi3.assist;

import java.util.Arrays;

import com.reprezen.swagedit.core.assist.JsonReferenceProposalProvider;
import com.reprezen.swagedit.core.assist.contexts.ComponentContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextTypeCollection;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.openapi3.Activator;
import com.reprezen.swagedit.openapi3.assist.contexts.OperationContextType;
import com.reprezen.swagedit.openapi3.assist.contexts.OperationIdContextType;
import com.reprezen.swagedit.openapi3.assist.contexts.SecuritySchemeContextType;
import com.reprezen.swagedit.openapi3.editor.OpenApi3ContentDescriber;

public class OpenApi3ReferenceProposalProvider extends JsonReferenceProposalProvider {

    public OpenApi3ReferenceProposalProvider() {
        this(Activator.getDefault().getSchema());
    }

    public OpenApi3ReferenceProposalProvider(CompositeSchema schema) {
        super(new OpenApi3ContextTypeCollection(schema), OpenApi3ContentDescriber.CONTENT_TYPE_ID);
    }

    private static final String COMPONENT_NAME_REGEX = "[\\w\\.\\-]+";
   
    public static class OpenApi3ContextTypeCollection extends ContextTypeCollection {

        protected OpenApi3ContextTypeCollection(CompositeSchema schema) {
            super(Arrays.asList( //
                    // SCHEMA_EXAMPLE, // 
                    new ComponentContextType("components/schemas", "schemas", "schemaOrReference"), //
                    new ComponentContextType("components/parameters", "parameters", "parameterOrReference"), //
                    new ComponentContextType("components/responses", "responses", "responseOrReference"), //
                    new ComponentContextType("components/requestBodies", "requestBody", "requestBodyOrReference"), //
                    new ComponentContextType("components/links", "link", "linkOrReference"), //
                    new ComponentContextType("components/examples", "examples", "exampleOrReference"), //
                    new ComponentContextType("components/headers", "header", "headerOrReference"), //
                    new ComponentContextType("components/callbacks", "callback", "callbackOrReference"), //
                    new ComponentContextType("paths", "path items", "pathItem") {
                        
                        @Override
                        protected String getReferencePointerString() {
                            return "/definitions/pathItem/properties/$ref";
                        }
                    }, //
                    new SecuritySchemeContextType(schema, ".*/security/\\d+"), //
                    new OperationIdContextType(schema, ".*/links/" + OpenApi3ReferenceProposalProvider.COMPONENT_NAME_REGEX + "/operationId"), //
                    new OperationContextType(schema, ".*/links/" + OpenApi3ReferenceProposalProvider.COMPONENT_NAME_REGEX + "/operationRef")));
        }

    }

}
