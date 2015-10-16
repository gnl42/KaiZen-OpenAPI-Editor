package com.reprezen.swagedit.tests

import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.validation.Validator
import org.junit.Test

import static org.junit.Assert.assertEquals

class ValidationMessageTest {

	val validator = new Validator
	val document = new SwaggerDocument

	@Test
	def	testMessage_notInEnum() {
		val expected = "instance value (\"foo\") not found in enum (possible values: [\"http\",\"https\",\"ws\",\"wss\"])"
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
		assertEquals(1, errors.size)

		val error = errors.get(0)
		assertEquals(expected, error.message)
	}

}