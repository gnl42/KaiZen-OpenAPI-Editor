package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.SwaggerSchema
import io.swagger.util.Yaml
import org.junit.Test

class SwaggerSchemaTest {

	val schema = new SwaggerSchema()

	@Test
	def void testInfoProposals() {
		val yaml = '''
		info:
		  description: ""
		  version: "1.0.0"
		tags:
		  - foo: ""
		  - bar: ""
		'''

		val node = Yaml.mapper.readTree(yaml)
		schema.getProposals("/info", node)
	}

}