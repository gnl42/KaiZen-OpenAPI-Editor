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

import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.nio.file.Paths
import java.util.Collection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import static org.junit.Assert.*
import com.reprezen.swagedit.openapi3.templates.OpenApi3ContextType

@RunWith(typeof(Parameterized))
class CodeTemplateContextTest extends CodeAssistContextTest{

	val static KZOEref = "#KZOE-template"

	@Parameters(name="{index}: {1} - {3}")
	def static Collection<Object[]> data() {
		val resourcesDir = Paths.get("resources", "code-assist", "code-templates").toFile();
		return data(resourcesDir, KZOEref)
	}

	@Test
	def void test_code_template_context() {
		val document = new OpenApi3Document(new OpenApi3Schema())
		val text = specFile.fileContents()
		document.set(text)

		val region = document.getLineInformationOfOffset(offset)
		val line = document.getLineOfOffset(offset)
		val annotationLine = document.get(region.offset, region.getLength())

		val path = document.getModel(offset).getPath(line, document.getColumnOfOffset(line, region))
		val templateContext = OpenApi3ContextType.getContextType(path.toString)
		val matcher = refValuePattern.matcher(annotationLine)
		if (matcher.matches) {
			val String refValue = matcher.group(1);
			assertNotNull("Code-template context is null, but expected: " + refValue, templateContext)
			assertEquals(refValue, templateContext.name);
		} else {
			fail("Invalid test annotation line: " + annotationLine)
		}
	}

}
