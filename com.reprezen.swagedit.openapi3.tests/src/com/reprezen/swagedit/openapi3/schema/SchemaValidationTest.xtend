package com.reprezen.swagedit.openapi3.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.google.common.collect.Iterators
import com.google.common.collect.Lists
import com.reprezen.swagedit.core.validation.ErrorProcessor
import com.reprezen.swagedit.core.validation.SwaggerError
import com.reprezen.swagedit.core.validation.Validator
import com.reprezen.swagedit.openapi3.Activator
import java.net.URL
import java.util.Collection
import java.util.Iterator
import java.util.Set
import org.eclipse.core.runtime.Platform
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static org.junit.Assert.*
import org.junit.Test

@RunWith(typeof(Parameterized))
class SchemaValidationTest {

	@Parameters(name = "{index}: {1}")
	def static Collection<Object[]> data() {
		val Iterator<URL> specFiles = Iterators::forEnumeration(
			Platform.getBundle("com.reprezen.swagedit.openapi3.tests").findEntries("/resources/", "*.yaml",
				true))
		return Lists.<Object[]>newArrayList(specFiles.map[#[it, it.file] as Object[]])
	}

	@Parameter
	var public URL specFileURL

	@Parameter(1)
	var public String fileName // for test name only

	val mapper = new YAMLMapper()

	@Test
	def public validateSpec() {
		validate(specFileURL)
	}

	def protected void validate(URL specUrl) {
		validate(mapper.readTree(specUrl.openStream))
	}

	def protected void validate(JsonNode documentAsJson) {
		val JsonNode schemaAsJson = Activator.getDefault().getSchema().asJson()
		val ErrorProcessor processor = new ErrorProcessor(null, null) {
			override protected Set<SwaggerError> fromNode(JsonNode error, int indent) {
				fail('''JSON Schema validation error: «error.asText()»''')
				return super.fromNode(error, indent)
			}
		}
		new Validator().validateAgainstSchema(processor, schemaAsJson, documentAsJson)
	}
}
