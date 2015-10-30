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
		assertEquals("/info", document.getPath(0))
		assertEquals("/info/description", document.getPath(1))
		assertEquals("/info/version", document.getPath(2))
		assertEquals("/tags", document.getPath(3))

		assertEquals("/tags/0/foo", document.getPath(4))
		assertEquals("/tags/1/bar", document.getPath(5))
	}

}