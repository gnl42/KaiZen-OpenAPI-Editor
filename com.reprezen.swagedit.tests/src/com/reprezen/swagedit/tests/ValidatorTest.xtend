package com.reprezen.swagedit.tests

import com.reprezen.swagedit.validation.Validator
import java.io.IOException
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.junit.Assert.assertEquals

class ValidatorTest {

	val validator = new Validator

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

		assertEquals(0, validator.validate(content).size())
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

		val errors = validator.validate(content)
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

		val errors = validator.validate(content)
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

		val errors = validator.validate(content)
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

		val errors = validator.validate(content)
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

		val errors = validator.validate(content)
		assertEquals(1, errors.size())

		val error = errors.get(0)
		assertEquals(IMarker.SEVERITY_ERROR, error.getLevel())
		assertEquals(5, error.getLine())
	}

}