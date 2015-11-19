package com.reprezen.swagedit.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.swagedit.assist.SwaggerProposalProvider
import com.reprezen.swagedit.validation.SwaggerSchema
import io.swagger.util.Yaml
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

		val proposals = provider.createProposals(data, definition)
		// should contain all required and non properties
		// should exclude already properties that are already present
		assertEquals(15, proposals.size)
		assertArrayEquals(#[ 
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
		], proposals.map[ it.get("value").asText ])
	}

	@Test
	def void testGetProposalForArrayDefinition() {
		val definition = schema.asJson.get("properties").get("tags")		
		val proposals = provider.createProposals(mapper.createObjectNode, definition)

		assertNotNull(proposals)
		assertEquals(1, proposals.size)
	}

	@Test
	def void testGetProposalForEnumDefinition() {
		val definition = schema.asJson.get("properties").get("swagger")		
		val proposals = provider.createProposals(mapper.createObjectNode, definition)

		assertArrayEquals(#["'2.0'"], 
			proposals.map(it | it.get("value").asText))
	}

	@Test
	def void testInfoProposals() {
		val yaml = '''
		info:
		  description: ""
		  version: "1.0.0"
		'''

		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, schema.asJson.get("definitions").get("info"))

		assertArrayEquals(#[ 
			"title:",
			"version:", 
			"description:",		
			"termsOfService:",
			"contact:",
			"license:",
			"x-:"
		], proposals.map[ it.get("value").asText ])
	}

	@Test
	def void testTagsProposals() {
		val yaml = '''
		tags:
		  - foo: ""
		  - bar: ""
		'''

		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, 
			schema.getDefinitionForPath(":tags")
		)

		assertArrayEquals(#[ 
			"-"
		], proposals.map[ it.get("value").asText ])
	}

	@Test
	def void testPathsProposals() {
		val yaml = '''
		paths:
		'''
		
		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, 
			schema.getDefinitionForPath(":paths")
		)

		assertArrayEquals(#[ 
			"x-:",
			"/:"
		], proposals.map[ it.get("value").asText ])
	}
	
	@Test
	def void testPathGetProposals() {
		val yaml = '''
		paths:
		  /:
		    get:
		'''

		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, 
			schema.getDefinitionForPath(":paths:/:get")
		)

		assertArrayEquals(#[ 
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
		], proposals.map[ it.get("value").asText ])
	}

	@Test
	def void testGetOneOfProposals() {
		val proposals = provider.createProposals(mapper.createObjectNode, 
				schema.asJson.get("definitions").get("responseValue")
		)

		assertArrayEquals(#[ 
			"description:",		
			"schema:",
			"headers:",
			"examples:",
			"x-:",
			"$ref:"
		], proposals.map[ it.get("value").asText ])
	}
	
}
