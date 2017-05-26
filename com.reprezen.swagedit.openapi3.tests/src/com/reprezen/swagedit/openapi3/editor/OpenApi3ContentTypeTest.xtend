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
package com.reprezen.swagedit.openapi3.editor

import static org.junit.Assert.*
import org.junit.Test

class OpenApi3ContentTypeTest {

	@Test
	def void double_quotes() {
		val String content = '''
		openapi: "3.0.0"
		info:''';
		assertTrue(new OpenApi3ContentDescriber().isSupported(content));
	}

	@Test
	def void single_quotes() {
		val String content = '''
		openapi: '3.0.0'
		info:''';
		assertTrue(new OpenApi3ContentDescriber().isSupported(content));
	}

	@Test
	def void no_quotes() {
		val String content = '''
		openapi: 3.0.0
		info:''';
		assertTrue(new OpenApi3ContentDescriber().isSupported(content));
	}
		@Test
	def void spec_with_comment() {
		val String content = '''
		#a comment
		openapi: "3.0.0"
		info:''';
		assertTrue(new OpenApi3ContentDescriber().isSupported(content));
	}

	@Test
	def void version_with_release_candidate() {
		val String content = '''
		openapi: '3.0.0-RC0'
		info:''';
		assertTrue(new OpenApi3ContentDescriber().isSupported(content));
	}

	@Test
	def void no_closing_quotes_not_supported() {
		val String content = '''
		openapi: '3.0.0
		info:''';
		assertFalse(new OpenApi3ContentDescriber().isSupported(content));
	}
}
