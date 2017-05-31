package com.reprezen.swagedit.openapi3.validation

import org.junit.Test
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.core.validation.Validator

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.core.validation.Messages

class ValidatorTest {

	val validator = new Validator
	val document = new OpenApi3Document(new OpenApi3Schema)

    @Test
	def void testValidationShouldPass_IfRefIsCorrectType() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /:
		    get:
		      responses:
		        '200':
		          $ref: "#/components/responses/ok"
		components:
		  responses:
		    ok:
		      description: Ok
		  schemas:
		    Foo:
		      type: object
		'''
		
		document.set(content)
		val errors = validator.validate(document, null)
		assertEquals(0, errors.size())
	}

	@Test
	def void testValidationShouldFail_IfRefIsNotCorrectType() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /:
		    get:
		      responses:
		        '200':
		          $ref: "#/components/schemas/Foo"
		components:
		  responses:
		    ok:
		      description: Ok
		  schemas:
		    Foo:
		      type: object
		'''
		
		document.set(content)
		val errors = validator.validate(document, null)
		assertEquals(1, errors.size())
		assertTrue(errors.map[message].forall[it.equals(Messages.error_invalid_reference_type)])
	}

}