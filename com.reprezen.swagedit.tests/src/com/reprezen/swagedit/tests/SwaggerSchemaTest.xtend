package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.SwaggerSchema
import io.swagger.util.Yaml
import org.junit.Test

import static org.junit.Assert.*

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
		val result = schema.getProposals("/info", node)

		assertArrayEquals(#[ 
			"title",
			"version", 
			"description",		
			"termsOfService",
			"contact",
			"license"
		], result.asCompletionProposal(1).map[ it.displayString ])
	}

}