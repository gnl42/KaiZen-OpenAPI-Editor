package com.reprezen.swagedit.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.swagedit.assist.SwaggerProposalProvider
import com.reprezen.swagedit.validation.SwaggerSchema
import org.junit.Test

import static org.junit.Assert.*

class SwaggerProposalProviderTest {

	val mapper = new ObjectMapper
	val schema = new SwaggerSchema
	val provider = new SwaggerProposalProvider

	@Test
	def void testProviderProposalForRootDefinition() {
		val definition = schema.asJson
		val data = mapper.createObjectNode
				.set("swagger", null)

		val proposal = provider.get(definition, data, definition)

		assertEquals("object", proposal.get(SwaggerProposalProvider.TYPE).asText)
		// should contain all required and non properties
		// should exclude already properties that are already present 
	}

	@Test
	def void testGetProposalForArrayDefinition() {
		val definition = schema.asJson.get("properties").get("tags")		
		val proposal = provider.get(schema.asJson, mapper.createObjectNode, definition)

		assertNotNull(proposal)
		assertEquals("array", proposal.get(SwaggerProposalProvider.TYPE).asText)
	}

	@Test
	def void testGetProposalForEnumDefinition() {
		val definition = schema.asJson.get("properties").get("swagger")		
		val proposal = provider.get(schema.asJson, mapper.createObjectNode, definition)

		assertNotNull(proposal)
		assertEquals("enum", proposal.get(SwaggerProposalProvider.TYPE).asText)
		assertArrayEquals(#["2.0"], proposal.get("literals").map(it | it.asText))
	}

}
