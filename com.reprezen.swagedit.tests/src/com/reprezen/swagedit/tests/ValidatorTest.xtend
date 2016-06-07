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
package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.validation.Validator
import java.io.IOException
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.validation.SwaggerError
import com.reprezen.swagedit.Messages

class ValidatorTest {

	val validator = new Validator
	val document = new SwaggerDocument

	@Test
	def shouldNotReturnErrorsIfDocumentIsValid() throws IOException {
		// valid document
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /:
			    get:
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(0, errors.size())
	}

	@Test
	def shouldReturnSingleErrorIfMissingRootProperty() throws IOException {
		// missing property paths
		val content = '''
		  swagger: '2.0'
		  info:
		    version: 0.0.0
		    title: Simple API
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(1, error.getLine())
	}

	@Test
	def shouldReturnSingleErrorIfTypeOfPropertyIsIncorrect() throws IOException {
		// incorrect value type for property paths
		val content = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths: 'Hello'
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(5, error.getLine())
	}

	@Test
	def shouldReturnSingleErrorIfTypeOfDeepPropertyIsIncorrect() throws IOException {
		// invalid responses type
		val content = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /:
		    get:
		      responses: 'Hello'
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(8, error.getLine())
	}

	@Test
	def shouldReturnSingleErrorIfInvalidResponseCode() throws IOException {
		// invalid response code
		val content = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /:
		    get:
		      responses:
		        '0':
		          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(9, error.getLine())
	}

	@Test
	def shouldReturnErrorForInvalidScheme() throws IOException {
		// invalid scheme foo
		val content = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		schemes:
		  - http
		  - foo
		paths:
		  /:
		    get:
		      responses:
		        '200':
		          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(5, error.getLine())
	}

	@Test
	def void shouldReturnCorrectErrorPositionOnPathWithHierarchy() throws IOException {
		// invalid property schema
		val content = '''
		swagger: '2.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      responses:
		        '200':
		          description: OK
		          schema:
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(1, errors.size())

		errors.forEach[
			assertTrue(it.line == 10 || it.line == 11)
			assertEquals(IMarker.SEVERITY_ERROR, it.level)
		]
	}

	@Test
	def void shouldWarnOnDuplicateKeys() {
		val content = '''
			swagger: '2.0'
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(2, errors.size())
		assertThat(errors, hasItems(
			new SwaggerError(1, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "swagger")),
			new SwaggerError(2, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "swagger"))
		))
	}

	@Test
	def void shouldNotWarnOnDuplicateKeys_InDifferentObjects() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      responses:
			        '200':
			          description: OK
			    put:
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(0, errors.size())
	}

	@Test
	def void shouldWarnOnDuplicateKeys_InsideObjects() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  version: 1.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(2, errors.size())
		assertThat(errors, hasItems(
			new SwaggerError(3, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "version")),
			new SwaggerError(4, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "version"))
		))
	}

	@Test
	def void shouldWarnOnDuplicateKeys_InsideItems() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			         - name: bar
			           in: path
			           in: path
			           type: string
			           required: true
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(2, errors.size())
		assertThat(errors, hasItems(
			new SwaggerError(10, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "in")),
			new SwaggerError(11, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "in"))
		))
	}

	@Test
	def void shouldWarnOnDuplicateKeys_InsidePaths() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      responses:
			        '200':
			          description: OK
			      responses:
			        '201':
			          description: OK
		'''

		document.set(content)
		val errors = validator.validate(document)

		assertEquals(2, errors.size())
		assertThat(errors, hasItems(
			new SwaggerError(8, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "responses")),
			new SwaggerError(11, IMarker.SEVERITY_WARNING, String.format(Messages.error_duplicate_keys, "responses"))
		))
	}
}

