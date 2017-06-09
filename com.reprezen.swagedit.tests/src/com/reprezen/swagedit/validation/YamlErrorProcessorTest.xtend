package com.reprezen.swagedit.validation

import com.reprezen.swagedit.core.validation.YamlErrorProcessor
import io.swagger.util.Yaml
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.core.validation.Messages

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

		var mapper = Yaml.mapper
		try {
			mapper.readTree(content)
			fail()
		} catch (Exception e) {
			assertEquals(Messages.error_yaml_parser_indentation, processor.rewriteMessage(e))
		}
	}

}
