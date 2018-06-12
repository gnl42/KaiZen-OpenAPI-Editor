package com.reprezen.swagedit.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.swagedit.core.json.LineRecorderYamlFactory
import com.reprezen.swagedit.core.json.LineRecorderYamlParser
import org.junit.Test

import static org.junit.Assert.*
import com.fasterxml.jackson.core.JsonPointer

class RangeNodeTest {

	val factory = new LineRecorderYamlFactory()
	val mapper = new ObjectMapper(factory)

	@Test
	def void parseRootValues() {
		val text = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
		'''
		val parser = factory.createParser(text) as LineRecorderYamlParser
		val content = mapper.reader().readTree(parser)

		val ranges = parser.lines

		val root = ranges.get(JsonPointer.compile(""))
		assertNotNull(root)
		assertNull(root.fieldLocation)
		assertEquals(1, root.contentLocation.startLine)
		
		val swagger = ranges.get(JsonPointer.compile("/swagger"))
		assertNotNull(swagger)
		assertNotNull(swagger.fieldLocation)
		assertEquals(1, swagger.fieldLocation.startLine)
		assertEquals(1, swagger.contentLocation.startLine)

		val info = ranges.get(JsonPointer.compile("/info"))
		assertNotNull(info)
		assertNotNull(info.fieldLocation)
		assertEquals(2, info.fieldLocation.startLine)
		assertEquals(3, info.contentLocation.startLine)

		val info_version = ranges.get(JsonPointer.compile("/info/version"))
		assertNotNull(info_version)
		assertNotNull(info_version.fieldLocation)
		assertEquals(3, info_version.fieldLocation.startLine)
		assertEquals(3, info_version.contentLocation.startLine)

		val info_title = ranges.get(JsonPointer.compile("/info/title"))
		assertNotNull(info_title)
		assertNotNull(info_title.fieldLocation)
		assertEquals(4, info_title.fieldLocation.startLine)
		assertEquals(4, info_title.contentLocation.startLine)
	}

	@Test
	def void parseDefinitions() {
		val text = '''
		definitions:
		  foo:
		    type: object
		'''
		val parser = factory.createParser(text) as LineRecorderYamlParser
		val content = mapper.reader().readTree(parser)

		val ranges = parser.lines
		
		val definitions = ranges.get(JsonPointer.compile("/definitions"))
		assertNotNull(definitions)
		assertNotNull(definitions.fieldLocation)
		assertEquals(1, definitions.fieldLocation.startLine)
		assertEquals(2, definitions.contentLocation.startLine)
		
		val definitions_foo = ranges.get(JsonPointer.compile("/definitions/foo"))
		assertNotNull(definitions_foo)
		assertNotNull(definitions_foo.fieldLocation)
		assertEquals(2, definitions_foo.fieldLocation.startLine)
		assertEquals(3, definitions_foo.contentLocation.startLine)

		val definitions_foo_type = ranges.get(JsonPointer.compile("/definitions/foo/type"))
		assertNotNull(definitions_foo_type)
		assertNotNull(definitions_foo_type.fieldLocation)
		assertEquals(3, definitions_foo_type.fieldLocation.startLine)
		assertEquals(3, definitions_foo_type.contentLocation.startLine)
	}
}
