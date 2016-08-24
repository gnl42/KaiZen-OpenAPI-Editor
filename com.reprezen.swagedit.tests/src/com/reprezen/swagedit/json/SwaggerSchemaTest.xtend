/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.json

import com.reprezen.swagedit.json.JsonSchemaManager
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.json.SchemaDefinitionProvider

class SwaggerSchemaTest {

	val schema = new JsonSchemaManager().getSchema("swagger").asJson
	val provider = new SchemaDefinitionProvider

	@Test
	def void testTraverse_WithPath_root() {
		val result = provider.getDefinitions("")

		assertThat(result.map[definition], hasItems(schema))
	}

	@Test
	def void testTraverse_With_Path_swagger() {
		val result = provider.getDefinitions("swagger")

		assertThat(result.map[definition], hasItems(
			schema.get("properties").get("swagger")))
	}

	@Test
	def void testTraverse_WithPath_info() {
		val result = provider.getDefinitions("info")

		assertThat(result.map[definition], hasItems(
			schema.get("definitions").get("info")))
	}

	@Test
	def void testTraverse_WithPath_paths() {
		val result = provider.getDefinitions("paths")

		assertThat(result.map[definition], hasItems(
			schema.get("definitions").get("paths"))
		)
	}

	@Test
	def void testTraverse_WithPath_paths_slash() {
		val result = provider.getDefinitions("paths:/")

		assertThat(result.map[definition], hasItems(
			schema.get("definitions").get("pathItem")
		))
	}

	@Test
	def void testTraverse_With_responseValue() {
		val result = provider.getDefinitions("paths:/:get:responses:200")

		assertThat(result.map[definition], hasItems(
				schema.get("definitions").get("responseValue")))
	}

	@Test
	def void testTraverse_With_parameter_in() {
		val result = provider.getDefinitions("paths:/:get:parameters:@1:in")

		assertThat(result.map[definition], hasItems(
				schema.get("definitions").get("bodyParameter").get("properties").get("in"),
				schema.get("definitions").get("headerParameterSubSchema").get("properties").get("in"),
				schema.get("definitions").get("formDataParameterSubSchema").get("properties").get("in"),
				schema.get("definitions").get("queryParameterSubSchema").get("properties").get("in"),
				schema.get("definitions").get("pathParameterSubSchema").get("properties").get("in")))
	}

	@Test
	def void testTraverse_With_parameter_required() {
		val result = provider.getDefinitions("paths:/:get:parameters:@1:required")

		assertThat(result.map[definition], hasItems(
				schema.get("definitions").get("bodyParameter").get("properties").get("required"),
				schema.get("definitions").get("pathParameterSubSchema").get("properties").get("required")))
	}

	@Test
	def void testTraverse_external_refs() {
		val walker = new SchemaDefinitionProvider
		val result = walker.getDefinitions("definitions:foo:type")		
		
		println(result.map[definition])		
	}

}

