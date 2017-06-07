package com.reprezen.swagedit.openapi3.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator
import com.reprezen.swagedit.core.validation.ErrorProcessor
import com.reprezen.swagedit.core.validation.SwaggerError
import com.reprezen.swagedit.core.validation.Validator
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Paths
import java.util.Collection
import java.util.Map
import java.util.Set
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static org.junit.Assert.*
import java.util.List
import java.io.FileFilter

@RunWith(typeof(Parameterized))
class SchemaValidationTest {

	@Parameters(name = "{index}: {1}")
	def static Collection<Object[]> data() {
		val resourcesDir = Paths.get("resources").toFile();
		val nestedResourcesDirs = newArrayList();
		getAllFolders(resourcesDir, nestedResourcesDirs);
		val specFiles = nestedResourcesDirs.map[it.listFiles(new FilenameFilter() {
			
			override accept(File dir, String name) {
				name.endsWith(".yaml")
			}

		}) as List<File>].flatten;
		// File.toString shows relative path while File.getName only file name
		return Lists.<Object[]>newArrayList(specFiles.map[#[it, it.toString] as Object[]])
	}

	@Parameter
	var public File specFile

	@Parameter(1)
	var public String fileName // for test name only

	val mapper = new YAMLMapper()

	@Test
	def public validateSpec() {
		validate(specFile)
	}

	def protected void validate(File specFile) {
		validate(mapper.readTree(specFile))
	}

	def protected void validate(JsonNode documentAsJson) {
		val JsonNode schemaAsJson = getSchema().asJson()
		val ErrorProcessor processor = new ErrorProcessor(null, null) {
			override protected Set<SwaggerError> fromNode(JsonNode error, int indent) {
				fail('''JSON Schema validation error: «error.asText()»''')
				return super.fromNode(error, indent)
			}
		}
		val Map<String, JsonNode> preloadedSchemas = Maps.newHashMap();
		preloadedSchemas.put("http://openapis.org/v3/schema.json", getSchema().getRootType().asJson());
		new Validator(new JsonReferenceValidator(new JsonReferenceFactory()), preloadedSchemas).
			validateAgainstSchema(processor, schemaAsJson, documentAsJson)
	}
	
	def protected getSchema() {
		return new OpenApi3Schema();
	}
	
	def protected static getAllFolders(File dir, List<File> acc) {
		if (!dir.isDirectory) {
			return acc;
		}
		acc.add(dir);
		val nested = dir.listFiles(new FileFilter() {
			
			override accept(File pathname) {
				return dir.directory
			}
			
		})
		nested.forEach[getAllFolders(it, acc)]
		return acc;
	}
}
