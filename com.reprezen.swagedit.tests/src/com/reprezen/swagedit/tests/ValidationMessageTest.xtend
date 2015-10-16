package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.Validator
import org.junit.Test

import static org.junit.Assert.assertEquals

class ValidationMessageTest {

	val validator = new Validator

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

		val errors = validator.validate(content)		
		assertEquals(1, errors.size)

		val error = errors.get(0)
		assertEquals(expected, error.message)
	}

}