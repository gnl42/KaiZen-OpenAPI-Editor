package com.reprezen.swagedit.schema

import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*

class SwaggerSchemaTest {

	val schema = new SwaggerSchema

	@Test
	def void testGetCoreTypes() {
		var type = schema.getType("http://json-schema.org/draft-04/schema#")
		assertTrue(type instanceof ObjectTypeDefinition)
		assertTrue(type.getType == JsonType.OBJECT)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/schemaArray")
		assertTrue(type instanceof ArrayTypeDefinition)
		assertTrue(type.getType == JsonType.ARRAY)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/positiveInteger")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.INTEGER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/positiveIntegerDefault0")
		assertTrue(type instanceof ComplexTypeDefinition)
		assertTrue(type.getType == JsonType.ALL_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/simpleTypes")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ENUM)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/stringArray")
		assertTrue(type instanceof ArrayTypeDefinition)
		assertTrue(type.getType == JsonType.ARRAY)

		type = schema.getType("http://json-schema.org/draft-04/schema#/definitions/stringArray")
		assertTrue(type instanceof ArrayTypeDefinition)
		assertTrue(type.getType == JsonType.ARRAY)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/id")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.STRING)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/$schema")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.STRING)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/title")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.STRING)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/description")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.STRING)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/default")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.UNDEFINED)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/multipleOf")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.NUMBER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/maximum")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.NUMBER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/exclusiveMaximum")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.BOOLEAN)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/minimum")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.NUMBER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/exclusiveMinimum")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.BOOLEAN)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/maxLength")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.INTEGER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/minLength")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ALL_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/pattern")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.STRING)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/additionalItems")
		assertTrue(type instanceof ComplexTypeDefinition)
		assertTrue(type.getType == JsonType.ANY_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/items")
		assertTrue(type instanceof ComplexTypeDefinition)
		assertTrue(type.getType == JsonType.ANY_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/maxItems")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.INTEGER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/minItems")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ALL_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/uniqueItems")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.BOOLEAN)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/maxProperties")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.INTEGER)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/minProperties")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ALL_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/required")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ARRAY)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/additionalProperties")
		assertTrue(type instanceof TypeDefinition)
		assertTrue(type.getType == JsonType.ANY_OF)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/definitions")
		assertTrue(type instanceof ObjectTypeDefinition)
		assertTrue(type.getType == JsonType.OBJECT)

		type = schema.getType("http://json-schema.org/draft-04/schema#/properties/definitions")
		assertTrue(type instanceof ObjectTypeDefinition)
		assertTrue(type.getType == JsonType.OBJECT)
	}

	@Test
	def void testRootType() {
		assertNotNull(schema.rootType)

		val rootType = schema.rootType
		assertEquals(schema.asJson, rootType.content)
		assertTrue(rootType instanceof ObjectTypeDefinition)

		assertThat(
			(rootType as ObjectTypeDefinition).requiredProperties,
			hasItems("swagger", "info", "paths")
		)
	}

	@Test
	def void testSwaggerType() {
		val swaggerType = schema.rootType.getPropertyType("swagger")

		assertTrue(swaggerType instanceof TypeDefinition)
		assertEquals(schema.asJson.at('/properties/swagger'), swaggerType.content)
	}

	@Test
	def void testInfoType() {
		val infoType = schema.getType("/definitions/info")
		assertTrue(infoType instanceof ObjectTypeDefinition)

		assertThat(
			(infoType as ObjectTypeDefinition).requiredProperties,
			hasItems("version", "title")
		)

		val titleType = infoType.getPropertyType("title")
		assertTrue(titleType instanceof TypeDefinition)
		assertEquals(schema.getType("/definitions/info/properties/title"), titleType)
	}

	@Test
	def void testPathType() {
		val pathType = schema.getType("/definitions/paths")

		assertTrue(pathType instanceof ObjectTypeDefinition)
	}

	@Test
	def void testParameterType() {
		val parametersType = schema.getType("/definitions/parametersList")

		assertTrue(parametersType instanceof ArrayTypeDefinition)
	}

	@Test
	def void testParameterItems() {
		val type = schema.getType("/definitions/parametersList/items")
		assertTrue(type instanceof ComplexTypeDefinition)

		val complexType = type as ComplexTypeDefinition
		assertEquals(2, complexType.complexTypes.size)

		val first = complexType.complexTypes.get(0)
		assertTrue(first instanceof ReferenceTypeDefinition)

		val first_resolved = (first as ReferenceTypeDefinition).resolve
		assertTrue(first_resolved instanceof ComplexTypeDefinition)
		assertEquals(2, (first_resolved as ComplexTypeDefinition).complexTypes.size)

		val second = complexType.complexTypes.get(1)
		assertTrue(second instanceof ReferenceTypeDefinition)
	}
}
