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
package com.reprezen.swagedit.openapi3.assist

import com.google.common.base.Charsets
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.regex.Pattern
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static org.junit.Assert.*

@RunWith(typeof(Parameterized))
class ReferenceContextTest {

	val static KZOEref = "#KZOE-ref"
	val static testNamePattern = Pattern::compile(".*name=\"([\\w\\s/]+)\".*")
	val static refValuePattern = Pattern::compile(".*value=\"([\\w/]+)\".*")

	@Parameters(name="{index}: {1} - {3}")
	def static Collection<Object[]> data() {
		val resourcesDir = Paths.get("resources", "code-assist", "references").toFile();
		val specFiles = resourcesDir.listFiles(new FilenameFilter() {

			override accept(File dir, String name) {
				name.endsWith(".yaml")
			}

		})
		val Collection<Object[]> result = new ArrayList<Object[]>()
		for (File specFile : specFiles) {
			val fileContents = fileContents(specFile)
			val indices = getAllIndicesOf(fileContents, KZOEref)
			indices.forEach[result.add(#[specFile, specFile.name, it, getTestName(fileContents, it)] as Object[])]

		}
		return result
	}

	@Parameter
	var public File specFile

	@Parameter(1)
	var public String fileName // for test name only
	
	@Parameter(2)
	var public int offset // for test name only
	
	@Parameter(3)
	var public String testName // for test name only

	@Test
	def void test_reference_context() {
		val document = new OpenApi3Document(new OpenApi3Schema())
		val text = specFile.fileContents()
		document.set(text)

		val region = document.getLineInformationOfOffset(offset)
		val line = document.getLineOfOffset(offset)

		val path = document.getModel(offset).getPath(line, document.getColumnOfOffset(line, region))
		val allContextTypes = OpenApi3ReferenceProposalProvider.OPEN_API3_CONTEXT_TYPES
		val contextType = allContextTypes.get(path.toString + "/$ref")

		val annotationLine = document.get(region.offset, region.getLength())
		val matcher = refValuePattern.matcher(annotationLine)
		if (matcher.matches) {
			val String refValue = matcher.group(1);
			assertEquals(refValue, contextType.value);
		} else {
			fail("Invalid test annotation line: " + annotationLine)
		}
	}

	def public static List<Integer> getAllIndicesOf(String str, String substring) {
		val result = newArrayList()
		var index = str.indexOf(substring)
		while (index > 0) {
			result.add(index)
			index = str.indexOf(substring, index + 1)
		}
		return result
	}

	def public static String fileContents(File file) {
		return new String(Files.readAllBytes(Paths.get(file.getPath)), Charsets.UTF_8);
	}
	
	def public static String getTestName(String spec, int offset) {
		val input = spec.substring(offset, spec.indexOf("\n", offset))
		val matcher = testNamePattern.matcher(input)
		if (matcher.matches) {
			return matcher.group(1)
		}
		return "" + offset
	}
}
