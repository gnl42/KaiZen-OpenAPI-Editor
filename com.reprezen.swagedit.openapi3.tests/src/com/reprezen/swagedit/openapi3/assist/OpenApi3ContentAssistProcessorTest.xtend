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
package com.reprezen.swagedit.openapi3.assist

import com.reprezen.swagedit.core.assist.StyledCompletionProposal
import com.reprezen.swagedit.core.editor.JsonDocument
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.util.ArrayList
import org.junit.Test

import static com.reprezen.swagedit.openapi3.utils.Cursors.*
import static org.hamcrest.core.IsCollectionContaining.*
import static org.hamcrest.core.IsNot.*
import static org.junit.Assert.*
import com.reprezen.swagedit.core.json.JsonModel

class OpenApi3ContentAssistProcessorTest {

	val processor = new OpenApi3ContentAssistProcessor(null, new OpenApi3Schema) {
		override protected initTextMessages(JsonModel doc) { new ArrayList }

		override protected getContextTypeRegistry() { null }

		override protected getTemplateStore() { null }

		override protected getContextTypeId(JsonDocument doc, String path) { null }
	}

	@Test
	def void testCallbacksInOperation_ShouldReturn_CallbackName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      callbacks:
			        <1>
		''', document)

		val proposals = test.apply(processor, "1")		
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(callback name):")
		)
	}

	@Test
	def void testCallbacksInComponents_ShouldReturn_CallbackName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  callbacks:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(callback name):")
		)
	}

	@Test
	def void testSchemaInComponents_ShouldReturn_SchemaName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(schema name):")
		)
	}
	
	@Test
	def void testAnonymousSchemaInMediaType_Should_NOT_Return_SchemaName_Key() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
paths:
  /resource:
    get:
      description: description
      responses:
        default:
          description: Ok
          content:
            application/json:
              schema:
                <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			not(hasItem("(schema name):"))
		)
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			not(hasItem("_key_:"))
		)
	}

	@Test
	def void testSchemaInSchemaProperties_ShouldReturn_PropertyName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    MyDataType:
			      type: object
			      properties:
			        <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(property name):")
		)
	}
	
	
	@Test
	def void testResponseInComponents_ShouldReturn_ResponseName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  responses:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(response name):")
		)
	}
	
	
	@Test
	def void testParameterInComponents_ShouldReturn_ParameterName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  parameters:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(parameter name):")
		)
	}
	
	@Test
	def void testExampleInComponents_ShouldReturn_ExampleName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  examples:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(example name):")
		)
	}
	
	@Test
	def void testRequestBodiesInComponents_ShouldReturn_RequestBodyName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  requestBodies:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(requestBody name):")
		)
	}
	
	@Test
	def void testHeaderInComponents_ShouldReturn_HeaderName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  headers:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(header name):")
		)
	}
	
	@Test
	def void testSecuritySchemeInComponents_ShouldReturn_SecuritySchemeName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  securitySchemes:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(securityScheme name):")
		)
	}
	
	@Test
	def void testLinkInComponents_ShouldReturn_LinkName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  links:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(link name):")
		)
	}
	
	@Test
	def void testMediaTypeInContent_Should_NOT_Return_MediaTypeName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
paths:
  "/resource":
    get:
      description: description
      responses:
        '200':
          description: Ok
          content:
            <1>
		''', document)

		val proposals = test.apply(processor, "1")
		// we don't need a "(mediaType name)" as valid mediatypes are provided by 
		// [#395] OpenAPI v3: Content assist for media types
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			not(hasItem("(mediaType name):"))
		)
	}
	
	@Test
	def void testEncodingInMediaType_ShouldReturn_EncodingName() {
		val document = new OpenApi3Document(new OpenApi3Schema)	
		val test = setUpContentAssistTest('''
paths:
  "/resource":
    get:
      description: description
      responses:
        '200':
          description: Ok
          content:
            application/json:
              encoding:
                <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(encoding name):")
		)
	}
	
	@Test
	def void testServerVariableInServer_ShouldReturn_ServerVariableName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
servers:  
- url: https://development.gigantic-server.com/v1
  description: Development server
  variables:
    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(serverVariable name):")
		)
	}
	
	@Test
	def void testAnyInLinkParameter_ShouldReturn_AnyName() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
components:
  links:
    MyLink:
      operationId: getRepositoriesByOwner
      parameters:
        <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(any name):")
		)
	}	
	
	@Test
	def void testStringsInDiscriminatorMapping() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
components:
  schemas:
    MyResponseType:
      oneOf:
      - $ref: '#/components/schemas/Cat'
      - $ref: '#/components/schemas/Dog'
      discriminator:
        propertyName: pet_type
        mapping:
          <1>
		''', document)

		val proposals = test.apply(processor, "1")
		// _key_ is a temporary solution, just documenting the current state
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("_key_:")
		)
	}	

	@Test
	def void testStringsInOauthFlowScopes() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
components:
  securitySchemes:
    mySecurityScheme:
      type: oauth2
      flows: 
        implicit:
          authorizationUrl: https://example.com/api/oauth/dialog
          scopes:
            <1>
		''', document)

		// _key_ is a temporary solution, just documenting the current state
		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("_key_:")
		)
	}

	@Test
	def void testResponseSchemaStillContainsKeyProposal() {
		val document = setUpContentAssistTest('''
		openapi: "3.0.0"
		info:
		  version: 1.0.0
		  title: Test
		paths:
		  /:
		    get:
		      summary: All
		      responses:
		        200:
		          description: Ok          
		          content:
		            application/json:    
		              schema:
		                <1>$ref: "#/components/schemas/Pets"
		components:
		  schemas:
		    Pets:
		      type: object
		''', new OpenApi3Document(new OpenApi3Schema))

		val proposals = document.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("_key_:")
		)
	}

	@Test
	def void testSchemaFormat_ForString() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: string
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("byte", "binary", "date", "date-time", "password", "")
		)
	}

	@Test
	def void testSchemaFormat_ForInteger() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: integer
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("int32", "int64")
		)
	}

	@Test
	def void testSchemaFormat_ForNumber() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: number
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("float", "double")
		)
	}

	@Test
	def void testSchemaFormat_ForOthers() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: boolean
			          format: <1>
			        name2:
			          type: object
			          format: <2>
			        name2:
			          type: array
			          format: <3>
			        name3:
			          type: "null"
			          format: <4>
			        name4:
			          format: <5>
		''', document)

		var proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "2")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "3")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "4")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "5")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("int32", "int64", "float", "double", "byte", "binary", "date", "date-time", "password", "")
		)
	}

	@Test
	def void testResponseStatusCode() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      responses:
			        <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("'100':", "'200':", "'300':", "'400':", "'500':", "default:", "x-")
		)
	}
	
	@Test
	def void testResponseStatusCodeWithPrefix() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      responses:
			         1<1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("'100':", "'101':", "'102':")
		)
	}

}
