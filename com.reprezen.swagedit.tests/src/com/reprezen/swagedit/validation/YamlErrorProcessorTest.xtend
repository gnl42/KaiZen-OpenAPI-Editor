package com.reprezen.swagedit.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.swagedit.core.validation.Messages
import com.reprezen.swagedit.core.validation.YamlErrorProcessor
import org.junit.Test

import static org.junit.Assert.*

class YamlErrorProcessorTest {

	val processor = new YamlErrorProcessor

	@Test
	def void testIndentationError() {
		val content = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: Swagger Petstore
			paths: {}
			components:
			  schemas:
			    Pet:
			      required:
			        - id
			      properties:
			        id:
			          type: integer
			          format: int64
			       name:
			         type: string
		'''

		var mapper = new ObjectMapper(new YAMLFactory)
		try {
			mapper.readTree(content)
			fail()
		} catch (Exception e) {
			assertEquals(Messages.error_yaml_parser_indentation, processor.rewriteMessage(e))
		}
	}

}
