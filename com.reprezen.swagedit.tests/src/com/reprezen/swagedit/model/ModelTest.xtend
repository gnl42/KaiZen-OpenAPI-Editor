package com.reprezen.swagedit.model

import org.junit.Test
import com.reprezen.swagedit.json.JsonSchemaManager

import static org.junit.Assert.*
import com.reprezen.swagedit.json.JsonType2.ArrayType
import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.json.JsonType2.ObjectType

class ModelTest {

	val schema = new JsonSchemaManager().swaggerSchema

	@Test
	def void parseRootValues() {
		val text = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
		'''

		val model = Model.parseYaml(text)
		val root = model.root

		assertTrue(root.type2 instanceof ObjectType)

		val swagger = root.get("swagger").type2
		assertEquals("swagger", swagger.containingProperty)
		assertEquals(
			schema.asJson.get("properties").get("swagger"),
			swagger.definition
		)

		val info = root.get("info").type2
		assertTrue(info instanceof ObjectType)

		assertEquals("info", info.containingProperty)
		assertEquals(
			schema.asJson.get("definitions").get("info"),
			info.definition
		)

		val version = root.get("info").get("version").type2
		assertEquals("version", version.containingProperty)
		assertEquals(
			schema.asJson.get("definitions").get("info").get("properties").get("version"),
			version.definition
		)

		val title = root.get("info").get("title").type2
		assertEquals("title", title.containingProperty)
		assertEquals(
			schema.asJson.get("definitions").get("info").get("properties").get("title"),
			title.definition
		)
	}

	@Test
	def void testParseEnumArrayValues() {
		val text = '''
			schemes:
			  - http
			  - https
		'''

		val model = Model.parseYaml(text)
		val root = model.root

		val schemes = root.get("schemes").type2
		assertTrue(schemes instanceof ArrayType)
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/schemesList")), schemes.definition)

		val http = root.get("schemes").get(0).type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/schemesList")).get("items"), http.definition)

		val https = root.get("schemes").get(1).type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/schemesList")).get("items"), https.definition)
	}

	@Test
	def void testParseArrayValues() {
		val text = '''
			consumes:
			  - application/json
			  - text/xml
		'''

		val model = Model.parseYaml(text)
		val root = model.root

		val consumes = root.get("consumes").type2
		assertTrue(consumes instanceof ArrayType)
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/mediaTypeList")), consumes.definition)

		val http = root.get("consumes").get(0).type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/mimeType")), http.definition)
		
		val https = root.get("consumes").get(1).type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/mimeType")), https.definition)
	}

	@Test
	def void testParsePathParameters_OneOfType() {
		val text = '''
			paths:
			  /:
			    get:
			      parameters:
			        - name: limit
			          in: query
			          description: number of pets to return
			          type: integer
			          default: 11
			          minimum: 11
			          maximum: 10000
		'''

		val model = Model.parseYaml(text)
		val root = model.root

		val paths = root.get("paths").type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/paths")), paths.definition)
		
		val pathItem = root.get("paths").get("/").type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/pathItem")), pathItem.definition)
		
		val get = root.get("paths").get("/").get("get").type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/operation")), get.definition)
		
		val parameters = root.get("paths").get("/").get("get").get("parameters").type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/queryParameterSubSchema")), parameters.definition)
		
		val param1 = root.get("paths").get("/").get("get").get("parameters").get(0).type2
		assertEquals(schema.asJson.at(JsonPointer.compile("/definitions/parameter/")), param1.definition)
	}
}
