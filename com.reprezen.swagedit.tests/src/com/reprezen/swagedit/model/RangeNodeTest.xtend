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
		assertNull(root.getFieldLocation)
		assertEquals(1, root.getContentLocation.startLine)
		
		val swagger = ranges.get(JsonPointer.compile("/swagger"))
		assertNotNull(swagger)
		assertNotNull(swagger.getFieldLocation)
		assertEquals(1, swagger.getFieldLocation.startLine)
		assertEquals(1, swagger.getContentLocation.startLine)

		val info = ranges.get(JsonPointer.compile("/info"))
		assertNotNull(info)
		assertNotNull(info.getFieldLocation)
		assertEquals(2, info.getFieldLocation.startLine)
		assertEquals(3, info.getContentLocation.startLine)

		val info_version = ranges.get(JsonPointer.compile("/info/version"))
		assertNotNull(info_version)
		assertNotNull(info_version.getFieldLocation)
		assertEquals(3, info_version.getFieldLocation.startLine)
		assertEquals(3, info_version.getContentLocation.startLine)

		val info_title = ranges.get(JsonPointer.compile("/info/title"))
		assertNotNull(info_title)
		assertNotNull(info_title.getFieldLocation)
		assertEquals(4, info_title.getFieldLocation.startLine)
		assertEquals(4, info_title.getContentLocation.startLine)
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
		assertNotNull(definitions.getFieldLocation)
		assertEquals(1, definitions.getFieldLocation.startLine)
		assertEquals(2, definitions.getContentLocation.startLine)
		
		val definitions_foo = ranges.get(JsonPointer.compile("/definitions/foo"))
		assertNotNull(definitions_foo)
		assertNotNull(definitions_foo.getFieldLocation)
		assertEquals(2, definitions_foo.getFieldLocation.startLine)
		assertEquals(3, definitions_foo.getContentLocation.startLine)

		val definitions_foo_type = ranges.get(JsonPointer.compile("/definitions/foo/type"))
		assertNotNull(definitions_foo_type)
		assertNotNull(definitions_foo_type.getFieldLocation)
		assertEquals(3, definitions_foo_type.getFieldLocation.startLine)
		assertEquals(3, definitions_foo_type.getContentLocation.startLine)
	}
}
