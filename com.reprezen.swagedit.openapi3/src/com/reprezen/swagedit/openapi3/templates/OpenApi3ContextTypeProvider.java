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
package com.reprezen.swagedit.openapi3.templates;

import java.util.List;

import org.eclipse.jface.text.templates.TemplateContextType;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.templates.SchemaBasedTemplateContextType;

public class OpenApi3ContextTypeProvider {
    
    private static final String TEMPLATE_ID_PREFIX = "com.reprezen.swagedit.openapi3.templates.";
  
    public TemplateContextType getContextType(final JsonDocument doc, final String path) {
        if (OpenApi3ContextTypeProvider.RootContextType.isRoot(path)) {
            return new RootContextType();
        }
        return Iterables
                .getFirst(Iterables.filter(allContextTypes(), new Predicate<TemplateContextType>() {

                    @Override
                    public boolean apply(TemplateContextType input) {
                        if (input instanceof SchemaBasedTemplateContextType) {
                            return ((SchemaBasedTemplateContextType) input).matches(doc, path);
                        }
                        return false;
                    }

                }), null);
    }
    
    public static class RootContextType extends TemplateContextType {
        public RootContextType() {
            super(TEMPLATE_ID_PREFIX + "root", "root");
        }
        public static boolean isRoot(String normalizedPath) {
            return normalizedPath == null || normalizedPath.isEmpty() || "/".equals(normalizedPath);
        }
    }

    private SchemaBasedTemplateContextType createOpenApi3TemplateContextType(String name,
            String... pathToSchemaType) {
        return new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + name, name, pathToSchemaType);
    }

    private List<TemplateContextType> allContextTypes = Lists.newArrayList( //
            new RootContextType(), //
            createOpenApi3TemplateContextType("info.contact", "/definitions/info"), //
            createOpenApi3TemplateContextType("paths", "/definitions/paths"), //
            createOpenApi3TemplateContextType("pathItem", "/definitions/pathItem"), //
            // Components
            createOpenApi3TemplateContextType("components", "/definitions/components"),
            // Component Object Maps
            createOpenApi3TemplateContextType("schemas", "/definitions/schemasOrReferences"),
            createOpenApi3TemplateContextType("callbacks", "/definitions/callbacksOrReferences",
                    "/definitions/callbacks"),
            createOpenApi3TemplateContextType("links", "/definitions/linksOrReferences"),
            createOpenApi3TemplateContextType("parameters", "/definitions/parametersOrReferences"),
            createOpenApi3TemplateContextType("requestBodies", "/definitions/requestBodiesOrReferences"),
            createOpenApi3TemplateContextType("responses", "/definitions/responsesOrReferences",
                    "/definitions/responses"), //
            createOpenApi3TemplateContextType("securitySchemes", "/definitions/securitySchemesOrReferences"),
            createOpenApi3TemplateContextType("headers", "/definitions/headersOrReferences"),
            // Component Objects
            createOpenApi3TemplateContextType("schema", "/definitions/schema"),
            createOpenApi3TemplateContextType("callback", "/definitions/callback"),
            createOpenApi3TemplateContextType("link", "/definitions/link"),
            createOpenApi3TemplateContextType("parameter", "/definitions/parameter"),
            createOpenApi3TemplateContextType("requestBody", "/definitions/requestBody"),
            createOpenApi3TemplateContextType("response", "/definitions/response"),
            createOpenApi3TemplateContextType("securityScheme", "/definitions/securityScheme"),
            createOpenApi3TemplateContextType("header", "/definitions/header"),
            // Other
            createOpenApi3TemplateContextType("mediaTypes", "/definitions/mediaTypes"),
            createOpenApi3TemplateContextType("properties", "/definitions/schema/properties/type",
                    "/definitions/schema/properties/properties")//
    );

    public List<TemplateContextType> allContextTypes() {
        return allContextTypes;
    }
    
}
