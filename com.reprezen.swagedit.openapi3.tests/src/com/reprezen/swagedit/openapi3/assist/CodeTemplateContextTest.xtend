/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.assist

import java.io.File
import java.nio.file.Paths
import java.util.Collection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static com.reprezen.swagedit.openapi3.assist.CodeAssistHelper.*
import static org.junit.Assert.*

@RunWith(typeof(Parameterized))
class CodeTemplateContextTest {
	
	val static KZOEref = "#KZOE-template"
	
	@Parameter
	var File specFile

	@Parameter(1)
	var String fileName // for test name only
	
	@Parameter(2)
	var int offset // for test name only
	
	@Parameter(3)
	var String testName // for test name only
	

	@Parameters(name="{index}: {1} - {3}")
	def static Collection<Object[]> data() {
		val resourcesDir = Paths.get("resources", "code-assist", "code-templates").toFile();
		return new CodeAssistHelper().extractTests(resourcesDir, KZOEref).map[#[it.file, it.file.name, it.offset, it.name] as Object[]]
	}

	@Test
	def void test_code_template_context() {
		val document = createOpenApi3Document(specFile)

		val region = document.getLineInformationOfOffset(offset)

		val contextType = getCodeTemplateContext(document, offset)

		val annotationLine = document.get(region.offset, region.getLength())
		val matcher = refValuePattern.matcher(annotationLine)
		if (matcher.matches) {
			val String refValue = matcher.group(1);
			assertNotNull("Code-template context is null, but expected: " + refValue, contextType)
			assertEquals(refValue, contextType.name);
		} else {
			fail("Invalid test annotation line: " + annotationLine)
		}
	}

}
