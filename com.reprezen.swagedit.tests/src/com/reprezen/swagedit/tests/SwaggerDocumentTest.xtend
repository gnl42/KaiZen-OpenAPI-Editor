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
		assertEquals(":info:description", document.getPath(1, 13))
		assertEquals(":info:version", document.getPath(2, 9))
		assertEquals(":tags", document.getPath(3, 2))

		assertEquals(":tags:@0:foo", document.getPath(4, 7))
		assertEquals(":tags:@1:bar", document.getPath(5, 7))
	}

	@Test
	def void testGetPathOnEmptyLine() {
		val yaml = '''
		info:
		  description: ""
		  
		  version: "1.0.0"
		'''

		document.set(yaml)
		assertEquals(":info:description", document.getPath(1, 13))
		assertEquals(":info", document.getPath(2, 2))
		assertEquals(":info:version", document.getPath(3, 9))
	}

	@Test
	def void testGetPathOnEmptyLineAfter() {
		val yaml = '''
		info:
		  description: ""
		  version: "1.0.0"
		  
		'''

		document.set(yaml)
		assertEquals(":info:description", document.getPath(1, 14))
		assertEquals(":info:version", document.getPath(2, 9))
		assertEquals(":info", document.getPath(3, 2))
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
		assertEquals(":paths:/", document.getPath(1, 3));
		assertEquals(":paths:/:get", document.getPath(2, 7));
		assertEquals(":paths:/:get:responses", document.getPath(3, 14));
		assertEquals(":paths:/:get:responses:200", document.getPath(4, 10));
	}

	@Test
	def void testGetPathOnPathsAfter() {
		val yaml = '''
		paths:
		  /:
		    
		'''

		document.set(yaml)

		assertEquals(":paths", document.getPath(0, 1));
		assertEquals(":paths:", document.getPath(1, 1));
		assertEquals(":paths:/", document.getPath(1, 3));
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
