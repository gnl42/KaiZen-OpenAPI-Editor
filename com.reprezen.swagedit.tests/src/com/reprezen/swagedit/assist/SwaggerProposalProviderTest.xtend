package com.reprezen.swagedit.assist

import com.reprezen.swagedit.core.model.Model
import com.reprezen.swagedit.core.schema.JsonType
import com.reprezen.swagedit.core.schema.MultipleTypeDefinition
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.schema.SwaggerSchema
import com.reprezen.swagedit.core.assist.JsonProposalProvider

class SwaggerProposalProviderTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema	
	val provider = new JsonProposalProvider
	var Model model
	
	@Before
	def void setUp() {
		model = Model.empty(schema)
	}

	@Test
	def void testGetProposals_RootObject() {
		val node = model.objectNode(null, "".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map[replacementString], hasItems(
			"swagger:",
			"info:",
			"host:",
			"basePath:",
			"schemes:",
			"consumes:",
			"produces:",
			"paths:",
			"definitions:",
			"parameters:",
			"responses:",
			"security:",
			"securityDefinitions:",
			"tags:",
			"externalDocs:",
			"x-:"
		))
	}

	@Test
	def void testGetProposals_SwaggerEnum() {
		val node = model.valueNode(null, "/swagger".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems("\"2.0\""))
	}

	@Test
	def void testGetProposals_InfoObject() {
		val node = model.objectNode(null, "/info".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"title:",
			"version:",
			"description:",
			"termsOfService:",
			"contact:",
			"license:"
		))
	}

	@Test
	def void testGetProposals_SchemesArray() {
		val node = model.arrayNode(null, "/schemes".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"- http",
			"- https",
			"- ws",
			"- wss"
		) as Matcher<Iterable<String>>)
	}

	@Test
	def void testGetProposals_PathsObject() {
		val node = model.objectNode(null, "/paths".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"/:",
			"x-:"
		))
	}

	@Test
	def void testGetProposals_DefinitionsObject() {
		val node = model.objectNode(null, "/definitions".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"(schema name):"
		))
	}

	@Test
	def void testPathGetProposals() {
		val node = model.objectNode(null, "/paths/~1/get".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"tags:",
			"summary:",
			"description:",
			"externalDocs:",
			"operationId:",
			"produces:",
			"consumes:",
			"parameters:",
			"responses:",
			"schemes:",
			"deprecated:",
			"security:",
			"x-:"
		))
	}

	@Test
	def void testPathParametersProposals() {
		val node = model.arrayNode(null, "/paths/~1/get/parameters".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"-"
		))
	}

	@Test
	def void testParameterInProposals() {
		val node = model.valueNode(null, "/paths/~1/get/parameters/0/in".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"header",
			"path",
			"formData",
			"body",
			"query"
		))
	}

	@Test
	def void testGetOneOfProposals() {
		val node = model.objectNode(null, "/paths/~1/get/responses/200".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"description:",
			"schema:",
			"headers:",
			"examples:",
			"x-:",
			"$ref:"
		))
	}

	@Test
	def void testGetAnyOfProposals() {
		val node = model.valueNode(null, "/paths/~1/get/parameters/0/format".ptr, null)
		node.type = schema.getType(node)

		assertTrue(node.type instanceof MultipleTypeDefinition)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(
			"",
			"byte",
			"double",
			"date-time",
			"float",
			"int32",
			"int64",
			"password",
			"binary",
			"date"
		))
	}

	@Test
	def void testGetParameterRequired() {
		val node = model.objectNode(null, "/parameters/foo".ptr)
		node.type = schema.getType(node)

		assertEquals(JsonType.ONE_OF, node.type.type)

		val values = provider.getProposals(node).map [
			replacementString
		]

		assertEquals(1, values.filter[equals("required:")].size)
	}

	@Test
	def void testGetResponsesType() {
		val node = model.objectNode(null, "/paths/~1foo/get/responses/200/schema/type".ptr)
		node.type = schema.getType(node)

		val values = provider.getProposals(node).map [
			replacementString
		]

		assertEquals(8, values.size)
		assertThat(values, hasItems(
			"array",
			"boolean",
			"integer",
			"\"null\"",
			"number",
			"object",
			"string",
			"file"
		))
	}
}
