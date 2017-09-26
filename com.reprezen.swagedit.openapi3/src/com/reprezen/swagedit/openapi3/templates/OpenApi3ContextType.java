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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.reprezen.swagedit.core.model.Model;
import com.reprezen.swagedit.core.templates.SchemaBasedTemplateContextType;

public class OpenApi3ContextType {
    
    private static final String TEMPLATE_ID_PREFIX = "com.reprezen.swagedit.openapi3.templates.";
  
    public static class RootContextType extends TemplateContextType {
        public RootContextType() {
            super(TEMPLATE_ID_PREFIX + "root", "root");
        }
        protected static boolean isRoot(String normalizedPath) {
            return normalizedPath == null || normalizedPath.isEmpty() || "/".equals(normalizedPath);
        }
    }

    private static List<TemplateContextType> allContextTypes = Lists.<TemplateContextType>newArrayList( //
            new RootContextType(), //
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "info.contact", "info.contact", "/definitions/info"), //
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "paths", "paths", "/definitions/paths"), //
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "path_item", "pathItem", "/definitions/pathItem"), //
            // Components
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "components", "components", "/definitions/components"),
            // Component Object Maps
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "schemas", "schemas", "/definitions/schemasOrReferences"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "callbacks", "callbacks", "/definitions/callbacksOrReferences", "/definitions/callbacks"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "links", "links", "/definitions/linksOrReferences"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "parameters", "parameters", "/definitions/parametersOrReferences"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "requestBodies", "requestBodies", "/definitions/requestBodiesOrReferences"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "responses", "responses", "/definitions/responsesOrReferences", "/definitions/responses"), // 
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "securitySchemes", "securitySchemes", "/definitions/securitySchemesOrReferences"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "headers", "headers", "/definitions/headersOrReferences"),
            // Component Objects
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "schema", "schema", "/definitions/schema"), 
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "callback", "callback", "/definitions/callback"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "link", "link", "/definitions/link"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "parameter", "parameter", "/definitions/parameter"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "requestBody", "requestBody", "/definitions/requestBody"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "response", "response", "/definitions/response"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "securityScheme", "securityScheme", "/definitions/securityScheme"),
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "header", "header", "/definitions/header"),
            // Other
            new SchemaBasedTemplateContextType(TEMPLATE_ID_PREFIX + "mediaTypes", "mediaTypes", "/definitions/mediaTypes")
            );

    public static List<TemplateContextType> allContextTypes() {
        return allContextTypes;
    }
    
    public static List<String> allContextTypeIds() {
        return Lists.transform(allContextTypes, new Function<TemplateContextType, String>() {

            @Override
            public String apply(TemplateContextType input) {
                return input.getId();
            }

        });
    }

    public static TemplateContextType getContextType(final Model model, final String path) {
        
        final String normalizedPath = (path != null && path.endsWith("/")) ? path.substring(0, path.length() - 1)
                : path;
        if (RootContextType.isRoot(normalizedPath)) {
            return new RootContextType();
        }
        return Iterables.getFirst(Iterables.filter(allContextTypes, new Predicate<TemplateContextType>() {

            @Override
            public boolean apply(TemplateContextType input) {
                if (input instanceof SchemaBasedTemplateContextType) {
                    return ((SchemaBasedTemplateContextType)input).matches(model, path);
                }
                return false;
            }

        }), null);
    }

}
