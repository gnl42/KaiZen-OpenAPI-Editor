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

import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.nio.file.Paths
import java.util.Collection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import static org.junit.Assert.*
import com.fasterxml.jackson.core.JsonPointer
import com.reprezen.swagedit.openapi3.assist.OpenApi3ReferenceProposalProvider.OpenApi3ContextTypeCollection
import org.junit.runners.Parameterized.Parameter
import java.io.File

import static com.reprezen.swagedit.openapi3.assist.CodeAssistHelper.*

@RunWith(typeof(Parameterized))
class ReferenceContextTest {

	val static KZOEref = "#KZOE-ref"
	
	@Parameter
	var public File specFile

	@Parameter(1)
	var public String fileName // for test name only
	
	@Parameter(2)
	var public int offset // for test name only
	
	@Parameter(3)
	var public String testName // for test name only
	

	val allContextTypes = new OpenApi3ContextTypeCollection(new OpenApi3Schema)

	@Parameters(name="{index}: {1} - {3}")
	def static Collection<Object[]> data() {
		val resourcesDir = Paths.get("resources", "code-assist", "references").toFile();
		return new CodeAssistHelper().extractTests(resourcesDir, KZOEref).map[#[it.file, it.file.name, it.offset, it.name] as Object[]]
	}

	@Test
	def void test_reference_context() {
		val document = createOpenApi3Document(specFile) 

		val region = document.getLineInformationOfOffset(offset)
		val line = document.getLineOfOffset(offset)
		val annotationLine = document.get(region.offset, region.getLength())

		val path = document.getPath(line, document.getColumnOfOffset(line, region))
		val isArrayItem = annotationLine.contains(" " + arrayItemMarker)
		val maybeArrayPrefix = if (isArrayItem) "/0" else ""
//		val contextType = allContextTypes.get(document.content, path.append(JsonPointer.compile(maybeArrayPrefix + "/$ref")))
//
//
//		val matcher = refValuePattern.matcher(annotationLine)
//		if (matcher.matches) {
//			val String refValue = matcher.group(1);
//			assertEquals(refValue, contextType.value);
//		} else {
//			fail("Invalid test annotation line: " + annotationLine)
//		}
	}

}
