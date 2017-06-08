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
import java.io.File
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.regex.Pattern
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter

@RunWith(typeof(Parameterized))
abstract class CodeAssistContextTest {
	
	val static testNamePattern = Pattern::compile(".*name=\"([\\w\\s/]+)\".*")
	val protected static refValuePattern = Pattern::compile(".*value=\"([\\w\\s/]+)\".*")

	def static protected Collection<Object[]> data(File resourcesDir, String contextMarker) {
		val specFiles = resourcesDir.listFiles(new FilenameFilter() {

			override accept(File dir, String name) {
				name.endsWith(".yaml")
			}

		})
		val Collection<Object[]> result = new ArrayList<Object[]>()
		for (File specFile : specFiles) {
			val fileContents = fileContents(specFile)
			val indices = getAllIndicesOf(fileContents, contextMarker)
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
