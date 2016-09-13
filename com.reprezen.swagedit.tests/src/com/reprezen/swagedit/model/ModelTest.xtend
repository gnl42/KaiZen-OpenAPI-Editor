package com.reprezen.swagedit.model

import com.reprezen.swagedit.json.SwaggerSchema
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.json.ObjectTypeDefinition
import com.reprezen.swagedit.json.ArrayTypeDefinition

class ModelTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema

	@Test
	def void parseRootValues() {
		val text = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
		'''

		val model = Model.parseYaml(schema, text)
		val root = model.root

		assertTrue(root.type instanceof ObjectTypeDefinition)

		val swagger = root.get("swagger").type
		assertEquals("swagger", swagger.containingProperty)
		assertEquals(schema.asJson.at("/properties/swagger".ptr), swagger.getDefinition)

		val info = root.get("info").type
		assertTrue(info instanceof ObjectTypeDefinition)
		assertEquals("info", info.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info".ptr), info.getDefinition)

		val version = root.get("info").get("version").type
		assertEquals("version", version.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info/properties/version".ptr), version.getDefinition)

		val title = root.get("info").get("title").type
		assertEquals("title", title.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info/properties/title".ptr), title.getDefinition)
	}

	@Test
	def void testParseEnumArrayValues() {
		val text = '''
			schemes:
			  - http
			  - https
		'''

		val model = Model.parseYaml(schema, text)
		val root = model.root

		val schemes = root.get("schemes").type
		assertTrue(schemes instanceof ArrayTypeDefinition)
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr), schemes.getDefinition)

		val http = root.get("schemes").get(0).type
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr).get("items"), http.getDefinition)

		val https = root.get("schemes").get(1).type
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr).get("items"), https.getDefinition)
	}

	@Test
	def void testParseArrayValues() {
		val text = '''
			consumes:
			  - application/json
			  - text/xml
		'''

		val model = Model.parseYaml(schema, text)
		val root = model.root

		val consumes = root.get("consumes").type
		assertTrue(consumes instanceof ArrayTypeDefinition)
		assertEquals(schema.asJson.at("/definitions/mediaTypeList".ptr), consumes.getDefinition)

		val http = root.get("consumes").get(0).type
		assertEquals(schema.asJson.at("/definitions/mimeType".ptr), http.getDefinition)
		
		val https = root.get("consumes").get(1).type
		assertEquals(schema.asJson.at("/definitions/mimeType".ptr), https.getDefinition)
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

		val model = Model.parseYaml(schema, text)
		val root = model.root

		val paths = root.get("paths").type
		assertEquals(schema.asJson.at("/definitions/paths".ptr), paths.getDefinition)

		val pathItem = root.get("paths").get("/").type
		assertEquals(schema.asJson.at("/definitions/pathItem".ptr), pathItem.getDefinition)

		val get = root.get("paths").get("/").get("get").type
		assertEquals(schema.asJson.at("/definitions/operation".ptr), get.getDefinition)

		val parameters = root.get("paths").get("/").get("get").get("parameters").type
		assertEquals(schema.asJson.at("/definitions/parametersList".ptr), parameters.getDefinition)

		val param1 = root.get("paths").get("/").get("get").get("parameters").get(0).type
		assertEquals(schema.asJson.at("/definitions/parametersList/items".ptr), param1.getDefinition)
	}

}
