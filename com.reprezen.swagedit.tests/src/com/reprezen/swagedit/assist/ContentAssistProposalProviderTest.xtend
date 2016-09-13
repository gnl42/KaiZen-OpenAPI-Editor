package com.reprezen.swagedit.assist

import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.json.SwaggerSchema
import com.reprezen.swagedit.model.ArrayNode
import com.reprezen.swagedit.model.ObjectNode
import com.reprezen.swagedit.model.ValueNode
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.json.TypeDefinition

class ContentAssistProposalProviderTest {

	val schema = new SwaggerSchema

	@Test
	def void testGetProposals_RootObject() {
		val node = new ObjectNode(null, JsonPointer.compile(""))
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile(""))
		node.type = type

		assertThat(type.getProposals(node).map[get("value").asText], hasItems(#[
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
		]))
	}

	@Test
	def void testGetProposals_SwaggerEnum() {
		val node = new ValueNode(null, JsonPointer.compile("/swagger"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/properties/swagger"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#["\"2.0\""]))
	}

	@Test
	def void testGetProposals_InfoObject() {
		val node = new ObjectNode(null, JsonPointer.compile("/info"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/info"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
			"title:",
			"version:",
			"description:",
			"termsOfService:",
			"contact:",
			"license:"
		]))
	}

	@Test
	def void testGetProposals_SchemesArray() {
		val node = new ArrayNode(null, JsonPointer.compile("/info"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/schemesList"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
			"- http",
			"- https",
			"- ws",
			"- wss"
		]))
	}

	@Test
	def void testGetProposals_PathsObject() {
		val node = new ObjectNode(null, JsonPointer.compile("/paths"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/paths"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
			"/:",
			"x-:"
		]))
	}

	@Test
	def void testGetProposals_DefinitionsObject() {
		val node = new ObjectNode(null, JsonPointer.compile("/definitions"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/definitions"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
			"_key_:"
		]))
	}

	@Test
	def void testPathGetProposals() {
		val node = new ObjectNode(null, JsonPointer.compile("/paths/~1/get"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/operation"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
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
		]))
	}

	@Test
	def void testGetOneOfProposals() {
		val node = new ObjectNode(null, JsonPointer.compile("/paths/~1/get/responses/200"), null)
		val type = TypeDefinition.create(schema.asJson, JsonPointer.compile("/definitions/responseValue"))
		node.type = type

		assertThat(type.getProposals(node).map [
			get("value").asText
		], hasItems(#[
			"description:",
			"schema:",
			"headers:",
			"examples:",
			"x-:",
			"$ref:"
		]))
	}

	@Test
	def void testGetAnyOfProposals() {
//		val proposals = provider.createProposals(
//			mapper.createObjectNode,
//			definitionProvider.getDefinitions(":definitions:foo:type")
//		)
//
//		println(proposals)
//		println(proposals.map[it.get("value")])
	}
}
