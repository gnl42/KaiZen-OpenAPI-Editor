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

class JsonReferenceProposalProviderTest {

	var JsonReferenceProposalProvider provider

	@Before
	def void setUp() {
		provider = new JsonReferenceProposalProvider {
			override protected getActiveFile() {
				null
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
		println(proposals)
	}

}	