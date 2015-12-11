package com.reprezen.swagedit.tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.swagedit.assist.SwaggerProposalProvider
import com.reprezen.swagedit.json.JsonSchemaManager
import com.reprezen.swagedit.json.SchemaDefinition
import io.swagger.util.Yaml
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.json.SchemaDefinitionProvider

class SwaggerProposalProviderTest {

	val mapper = new ObjectMapper
	val schema = new JsonSchemaManager().getSchema("swagger").asJson
	val definitionProvider = new SchemaDefinitionProvider
	val provider = new SwaggerProposalProvider

	@Test
	def void testProviderProposalForRootDefinition() {
		val definition = new SchemaDefinition(schema, schema)
		val data = mapper.createObjectNode
				.set("swagger", null)

		val proposals = provider.createProposals(data, definition)
		// should contain all required and non properties
		// should exclude already properties that are already present

		assertEquals(15, proposals.map[ it.get("value").asText ].size) 
		assertThat(proposals.map[ it.get("value").asText ], hasItems(
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
	def void testGetProposalForArrayDefinition() {
		val definition = new SchemaDefinition(
			schema,
			schema.get("properties").get("tags"))
		val proposals = provider.createProposals(mapper.createObjectNode, definition)

		assertNotNull(proposals)
		assertEquals(1, proposals.size)
	}

	@Test
	def void testGetProposalForEnumDefinition() {
		val definition = new SchemaDefinition(
			schema,
			schema.get("properties").get("swagger")
		)
		val proposals = provider.createProposals(mapper.createObjectNode, definition)

		assertThat(proposals.map[ it.get("value").asText ], hasItems("\"2.0\"")) 
	}

	@Test
	def void testInfoProposals() {
		val yaml = '''
		info:
		  description: ""
		  version: "1.0.0"
		'''

		val node = Yaml.mapper.readTree(yaml)
		val definition = new SchemaDefinition(
			schema,
			schema.get("definitions").get("info")
		)
		val proposals = provider.createProposals(node, definition)

		assertThat(proposals.map[ it.get("value").asText ], hasItems(
			"title:",
			"version:", 
			"description:",		
			"termsOfService:",
			"contact:",
			"license:",
			"x-:"))
	}

	@Test
	def void testTagsProposals() {
		val yaml = '''
		tags:
		  - foo: ""
		  - bar: ""
		'''

		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, definitionProvider.getDefinitions(":tags"))

		assertThat(proposals.map[ it.get("value").asText ], hasItems("-"))
	}

	@Test
	def void testPathsProposals() {
		val yaml = '''
		paths:
		'''
		
		val node = Yaml.mapper.readTree(yaml)
		val proposals = provider.createProposals(node, 
			definitionProvider.getDefinitions(":paths")
		)

		assertThat(proposals.map[ it.get("value").asText ], hasItems("x-:", "/:"))
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
			definitionProvider.getDefinitions(":paths:/:get")
		)

		assertThat(proposals.map[ it.get("value").asText ], hasItems(
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
			"x-:"))
	}

	@Test
	def void testGetOneOfProposals() {
		val proposals = provider.createProposals(mapper.createObjectNode,
			new SchemaDefinition(
				schema,
				schema.get("definitions").get("responseValue")
			)
		)

		assertThat(proposals.map[ it.get("value").asText ], hasItems(		
			"description:",
			"schema:",
			"headers:",
			"examples:",
			"x-:",
			"$ref:"))
	}

	@Test
	def void testGetAnyOfProposals() {
		val proposals = provider.createProposals(
			mapper.createObjectNode, 
			definitionProvider.getDefinitions(":definitions:foo:type")
		)

		println(proposals)
		println(proposals.map[it.get("value")])	
	}

}
