package com.reprezen.swagedit.json

import com.reprezen.swagedit.json.JsonType2.ObjectType
import com.reprezen.swagedit.model.Model
import org.junit.Test

import static org.junit.Assert.*

class SwaggerSchemaTest {

	@Test
	def void test() {
		val schema = new SwaggerSchema
		
		assertNotNull(schema.rootType)
		
		println(schema.rootType.properties.get("swagger"))
		println(schema.rootType.properties.get("paths"))
	}

	@Test
	def void test2() {
		val yaml = '''
		  swagger: 2.0
		'''

		val root = Model.parseYaml(yaml).root
		
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

		val root = Model.parseYaml(yaml).root
		
		val schema = new SwaggerSchema
		val rootType = schema.getType(root)
		val infoType = schema.getType(root.get("info"))
		val titleType = schema.getType(root.get("info").get("title"))
		
		assertTrue(rootType instanceof ObjectType)
		assertTrue(infoType instanceof ObjectType)
		assertTrue(titleType instanceof JsonType2)
	}

	@Test
	def void test4() {
		val yaml = '''
		  info:
		    title: a
		    description: b
		'''

		val root = Model.parseYaml(yaml).root
		
		val schema = new SwaggerSchema
		val rootType = schema.getType(root)
		val infoType = schema.getType(root.get("info"))
		val titleType = schema.getType(root.get("info").get("title"))
		
		assertTrue(rootType instanceof ObjectType)
		assertTrue(infoType instanceof ObjectType)
		assertTrue(titleType instanceof JsonType2)
	}

}