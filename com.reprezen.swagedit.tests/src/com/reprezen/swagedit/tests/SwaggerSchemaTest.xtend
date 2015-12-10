package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.SwaggerSchema
import org.junit.Test

import static org.junit.Assert.*
import static org.hamcrest.core.IsCollectionContaining.*;

class SwaggerSchemaTest {

	val schema = new SwaggerSchema()

	@Test
	def void testTraverse_WithPath_root() {
		val result = schema.getDefinitions("")

		assertThat(result, hasItems(schema.asJson))
	}

	@Test
	def void testTraverse_With_Path_swagger() {
		val result = schema.getDefinitions("swagger")

		assertThat(result, hasItems(
			schema.asJson.get("properties").get("swagger")))
	}

	@Test
	def void testTraverse_WithPath_info() {
		val result = schema.getDefinitions("info")

		assertThat(result, hasItems(
			schema.asJson.get("definitions").get("info")))
	}

	@Test
	def void testTraverse_WithPath_paths() {
		val result = schema.getDefinitions("paths")

		assertThat(result, hasItems(
			schema.asJson.get("definitions").get("paths"))
		)
	}

	@Test
	def void testTraverse_WithPath_paths_slash() {
		val result = schema.getDefinitions("paths:/")

		assertThat(result, hasItems(
			schema.asJson.get("definitions").get("pathItem")
		))
	}

	@Test
	def void testTraverse_With_responseValue() {
		val result = schema.getDefinitions("paths:/:get:responses:200")

		assertThat(result, hasItems(
				schema.asJson.get("definitions").get("responseValue")))
	}

	@Test
	def void testTraverse_With_parameter_in() {
		val result = schema.getDefinitions("paths:/:get:parameters:@1:in")

		assertThat(result, hasItems(
				schema.asJson.get("definitions").get("bodyParameter").get("properties").get("in"),
				schema.asJson.get("definitions").get("headerParameterSubSchema").get("properties").get("in"),
				schema.asJson.get("definitions").get("formDataParameterSubSchema").get("properties").get("in"),
				schema.asJson.get("definitions").get("queryParameterSubSchema").get("properties").get("in"),
				schema.asJson.get("definitions").get("pathParameterSubSchema").get("properties").get("in")))
	}

	@Test
	def void testTraverse_With_parameter_required() {
		val result = schema.getDefinitions("paths:/:get:parameters:@1:required")

		assertThat(result, hasItems(
				schema.asJson.get("definitions").get("bodyParameter").get("properties").get("required"),
				schema.asJson.get("definitions").get("pathParameterSubSchema").get("properties").get("required")))
	}

}

