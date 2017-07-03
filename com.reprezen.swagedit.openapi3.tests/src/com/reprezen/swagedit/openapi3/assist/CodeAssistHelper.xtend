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
import com.reprezen.swagedit.openapi3.assist.CodeAssistHelper.TestDecriptor
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.List
import java.util.regex.Pattern
import org.eclipse.jface.text.templates.TemplateContextType
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.openapi3.templates.OpenApi3ContextType

class CodeAssistHelper {

	val static testNamePattern = Pattern::compile(".*name=\"([\\w\\s/]+)\".*")
	val static public refValuePattern = Pattern::compile(".*value=\"([\\w\\s./]+)\".*")
	val static public arrayItemMarker = "kzoe-arrayItem"

	def public List<TestDecriptor> extractTests(File resourcesDir, String contextMarker) {
		val specFiles = resourcesDir.listFiles(new FilenameFilter() {

			override accept(File dir, String name) {
				name.endsWith(".yaml")
			}

		})
		val result = new ArrayList<TestDecriptor>()
		for (File specFile : specFiles) {
			val fileContents = fileContents(specFile)
			val indices = getAllIndicesOf(fileContents, contextMarker)
			result.addAll(indices.map[new TestDecriptor(specFile, it, getTestName(fileContents, it))])
		}
		return result
	}

	public static class TestDecriptor {
		val public File file;
		val public int offset;
		val public String name

		new(File file, int offset, String name) {
			this.file = file
			this.offset = offset
			this.name = name
		}

	}

	def private List<Integer> getAllIndicesOf(String str, String substring) {
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

	def public String getTestName(String spec, int offset) {
		val input = spec.substring(offset, spec.indexOf("\n", offset))
		val matcher = testNamePattern.matcher(input)
		if (matcher.matches) {
			return matcher.group(1)
		}
		return "" + offset
	}
	
	def static createOpenApi3Document(File file) {
		val document = new OpenApi3Document(new OpenApi3Schema())
		document.set(file.fileContents())
		return document;
	}

	def static TemplateContextType getCodeTemplateContext(OpenApi3Document document, int offset) {
		val region = document.getLineInformationOfOffset(offset)
		val line = document.getLineOfOffset(offset)
		val annotationLine = document.get(region.offset, region.getLength())

		val path = document.getModel(offset).getPath(line, document.getColumnOfOffset(line, region))

		val isArrayItem = annotationLine.contains(" " + arrayItemMarker)
		val maybeArrayPrefix = if(isArrayItem) "/0" else ""

		val contextType = OpenApi3ContextType::getContextType(document.getModel(), path.toString + maybeArrayPrefix)
		return contextType
	}
}
