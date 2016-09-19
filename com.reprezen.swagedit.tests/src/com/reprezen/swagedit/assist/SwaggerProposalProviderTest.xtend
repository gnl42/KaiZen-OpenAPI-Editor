package com.reprezen.swagedit.assist

import com.reprezen.swagedit.json.SwaggerSchema
import com.reprezen.swagedit.model.ArrayNode
import com.reprezen.swagedit.model.ObjectNode
import com.reprezen.swagedit.model.ValueNode
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*

class SwaggerProposalProviderTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema
	val provider = new SwaggerProposalProvider

	@Test
	def void testGetProposals_RootObject() {
		val node = new ObjectNode(null, "".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map[replacementString], hasItems(#[
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
		val node = new ValueNode(null, "/swagger".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(#["\"2.0\""]))
	}

	@Test
	def void testGetProposals_InfoObject() {
		val node = new ObjectNode(null, "/info".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
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
		val node = new ArrayNode(null, "/schemes".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(#[
			"- http",
			"- https",
			"- ws",
			"- wss"
		]))
	}

	@Test
	def void testGetProposals_PathsObject() {
		val node = new ObjectNode(null, "/paths".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(#[
			"/:",
			"x-:"
		]))
	}

	@Test
	def void testGetProposals_DefinitionsObject() {
		val node = new ObjectNode(null, "/definitions".ptr, null)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(#[
			"_key_:"
		]))
	}

	@Test
	def void testPathGetProposals() {
		val node = new ObjectNode(null, "/paths/~1/get".ptr, null) 
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
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
	def void testPathParametersProposals() {
		val node = new ArrayNode(null, "/paths/~1/get/parameters".ptr, null) 
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
		], hasItems(#[
			"-"
		]))
	}

	@Test
	def void testGetOneOfProposals() {
		val node = new ObjectNode(null, "/paths/~1/get/responses/200".ptr)
		node.type = schema.getType(node)

		assertThat(provider.getProposals(node).map [
			replacementString
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
