package com.reprezen.swagedit.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.reprezen.swagedit.json.JsonSchemaManager
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

	def void testCombinedSchemas(String propertyName) throws Exception {
		val JsonNode swaggerSchema = new JsonSchemaManager().getSwaggerSchema().asJson();
		val swaggerError = new SwaggerError.MultipleSwaggerError(0, 0);
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
		// println("All nodes: " + altValues)
		// println("All labels: " + altValues.map[it|swaggerError.getHumanFriendlyText(it, null)])
		val emptyLabels = combinedSchemas.filter[it|Strings.isNullOrEmpty(swaggerError.getHumanFriendlyText(it, null))]
		assertTrue("Null labels are not expected, but got null for the following nodes: " + emptyLabels,
			emptyLabels.isNullOrEmpty)
	}
}