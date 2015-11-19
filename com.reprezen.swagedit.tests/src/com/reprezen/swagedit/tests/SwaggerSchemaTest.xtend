package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.SwaggerSchema
import org.junit.Test

import static org.junit.Assert.*

class SwaggerSchemaTest {

	val schema = new SwaggerSchema()

	@Test
	def void testGetDefinitionForRoot() {
		val definition = schema.getDefinitionForPath(":")
		
		assertNotNull(definition)
		assertEquals(schema.asJson, definition)
	}

}
