package com.reprezen.swagedit.assist

import com.reprezen.swagedit.core.schema.JsonType
import com.reprezen.swagedit.core.schema.MultipleTypeDefinition
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.hamcrest.Matcher
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.schema.SwaggerSchema
import com.reprezen.swagedit.core.assist.JsonProposalProvider
import com.reprezen.swagedit.core.json.JsonModel

class SwaggerProposalProviderTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema
	val provider = new JsonProposalProvider

	@Test
	def void testGetProposals_RootObject() {
		val model = new JsonModel(schema, "", false)

		assertThat(provider.getProposals("".ptr, model, "").map[replacementString], hasItems(
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
		val model = new JsonModel(schema, "swagger: ", false)

		assertThat(provider.getProposals("/swagger".ptr, model, "").map [
			replacementString
		], hasItems("\"2.0\""))
	}

	@Test
	def void testGetProposals_InfoObject() {
		val model = new JsonModel(schema, "info: ", false)

		assertThat(provider.getProposals("/info".ptr, model, "").map [
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
		val model = new JsonModel(schema, "schemes: ", false)

		assertThat(provider.getProposals("/schemes".ptr, model, "").map [
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
		val model = new JsonModel(schema, "paths: ", false)

		assertThat(provider.getProposals("/paths".ptr, model, "").map [
			replacementString
		], hasItems(
			"/:",
			"x-:"
		))
	}

	@Test
	def void testGetProposals_DefinitionsObject() {
		val model = new JsonModel(schema, "definitions: ", false)

		assertThat(provider.getProposals("/definitions".ptr, model, "").map [
			replacementString
		], hasItems(
			"(schema name):"
		))
	}

	@Test
	def void testGetProposals_SchemaPropertiesObject() {
		val model = new JsonModel(schema, '''
			definitions:
			  MyType:
			    properties:
		''', false)

		assertThat(provider.getProposals("/definitions/MyType/properties".ptr, model, "").map [
			replacementString
		], hasItems(
			"(property name):"
		))
	}

	@Test
	def void testPathGetProposals() {
		val model = new JsonModel(schema, '''
			paths:
			  /:
			    get:
		''', false)

		assertThat(provider.getProposals("/paths/~1/get".ptr, model, "").map [
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
		val model = new JsonModel(schema, '''
			paths:
			  /:
			    get:
			      parameters:
		''', false)

		assertThat(provider.getProposals("/paths/~1/get/parameters".ptr, model, "").map [
			replacementString
		], hasItems(
			"-"
		))
	}

	@Test
	def void testParameterInProposals() {
		val model = new JsonModel(schema, '''
			paths:
			  /:
			    get:
			      parameters:
			        - in:
		''', false)

		assertThat(provider.getProposals("/paths/~1/get/parameters/0/in".ptr, model, "").map [
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
		val model = new JsonModel(schema, '''
			paths:
			  /:
			    get:
			      responses:
			        200:
		''', false)

		assertThat(provider.getProposals("/paths/~1/get/responses/200".ptr, model, "").map [
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
		val model = new JsonModel(schema, '''
			paths:
			  /:
			    get:
			      parameters:
			        - format:
		''', false)

		assertTrue(model.getType("/paths/~1/get/parameters/0/format".ptr) instanceof MultipleTypeDefinition)

		assertThat(provider.getProposals("/paths/~1/get/parameters/0/format".ptr, model, "").map [
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
		val model = new JsonModel(schema, '''
			parameters:
			  foo:
		''', false)

		assertEquals(JsonType.ONE_OF, model.getType("/parameters/foo".ptr).type)

		val values = provider.getProposals("/parameters/foo".ptr, model, "").map [
			replacementString
		]

		assertEquals(1, values.filter[equals("required:")].size)
	}

	@Test
	def void testGetResponsesType() {
		val model = new JsonModel(schema, '''
			paths:
			  /foo:
			    get:
			      responses:
			        200:
			          schema:
			            type:
		''', false)

		val values = provider.getProposals("/paths/~1foo/get/responses/200/schema/type".ptr, model, "").map [
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
