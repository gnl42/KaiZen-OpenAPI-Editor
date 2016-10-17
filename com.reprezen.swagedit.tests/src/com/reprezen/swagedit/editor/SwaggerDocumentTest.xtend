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

import com.reprezen.swagedit.tests.utils.Cursors
import org.junit.Test

import static com.reprezen.swagedit.tests.utils.Cursors.*
import static org.junit.Assert.*

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
	def void testGetPathInfoLicense() {
		val test = setUpPathTest('''
			swagger: "2.0"
			info:
			  version: 1.0.0
			  title: Swagger Petstore
			  license:
			    <1>
		''', document)

		test.apply("/info/license", "1")
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
	def void groupTest() {
		val expected = '''
		paths:
		  /testing:
		    get:
		      parameters:
		        
		'''

		val yaml = '''
		paths:
		  /test<1>ing<2>:
		    <3>get<4>:
		      parameters:
		        <5>
		'''
		
		val doc = new SwaggerDocument
		
		doc.set(expected)
		val groups = Cursors.groupMarkers(yaml)

		var r = groups.get("1")	
		var line = doc.getLineOfOffset(r.offset)
		assertEquals(1, line)
		assertEquals(7, doc.getColumnOfOffset(1, r))
		
		r = groups.get("2")	
		line = doc.getLineOfOffset(r.offset)
		assertEquals(1, line)
		assertEquals(10, doc.getColumnOfOffset(1, r))

		r = groups.get("3")	
		line = doc.getLineOfOffset(r.offset)
		assertEquals(2, line)
		assertEquals(4, doc.getColumnOfOffset(2, r))
		
		r = groups.get("4")	
		line = doc.getLineOfOffset(r.offset)
		assertEquals(2, line)
		assertEquals(7, doc.getColumnOfOffset(2, r))
		
		r = groups.get("5")	
		line = doc.getLineOfOffset(r.offset)
		assertEquals(4, line)
		assertEquals(8, doc.getColumnOfOffset(4, r))
	}
	
	@Test
	def void testGetPath2() {
		val test = setUpPathTest('''
		paths:
		  /test<1>ing<2>:
		    <3>get<4>:
		      parameters:
		        <5>
		    <6>
		''', document)

		test.apply("/paths/~1testing", "1")
		test.apply("/paths/~1testing", "2")
		test.apply("/paths/~1testing", "3")
		test.apply("/paths/~1testing/get", "4")
		test.apply("/paths/~1testing/get/parameters", "5")
		test.apply("/paths/~1testing", "6")
	}

	@Test
	def void testGetPath3() {
		val test = setUpPathTest('''
			paths:
			  /testing:
			    get:
			      parameters:
			        - <1>
			        - name: id
			          type: number
			          required: true
			          in: path
			          <2>
		''', document)

		test.apply("/paths/~1testing/get/parameters/0", "1")
		test.apply("/paths/~1testing/get/parameters/1", "2")
	}

	@Test
	def void testPointerParameter() {
		val doc = new SwaggerDocument
		val test = Cursors.setUpPathTest('''
			paths:
			  /pets:
			    get:
			      parameters:
			        - $r<1>ef: 's<2>ome_ref'
			        - $<3>ref: 'some_o<4>ther_ref'
		''', doc)

		test.apply("/paths/~1pets/get/parameters/0", "1")
		test.apply("/paths/~1pets/get/parameters/0/$ref", "2")
		test.apply("/paths/~1pets/get/parameters/1", "3")
		test.apply("/paths/~1pets/get/parameters/1/$ref", "4")
	}

}
