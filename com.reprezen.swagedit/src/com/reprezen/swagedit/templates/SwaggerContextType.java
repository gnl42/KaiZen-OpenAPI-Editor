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
package com.reprezen.swagedit.templates;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public abstract class SwaggerContextType extends TemplateContextType {

    private static final String PARAMETERS_SCHEMA_REGEX = ".*/parameters(/\\d+)/schema";
    private static final String PATH_ITEM_REGEX = "/paths/~1[^/]+";
    // we can use a ? here as both 'PATH_ITEM_REGEX + "/parameters$"' and
    // 'PATH_ITEM_REGEX + "/[^/]+/parameters$"' are supported
    private static final String PARAMETERS_LIST_REGEX = PATH_ITEM_REGEX + "/([^/]+/)?parameters";

    public SwaggerContextType() {
        addGlobalResolvers();
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

    public static String getContextType(String path) {
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return RootContextType.CONTEXT_ID;
        }
        if (path.equals("/securityDefinitions")) {
            return SecurityDefContextType.CONTEXT_ID;
        }
        if (path.equals("/paths")) {
            return PathsContextType.CONTEXT_ID;
        }
        if (path.matches(PATH_ITEM_REGEX + "$")) { // /paths/[pathItem]/
            return PathItemContextType.CONTEXT_ID;
        }
        if (path.equals("/responses")//
                || path.matches(PATH_ITEM_REGEX + "/[^/]+/responses$")) {
            return ResponsesContextType.CONTEXT_ID;
        }
        if (path.matches(PARAMETERS_LIST_REGEX + "$")) {
            return ParametersListContextType.CONTEXT_ID;
        }
        if (path.matches(PARAMETERS_LIST_REGEX + "/\\d+$")//
                || path.matches("/parameters/[^/]+$")) {
            return ParameterObjectContextType.CONTEXT_ID;
        }
        if (path.equals("/parameters")) {
            return ParameterDefinitionContextType.CONTEXT_ID;
        }
        if (path.matches("/definitions/[^/]+$") //
                || path.matches(".+/[^/]+/additionalProperties$")//
                || path.matches(PARAMETERS_SCHEMA_REGEX + "$")//
                || path.matches(PARAMETERS_SCHEMA_REGEX + "/items$")//
                || path.matches(PARAMETERS_SCHEMA_REGEX + "/properties/[^/]+$")//
                || path.matches(".+/responses/[^/]+/schema$")) {
            return SchemaContextType.CONTEXT_ID;
        }
        return null;
    }

    public static Collection<String> allContextTypes() {
        return Collections.unmodifiableList(Arrays.asList(//
                RootContextType.CONTEXT_ID, //
                SecurityDefContextType.CONTEXT_ID, //
                PathsContextType.CONTEXT_ID, //
                PathItemContextType.CONTEXT_ID, //
                ResponsesContextType.CONTEXT_ID, //
                ParametersListContextType.CONTEXT_ID, //
                ParameterObjectContextType.CONTEXT_ID, //
                ParameterDefinitionContextType.CONTEXT_ID, //
                SchemaContextType.CONTEXT_ID));
    }

    public static class PathItemContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.path_item";
    }

    public static class SecurityDefContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.security_def";
    }

    public static class RootContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.root";
    }

    public static class PathsContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.paths";
    }

    public static class ResponsesContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.responses";
    }

    public static class ParametersListContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.parameters_list";
    }

    public static class ParameterObjectContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.parameter_object";
    }

    public static class ParameterDefinitionContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.parameter_definition";
    }

    public static class SchemaContextType extends SwaggerContextType {
        public static final String CONTEXT_ID = "com.reprezen.swagedit.templates.swagger.schema";
    }
}
