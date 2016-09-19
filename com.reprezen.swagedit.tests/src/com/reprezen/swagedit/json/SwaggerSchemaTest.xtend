package com.reprezen.swagedit.json

import com.reprezen.swagedit.model.Model
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.tests.utils.PointerHelpers
import com.reprezen.swagedit.model.ValueNode

class SwaggerSchemaTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema

	@Test
	def void test() {
		assertNotNull(schema.rootType)

		println(schema.rootType.properties.get("swagger"))
		println(schema.rootType.properties.get("paths"))
	}

	@Test
	def void test2() {
		val yaml = '''
			swagger: 2.0
		'''

		val root = Model.parseYaml(schema, yaml).root

		val schema = new SwaggerSchema
		val rootType = schema.getType(root)
		val swaggerType = schema.getType(root.get("swagger"))

		println(rootType)
		println(swaggerType)
	}

	@Test
	def void test3() {
		val yaml = '''
			info:
			  title: a
			  description: b
		'''

		val root = Model.parseYaml(schema, yaml).root

		val schema = new SwaggerSchema
		val rootType = schema.getType(root)
		val infoType = schema.getType(root.get("info"))
		val titleType = schema.getType(root.get("info").get("title"))

		assertTrue(rootType instanceof ObjectTypeDefinition)
		assertTrue(infoType instanceof ObjectTypeDefinition)
		assertTrue(titleType instanceof TypeDefinition)
	}

	@Test
	def void test4() {
		val yaml = '''
			info:
			  title: a
			  description: b
		'''

		val root = Model.parseYaml(schema, yaml).root

		val schema = new SwaggerSchema
		val rootType = schema.getType(root)
		val infoType = schema.getType(root.get("info"))
		val titleType = schema.getType(root.get("info").get("title"))

		assertTrue(rootType instanceof ObjectTypeDefinition)
		assertTrue(infoType instanceof ObjectTypeDefinition)
		assertTrue(titleType instanceof TypeDefinition)
	}

	@Test
	def void testTypeGetPath2() {
		val yaml = '''
			paths:
			  /foo:
			    get:
		'''
	}
	
	@Test
	def void test5() {
		val type = schema.getType(new ValueNode(null, "/paths/~1pets/get/parameters/0/in".ptr, null))

		println(type)
	}
}
