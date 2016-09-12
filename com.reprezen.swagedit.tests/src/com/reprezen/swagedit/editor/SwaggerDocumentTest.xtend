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
package com.reprezen.swagedit.editor

import org.junit.Test

import static com.reprezen.swagedit.tests.utils.Cursors.*

class SwaggerDocumentTest {

	private val document = new SwaggerDocument

	@Test
	def void testGetPaths() {
		val test = setUpPathTest('''
			<1>info:
			  description<2>: ""
			  version<3>: "1.0.0"
			tags<4>:
			  - foo<5>
			  - bar<6>
		''', document)

		test.apply("/info", "1")
		test.apply("/info/description", "2")
		test.apply("/info/version", "3")
		test.apply("/tags", "4")
		test.apply("/tags/0", "5")
		test.apply("/tags/1", "6")
	}

	@Test
	def void testGetPathOnEmptyLine() {
		val test = setUpPathTest('''
			info:
			  description<1>: ""
			  <2>
			  version<3>: "1.0.0"
		''', document)

		test.apply("/info/description", "1")
		test.apply("/info", "2")
		test.apply("/info/version", "3")
	}

	@Test
	def void testGetPathOnEmptyLineAfter() {
		val test = setUpPathTest('''
			info:
			  description:<1> ""
			  version:<2> "1.0.0"
			  <3>
		''', document)

		test.apply("/info/description", "1")
		test.apply("/info/version", "2")
		test.apply("/info", "3")
	}

	@Test
	def void testGetPathOnPaths() {
		val test = setUpPathTest('''
			paths<1>:
			  /<2>:
			    get<3>:
			      responses<4>:
			        '200'<5>:
		''', document)

		test.apply("/paths", "1")
		test.apply("/paths/~1", "2")
		test.apply("/paths/~1/get", "3")
		test.apply("/paths/~1/get/responses", "4")
		test.apply("/paths/~1/get/responses/200", "5")
	}

	@Test
	def void testGetPathOnPathsAfter() {
		val test = setUpPathTest('''
			<1>p<2>aths<3>:
			  /<4>:
			    
		''', document)

		test.apply("/paths", "1")
		test.apply("/paths", "2")
		test.apply("/paths", "3")
		test.apply("/paths/~1", "4")
	}

	@Test
	def void testGetPath() {
		val test = setUpPathTest('''
			paths:
			  /:
			    get:
			      responses:
			        '200':
			          description: OK
			   <1> 
			parameters:
			  foo:
			    name: foo
		''', document)

		test.apply("/paths/~1", "1")
	}

	@Test
	def void testGetPath2() {
		val test = setUpPathTest('''
			paths:
			  /test<1>ing<2>:
			    <3>get<4>:
			      parameters:
			        - name: id
			          type: number
			          required: true
			          in: path
		''', document)

		test.apply("/paths/~1testing", "1")
		test.apply("/paths/~1testing", "2")
		test.apply("/paths/~1testing/get", "3")
		test.apply("/paths/~1testing/get", "4")
	}

}
