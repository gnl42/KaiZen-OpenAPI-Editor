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

import org.junit.Test
import com.reprezen.swagedit.editor.SwaggerDocument
import org.junit.Before
import com.reprezen.swagedit.mocks.Mocks

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

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
	def void testLocalProposals() {
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

		val proposals = provider.createProposals(":paths:/foo:get:responses:200:schema", document, 0)

		assertThat(proposals, hasItems(
			mapper.createObjectNode
				.put("value", "\"#/definitions/Valid\"")
				.put("label", "Valid")
				.put("type", "#/definitions/Valid") as JsonNode
		))
	}

}	