package com.reprezen.swagedit.openapi3.validation

import com.reprezen.swagedit.core.validation.Messages
import com.reprezen.swagedit.core.validation.Validator
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.net.URI
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.swagedit.core.json.references.JsonReferenceValidator
import com.google.common.collect.ImmutableMap
import com.reprezen.swagedit.core.json.references.JsonReferenceFactory

class ValidatorTest {

	val validator = new Validator(new JsonReferenceValidator(new JsonReferenceFactory), 
		ImmutableMap.of("http://openapis.org/v3/schema.json", new OpenApi3Schema().asJson)
	)
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
		val errors = validator.validate(document, null as URI)
		println(errors.map[message])
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
		val errors = validator.validate(document, null as URI)
		assertEquals(1, errors.size())
		assertTrue(errors.map[message].forall[it.equals(Messages.error_invalid_reference_type)])
	}

	@Test
	def void testValidationShouldPass_ForArrays() {
		val content = '''
			openapi: '3.0.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  {}
			components:
			  schemas:
			    Foo:
			      type: array
			      items:
			        $ref: "#/components/schemas/Bar"
			    Bar:
			      type: object
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)		
		assertEquals(0, errors.size())
	}

	@Test
	def void testValidationShouldFail_ForArraysInWrongItemType() {
		val content = '''
			openapi: '3.0.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  {}
			components:
			  responses:
			    ok:
			      description: Ok
			  schemas:
			    Foo:
			      type: array
			      items:
			        $ref: "#/components/responses/ok"
			    Bar:
			      type: object
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())
		assertTrue(errors.map[message].forall[it.equals(Messages.error_invalid_reference_type)])
	}

}
