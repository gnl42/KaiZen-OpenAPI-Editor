package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerDocument
import org.junit.Test

import static org.junit.Assert.*

class SwaggerDocumentTest {

	private val document = new SwaggerDocument

	@Test
	def void testGetPaths() {
		val yaml = '''
		info:
		  description: ""
		  version: "1.0.0"
		tags:
		  - foo: ""
		  - bar: ""
		'''

		document.set(yaml)
		assertEquals(":info", document.getPath(0, 1))
		assertEquals(":info:description", document.getPath(1, 2))
		assertEquals(":info:version", document.getPath(2, 2))
		assertEquals(":tags", document.getPath(3, 2))

		assertEquals(":tags:@0:foo", document.getPath(4, 4))
		assertEquals(":tags:@1:bar", document.getPath(5, 4))
	}

	@Test
	def void testGetPathOnEmptyLine() {
		val yaml = '''
		info:
		  description: ""
		  
		  version: "1.0.0"
		'''

		document.set(yaml)
		assertEquals(":info:description", document.getPath(1, 10))
		assertEquals(":info", document.getPath(2, 2))
		assertEquals(":info:version", document.getPath(3, 2))
	}

	@Test
	def void testGetPathOnPaths() {
		val yaml = '''
		paths:
		  /:
		    get:
		      responses:
		        '200':
		'''

		document.set(yaml)

		assertEquals(":paths", document.getPath(0, 1));
		assertEquals(":paths:/", document.getPath(1, 2));
		assertEquals(":paths:/:get", document.getPath(2, 4));
		assertEquals(":paths:/:get:responses", document.getPath(3, 6));
		assertEquals(":paths:/:get:responses:200", document.getPath(4, 8));
	}

	@Test
	def void testGetPathOnPathsAfter() {
		val yaml = '''
		paths:
		  /:
		    
		'''

		document.set(yaml)

		assertEquals(":paths", document.getPath(0, 1));
		assertEquals(":paths:/", document.getPath(1, 1));
		assertEquals(":paths:/", document.getPath(2, 3));
	}
	
	@Test
	def void testGetPath() {
		val yaml = '''
		paths:
		  /:
		    get:
		      responses:
		        '200':
		          description: OK
		    
		parameters:
		  foo:
		    name: foo
		'''
		
		document.set(yaml)
		
		assertEquals(":paths:/", document.getPath(6, 3))
	}

}
