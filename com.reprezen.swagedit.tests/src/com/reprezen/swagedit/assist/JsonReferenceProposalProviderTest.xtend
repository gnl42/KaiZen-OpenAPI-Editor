/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.assist

import com.reprezen.swagedit.core.assist.contexts.RegexContextType
import com.reprezen.swagedit.core.utils.SwaggerFileFinder.Scope
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.junit.Before
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.core.assist.ProposalDescriptor

class JsonReferenceProposalProviderTest {

	extension PointerHelpers = new PointerHelpers
	var SwaggerReferenceProposalProvider provider

	@Before
	def void setUp() {
		provider = new SwaggerReferenceProposalProvider {
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

		val proposals = provider.getProposals("/paths/~1foo/get/responses/200/schema/$ref".ptr, document, Scope.LOCAL)

		assertThat(proposals, hasItems(
			new ProposalDescriptor("Valid").replacementString("\"#/definitions/Valid\"").type("#/definitions/Valid")
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

		val proposals = provider.getProposals("/definitions/Bar/properties/foo/$ref".ptr, document, Scope.LOCAL)

		assertThat(proposals, hasItems(
			new ProposalDescriptor("Foo").replacementString("\"#/definitions/Foo\"").type("#/definitions/Foo")
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
		val contextType = new RegexContextType("paths", "paths", "")
		val proposals = contextType.collectProposals(document.asJson, path)

		assertThat(proposals, hasItems(
			new ProposalDescriptor("/foo")//
				.replacementString("\"../Path%20With%20Spaces/Other%20%20Spaces.yaml#/paths/~1foo\"")
				.type("../Path With Spaces/Other  Spaces.yaml#/paths/~1foo"
			)
		))
	}

	@Test
	def void testContextTypes() {
		// schema definitions
		assertTrue(
			"/definitions/Foo/properties/bar/$ref".matches(SwaggerReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))
		assertTrue(
			"/paths/~1foo/get/responses/200/schema/$ref".matches(SwaggerReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))
		assertTrue(
			"/paths/~1foo/get/responses/200/schema/items/$ref".matches(
				SwaggerReferenceProposalProvider.SCHEMA_DEFINITION_REGEX))

		// responses
		assertTrue("/paths/~1foo/get/responses/200/$ref".matches(SwaggerReferenceProposalProvider.RESPONSE_REGEX))		
		assertTrue("/paths/~1foo/get/responses/default/$ref".matches(SwaggerReferenceProposalProvider.RESPONSE_REGEX))		
		assertFalse("/paths/~1foo/get/responses/2XX/$ref".matches(SwaggerReferenceProposalProvider.RESPONSE_REGEX))		

		// parameters
		assertTrue("/paths/~1/get/parameters/0/$ref".matches(SwaggerReferenceProposalProvider.PARAMETER_REGEX))

		// path items
		assertTrue("/paths/~1pets~1{id}/$ref".matches(SwaggerReferenceProposalProvider.PATH_ITEM_REGEX))
	}
}
