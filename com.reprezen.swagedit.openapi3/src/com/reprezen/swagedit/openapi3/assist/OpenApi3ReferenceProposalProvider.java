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

import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.assist.JsonReferenceProposalProvider;
import com.reprezen.swagedit.core.assist.contexts.ComponentContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextType;
import com.reprezen.swagedit.core.assist.contexts.ContextTypeCollection;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.openapi3.Activator;
import com.reprezen.swagedit.openapi3.assist.contexts.OperationContextType;
import com.reprezen.swagedit.openapi3.assist.contexts.OperationIdContextType;
import com.reprezen.swagedit.openapi3.assist.contexts.SecuritySchemeContextType;
import com.reprezen.swagedit.openapi3.editor.OpenApi3ContentDescriber;

public class OpenApi3ReferenceProposalProvider extends JsonReferenceProposalProvider {

    public OpenApi3ReferenceProposalProvider() {
        super(new OpenApi3ContextTypeCollection(Activator.getDefault().getSchema()), OpenApi3ContentDescriber.CONTENT_TYPE_ID);
    }

    protected static final String COMPONENT_NAME_REGEX = "[\\w\\.\\-]+";

    protected static final String SCHEMA_COMPONENT_REGEX = "^/components/schemas/(\\w+/)+";
    protected static final String INLINE_SCHEMA_REGEX = ".*schema/(\\w+/)*";
    protected static final String PATH_ITEM_REGEX = "/paths/~1[^/]+/\\$ref"// in paths object
            // e.g. "/components/callbacks/myWebhook/$request.body#~1url/$ref"
            + "|/components/callbacks/" + COMPONENT_NAME_REGEX + "/[^/]+/\\$ref"; // in callbacks
    protected static final String LINK_OPERATIONID_REGEX = ".*/links/" + COMPONENT_NAME_REGEX + "/operationId";
    protected static final String LINK_OPERATIONREF_REGEX = ".*/links/" + COMPONENT_NAME_REGEX + "/operationRef";
    protected static final String SCHEMA_EXAMPLE_REGEX = SCHEMA_COMPONENT_REGEX + "example/\\$ref" + "|"
            + INLINE_SCHEMA_REGEX + "example/\\$ref";
    protected static final String CALLBACK_REGEX = ".*/callbacks/" + COMPONENT_NAME_REGEX + "/\\$ref";
    protected static final String SECURITY_REGEX = ".*/security/\\d+";

    public static final ContextType PATH_ITEM = new ContextType("paths", "path items", PATH_ITEM_REGEX);
    public static final ContextType CALLBACK = new ContextType("components/callbacks", "callback", CALLBACK_REGEX);
    public static final ContextType PATH_LINK_OPERATION_ID = new ContextType("components/links/", "operationId",
            CALLBACK_REGEX);

    public static class OpenApi3ContextTypeCollection extends ContextTypeCollection {

        protected OpenApi3ContextTypeCollection(CompositeSchema schema) {
            super(Lists.newArrayList( //
                    // SCHEMA_EXAMPLE, // should go before schema definition
                    new ComponentContextType("components/schemas", "schemas", "schemaOrReference"), //
                    new ComponentContextType("components/parameters", "parameters", "parameterOrReference"), //
                    new ComponentContextType("components/responses", "responses", "responseOrReference"), //
                    new ComponentContextType("components/requestBodies", "requestBody", "requestBodyOrReference"), //
                    new ComponentContextType("components/links", "link", "linkOrReference"), //
                    new ComponentContextType("components/examples", "examples", "exampleOrReference"), //
                    new ComponentContextType("components/headers", "header", "headerOrReference"), //
                    CALLBACK, //
                    PATH_ITEM, //
                    new SecuritySchemeContextType(schema, OpenApi3ReferenceProposalProvider.SECURITY_REGEX), //
                    new OperationIdContextType(schema, OpenApi3ReferenceProposalProvider.LINK_OPERATIONID_REGEX), //
                    new OperationContextType(schema, OpenApi3ReferenceProposalProvider.LINK_OPERATIONREF_REGEX)));
        }

    }

}
