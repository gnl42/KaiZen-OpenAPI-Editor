package com.reprezen.swagedit.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.reprezen.swagedit.core.schema.JsonSchemaUtils
import com.reprezen.swagedit.schema.SwaggerSchema
import java.util.List
import org.junit.Test

import static org.junit.Assert.*

class MultipleSwaggerErrorMessageTest {

	@Test
	def void testAnyOf() throws Exception {
		testCombinedSchemas("anyOf")
	}

	@Test
	def void testOneOf() throws Exception {
		testCombinedSchemas("oneOf")
	}

	@Test
	def void testAllOf() throws Exception {
		testCombinedSchemas("allOf")
	}

	@Test
	def void testArrayOfSchemas() throws Exception {
		val String defaultValue = "MY CRAZY DEFAULT VALUE FOR TESTS"
		'''{
		          "type": "array",
		          "minItems": 1,
«««		          a normal $ref
		          "items": 
		          {
		            "$ref": "#/definitions/schema"
		          }
		        }'''.assertHumanFriendlyTextForNodeEquals("schema", defaultValue)
		// test the case where the value of items is not a schema, but an array of schemas 
		// as described in https://github.com/ModelSolv/SwagEdit/commit/dc1e222ec9be165b4f7774369bb7f168b6e940c1#commitcomment-16579761
		'''{
		          "type": "array",
		          "minItems": 1,
«««		          note that we use an array here
		          "items": [
		          {
		            "$ref": "#/definitions/schema" 
		          }, {
		          	"$ref": "#/definitions/value2"
		          }
		          ]
		        }'''.assertHumanFriendlyTextForNodeEquals(defaultValue, defaultValue)
	}

	@Test
	def void testArrayOfArraysSchema() throws Exception {
		val String defaultValue = "MY CRAZY DEFAULT VALUE FOR TESTS"
		'''{
  "type": "array",
  "minItems": 1,
  "items": {
    "type": "array",
    "minItems": 1,
    "items": {
      "$ref": "#/definitions/foo"
    }
  }
}'''.assertHumanFriendlyTextForNodeEquals("foo", defaultValue)

	}

	def void assertHumanFriendlyTextForNodeEquals(CharSequence json, String expectedLabel, String defaultValue) {
		val JsonNode arrayOfSchemasNode = new ObjectMapper().readTree(json.toString);
		assertNotNull(arrayOfSchemasNode)
		val label = JsonSchemaUtils::getHumanFriendlyText(arrayOfSchemasNode, defaultValue);
		assertEquals(expectedLabel, label)
	}

	def void testCombinedSchemas(String propertyName) throws Exception {
		val JsonNode swaggerSchema = new SwaggerSchema().asJson
		val List<JsonNode> combinedSchemas = newArrayList();
		// oneOf and anyOf are usually ArrayNodes
		swaggerSchema.findValues(propertyName).forEach [
			if (it instanceof ArrayNode) {
				combinedSchemas.addAll(Lists.newArrayList(it.elements))
			} else {
				combinedSchemas.add(it)
			}
		];
		assertFalse(combinedSchemas.filterNull.isNullOrEmpty)

		val emptyLabels = combinedSchemas.filter[it|Strings.isNullOrEmpty(JsonSchemaUtils::getHumanFriendlyText(it, null))]
		assertTrue("Null labels are not expected, but got null for the following nodes: " + emptyLabels,
			emptyLabels.isNullOrEmpty)
	}
}
