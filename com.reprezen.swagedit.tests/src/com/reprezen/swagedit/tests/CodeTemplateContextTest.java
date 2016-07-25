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
package com.reprezen.swagedit.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.reprezen.swagedit.templates.SwaggerContextType.getContextType;

import org.junit.Test;

import com.reprezen.swagedit.templates.SwaggerContextType.ParameterDefinitionContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.ParametersListContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.ParameterObjectContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.PathItemContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.ResponsesContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.SchemaContextType;
import com.reprezen.swagedit.templates.SwaggerContextType.SecurityDefContextType;

;

public class CodeTemplateContextTest {

    @Test
    public void testPathItem() throws Exception {
        assertThat(getContextType(":paths:/petstore"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pet-store"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets/{id}"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets/{pet-id}"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/my-pets/{pet-id}"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/my-pets/v1/{pet-id}"), equalTo(PathItemContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets"), equalTo(PathItemContextType.CONTEXT_ID));

        // tests for #Templates defined in the Path context show in other
        // contexts
        assertThat(getContextType(":paths:"), not(equalTo(PathItemContextType.CONTEXT_ID)));
        assertThat(getContextType(":paths:/pets:get"), not(equalTo(PathItemContextType.CONTEXT_ID)));
        assertThat(getContextType(":paths:/pets:get:responses"), not(equalTo(PathItemContextType.CONTEXT_ID)));
        assertThat(getContextType(":paths:/my-pets/v1/{pet-id}:somethingElse"),
                not(equalTo(PathItemContextType.CONTEXT_ID)));
    }

    @Test
    public void testSecurityDef() throws Exception {
        assertThat(getContextType(":securityDefinitions"), equalTo(SecurityDefContextType.CONTEXT_ID));
    }

    @Test
    public void testResponses() throws Exception {
        assertThat(getContextType(":responses"), equalTo(ResponsesContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/resource:get:responses"), equalTo(ResponsesContextType.CONTEXT_ID));
    }

    @Test
    public void testParameterObject() throws Exception {
        // top-level parameter definition
        assertThat(getContextType(":parameters:skipParam:"), equalTo(ParameterObjectContextType.CONTEXT_ID));
        // resource parameter
        assertThat(getContextType(":paths:/taxFilings/{id}:parameters:@0:"),
                equalTo(ParameterObjectContextType.CONTEXT_ID));
        // method parameter
        assertThat(getContextType(":paths:/taxFilings/{id}:get:parameters:@0:"),
                equalTo(ParameterObjectContextType.CONTEXT_ID));
    }

    @Test
    public void testParameterDefinition() throws Exception {
        assertThat(getContextType(":parameters:"), equalTo(ParameterDefinitionContextType.CONTEXT_ID));
    }

    @Test
    public void testParametersList() throws Exception {
        // resource parameters list
        assertThat(getContextType(":paths:/resource:parameters:"), equalTo(ParametersListContextType.CONTEXT_ID));
        // method parameters list
        assertThat(getContextType(":paths:/taxFilings/{id}:get:parameters"),
                equalTo(ParametersListContextType.CONTEXT_ID));
    }

    @Test
    public void testSchema() throws Exception {
        assertThat(getContextType(":definitions:Pet:"), equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets/{id}:delete:responses:default:schema:"),
                equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets/{id}:get:responses:200:schema:"), equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets:post:parameters:@0:schema:"), equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets:post:parameters:@0:schema:properties:name"),
                equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":paths:/pets:post:parameters:@0:schema:items"),
                equalTo(SchemaContextType.CONTEXT_ID));
        assertThat(getContextType(":definitions:TaxFilingObject:additionalProperties"),
                equalTo(SchemaContextType.CONTEXT_ID));
    }

    @Test
    public void test$InRegex() throws Exception {
        assertFalse("abcd".matches("abc"));
        assertTrue("abc".matches("abc"));

        assertFalse("abcd".matches("abc$"));
        assertTrue("abc".matches("abc$"));
    }

}
