package com.reprezen.swagedit.openapi3.schema

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.google.common.collect.Lists
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.nio.file.Paths
import java.util.Collection
import java.util.List
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters
import com.reprezen.swagedit.openapi3.validation.ValidationHelper

@RunWith(typeof(Parameterized))
class SchemaValidationTest {
	
	extension ValidationHelper validationHelper = new ValidationHelper

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
		validateFile(specFile)
	}

	def protected void validateFile(File specFile) {
		validate(mapper.readTree(specFile))
	}

	def protected static void getAllFolders(File dir, List<File> acc) {
		if (!dir.isDirectory) {
			return;
		}
		acc.add(dir);
		val nested = dir.listFiles(new FileFilter() {
			
			override accept(File pathname) {
				return dir.directory
			}
			
		})
		nested.forEach[getAllFolders(it, acc)]
		return;
	}
}
