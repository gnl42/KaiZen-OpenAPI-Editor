package com.reprezen.swagedit.model

import com.reprezen.swagedit.core.json.JsonModel
import com.reprezen.swagedit.core.schema.JsonType
import com.reprezen.swagedit.core.schema.ObjectTypeDefinition
import com.reprezen.swagedit.core.schema.ReferenceTypeDefinition
import com.reprezen.swagedit.schema.SwaggerSchema
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.junit.Test

import static org.junit.Assert.*

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

		val model = new JsonModel(schema, text, false)

		assertTrue(model.getType("".ptr) instanceof ObjectTypeDefinition)

		val swagger = model.getType("/swagger".ptr)
		assertEquals("swagger", swagger.containingProperty)
		assertEquals(schema.asJson.at("/properties/swagger".ptr), swagger.asJson)

		val info = model.getType("/info".ptr)
		assertTrue(info instanceof ReferenceTypeDefinition)
		assertEquals("info", info.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info".ptr), info.asJson)

		val version = model.getType("/info/version".ptr)
		assertEquals("version", version.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info/properties/version".ptr), version.asJson)

		val title = model.getType("/info/title".ptr)
		assertEquals("title", title.containingProperty)
		assertEquals(schema.asJson.at("/definitions/info/properties/title".ptr), title.asJson)
	}

	@Test
	def void testParseEnumArrayValues() {
		val text = '''
			schemes:
			  - http
			  - https
			foo:
			  bar: hello
		'''

		val model = new JsonModel(schema, text, false)

		val schemes = model.getType("/schemes".ptr)
		assertTrue(schemes instanceof ReferenceTypeDefinition)

		assertEquals(JsonType.ARRAY, schemes.getType)
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr), schemes.asJson)

		val http = model.getType("/schemes/0".ptr)
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr).get("items"), http.asJson)

		val https = model.getType("/schemes/1".ptr)
		println(https)
		assertEquals(schema.asJson.at("/definitions/schemesList".ptr).get("items"), https.asJson)
	}

	@Test
	def void testParseArrayValues() {
		val text = '''
			consumes:
			  - application/json
			  - text/xml
		'''

		val model = new JsonModel(schema, text, false)

		val consumes = model.getType("/consumes".ptr)
		assertTrue(consumes instanceof ReferenceTypeDefinition)
		assertEquals(JsonType.ARRAY, consumes.type)
		assertEquals(schema.asJson.at("/definitions/mediaTypeList".ptr), consumes.asJson)

		val http = model.getType("/consumes/0".ptr)
		assertEquals(schema.asJson.at("/definitions/mimeType".ptr), http.asJson)

		val https = model.getType("/consumes/1".ptr)
		assertEquals(schema.asJson.at("/definitions/mimeType".ptr), https.asJson)
	}

	@Test
	def void testParsePathParameters_OneOfType() {
		val text = '''
			paths:
			  /pets:
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

		val model = new JsonModel(schema, text, false)

		val paths = model.getType("/paths".ptr)
		assertEquals(schema.asJson.at("/definitions/paths".ptr), paths.asJson)

		val pathItem = model.getType("/paths/~1pets".ptr)
		assertEquals(schema.asJson.at("/definitions/pathItem".ptr), pathItem.asJson)

		val get = model.getType("/paths/~1pets/get".ptr)
		assertEquals(schema.asJson.at("/definitions/operation".ptr), get.asJson)

		val parameters = model.getType("/paths/~1pets/get/parameters".ptr)
		assertEquals(schema.asJson.at("/definitions/parametersList".ptr), parameters.asJson)

		val param1 = model.getType("/paths/~1pets/get/parameters/0".ptr)
		assertEquals(schema.asJson.at("/definitions/parametersList/items".ptr), param1.asJson)
	}

}
