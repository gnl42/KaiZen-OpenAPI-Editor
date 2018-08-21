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

import com.reprezen.swagedit.core.templates.SwaggerTemplateContext
import com.reprezen.swagedit.openapi3.Activator
import com.reprezen.swagedit.openapi3.validation.ValidationHelper
import java.io.File
import java.nio.file.Paths
import java.util.Collection
import org.eclipse.jface.text.templates.DocumentTemplateContext
import org.eclipse.jface.text.templates.Template
import org.eclipse.jface.text.templates.TemplateContextType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static com.reprezen.swagedit.openapi3.assist.CodeAssistHelper.*
import static org.junit.Assert.*

@RunWith(typeof(Parameterized))
class CodeTemplateTextTest {

	extension ValidationHelper validationHelper = new ValidationHelper

	val static KZOEref = "#KZOE-template"

	@Parameter
	var public File specFile

	@Parameter(1)
	var public String fileName // for test name only
	@Parameter(2)
	var public int offset

	@Parameter(3)
	var public String testName // for test name only
	@Parameter(4)
	var public Template template

	@Parameter(5)
	var public String templateName // for test name only
	@Parameter(6)
	var public TemplateContextType contextType // for test name only

	@Parameters(name="{index} CONTEXT: {3}; TEMPLATE: {5} ({1})")
	def static Collection<Object[]> data() {
		val result = newArrayList()
		val resourcesDir = Paths.get("resources", "code-assist", "code-templates").toFile();
		val testCases = new CodeAssistHelper().extractTests(resourcesDir, KZOEref)
		testCases.forEach [
			val contextType = getCodeTemplateContext(createOpenApi3Document(it.file), it.offset)
			val activator = new Activator() {

				override protected getTabWidth() {
					// YEdit preferences defaults are not iniatialized properly why running the test from Maven
					// Therefore, tabWidth is 0 which creates false test failures 
					2
				}

			}
			val templates = activator.templateStore.getTemplates(contextType?.id);
			result.addAll(templates.map [ t |
				#[it.file, it.file.name, it.offset, it.name, t, t.name, contextType] as Object[]
			])
		]
		return result
	}

	@Test
	def void test_code_template_text() {
		val document = createOpenApi3Document(specFile)
	
		val region = document.getLineInformationOfOffset(offset)
		val lineOfOffset = document.getLineOfOffset(offset)
		val annotationLine = document.get(region.offset, region.getLength())

		val isArrayItem = annotationLine.contains(" " + arrayItemMarker)

		// remove the line with comment
		document.replace(offset, region.offset + region.length - offset, "")
		if (isArrayItem) {
			document.replace(offset, 0, "- ")
			offset = offset + "- ".length
		} else {
			// remove the next line
			val lineInformation = document.getLineInformation(lineOfOffset+1)
			document.replace(lineInformation.offset, lineInformation.length, "")
		}
		
		val swaggerContext = new SwaggerTemplateContext(
			new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength()))
		val templateString = swaggerContext.evaluate(template).getString();
		document.replace(offset, 0, templateString + "\n");
		
		if (document.asJson === null) {
			fail("Null document for " + templateString)
		}
		val errors = validate(document)
		if (!errors.empty) {
			fail('JSON Schema validation error')
		}
	}
	
}
