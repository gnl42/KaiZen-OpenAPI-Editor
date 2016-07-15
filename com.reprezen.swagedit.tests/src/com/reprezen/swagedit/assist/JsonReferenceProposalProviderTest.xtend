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
package com.reprezen.swagedit.assist

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import org.junit.Before
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*

class JsonReferenceProposalProviderTest {

	val mapper = new ObjectMapper
	var JsonReferenceProposalProvider provider

	@Before
	def void setUp() {
		provider = new JsonReferenceProposalProvider {
			override protected getActiveFile() {
				Mocks.mockJsonReferenceProposalFile()
			}
		}
	}

	@Test
	def void testLocalProposals_Schema() {
		val text = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo:
		    get:
		      responses:
		        '200':
		          schema:
		            $ref: 
		          description: OK
		definitions:
		  Valid:
		    type: string
		'''

		val document = new SwaggerDocument
		document.set(text)

		val proposals = provider.createProposals(":paths:/foo:get:responses:200:schema:$ref", document, 0)

		assertThat(proposals, hasItems(
			mapper.createObjectNode
				.put("value", "\"#/definitions/Valid\"")
				.put("label", "Valid")
				.put("type", "#/definitions/Valid") as JsonNode
		))
	}

	@Test
	def void testLocalProposals_Definitions() {
		val text = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		definitions:
		  Foo:
		    type: object
		  Bar:
		    type: object
		    properties:
		      foo:
		        $ref: 
		'''

		val document = new SwaggerDocument
		document.set(text)

		val proposals = provider.createProposals(":definitions:Bar:properties:foo:$ref", document, 0)

		assertThat(proposals, hasItems(
			mapper.createObjectNode
				.put("value", "\"#/definitions/Foo\"")
				.put("label", "Foo")
				.put("type", "#/definitions/Foo") as JsonNode
		))
	}

	@Test
	def void shouldEncodeWhiteSpaceCharacters() {
		val text = '''
		paths:
		  /foo:
		    get:
		      responses:
		        '200':
		          schema:
		            $ref: 
		          description: OK
		definitions:
		  Valid:
		    type: string
		'''

		val document = new SwaggerDocument
		document.set(text)

		val path = Mocks.mockPath("../Path With Spaces/Other  Spaces.yaml")
		val proposals = provider.collectProposals(document.asJson, "paths", path)

		assertThat(proposals, hasItems(
			mapper.createObjectNode
				.put("value", "\"../Path%20With%20Spaces/Other%20%20Spaces.yaml#/paths/~1foo\"")
				.put("label", "/foo")
				.put("type", "../Path With Spaces/Other  Spaces.yaml#/paths/~1foo") as JsonNode
		))
	}

	@Test
	def void testContextTypes() {
		// schema definitions
		assertTrue(":definitions:Foo:properties:bar:$ref".matches(JsonReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))
		assertTrue(":paths:/foo:get:responses:200:schema:$ref".matches(JsonReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))
		assertTrue(":paths:/foo:get:responses:200:schema:items:$ref".matches(JsonReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))
		
		// responses
		assertTrue(":paths:/foo:get:responses:200:$ref".matches(JsonReferenceProposalProvider.RESPONSE_REGEX))
		
		// parameters
		assertTrue(":paths:/:get:parameters:@0:$ref".matches(JsonReferenceProposalProvider.PARAMETER_REGEX))

		// path items
		assertTrue(":paths:/pets/{id}:$ref".matches(JsonReferenceProposalProvider.PATH_ITEM_REGEX))
	}
}	