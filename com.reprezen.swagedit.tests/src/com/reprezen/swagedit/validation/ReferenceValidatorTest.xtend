package com.reprezen.swagedit.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.swagedit.Messages
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.json.references.JsonReferenceValidator
import com.reprezen.swagedit.mocks.Mocks
import java.net.URI
import java.util.Map
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.junit.Assert.*

class ReferenceValidatorTest {

	val document = new SwaggerDocument

	def validator(Map<URI, JsonNode> entries) {
		new JsonReferenceValidator(Mocks.mockJsonReferenceFactory(entries))
	}

	@Test
	def void shouldValidateReference_To_ValidDefinition() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: '#/definitions/Valid'
			      responses:
			        '200':
			          description: OK
			definitions:
			  Valid:
			    type: string
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/definitions/Valid")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(0, errors.size())
	}

	@Test
	def void shouldValidateReference_To_InvalidDefinition() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: '#/definitions/Invalid'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/definitions/Invalid")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(
			new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
		))
	}

	@Test
	def void shouldValidateReference_To_ValidPath() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: '#/paths/~1foo~1%7Bbar%7D'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/paths/~1foo~1{bar}")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(0, errors.size())
	}

	@Test
	def void shouldWarnOnInvalidCharacters() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: '#/paths/~1foo~1{bar}'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/paths/~1foo~1{bar}")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_invalid_reference)))
	}

	@Test
	def void shouldValidateReference_To_InvalidPath() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: '#/paths/~1foo'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/paths/~1foo")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(
			new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
		))
	}

	@Test
	def void shouldValidateReference_To_ExternalFile() {
		val other = '''
			swagger: '2.0'
		'''

		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: 'other.yaml'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val otherURI = URI.create("other.yaml")
		val errors = validator(#{otherURI -> other.asJson}).validate(baseURI, document.model)

		assertEquals(0, errors.size())
	}

	@Test
	def void should_Not_ValidateReference_To_Invalid_ExternalFile() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: 'other.yaml'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val otherURI = URI.create("other.yaml")
		val errors = validator(#{otherURI -> null}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(
			new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
		))
	}

	@Test
	def void shouldValidateReference_To_ExternalFileFragment() {
		val other = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
		'''

		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: 'other.yaml#/info/version'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val otherURI = URI.create("other.yaml#/info/version")
		val errors = validator(#{otherURI -> other.asJson}).validate(baseURI, document.model)

		assertEquals(0, errors.size())
	}

	@Test
	def void should_Not_ValidateReference_To_ExternalFile_WithInvalidFragment() {
		val other = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
		'''

		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: 'other.yaml#/info/foo'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val otherURI = URI.create("other.yaml#/info/foo")
		val errors = validator(#{otherURI -> other.asJson}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(
			new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
		))
	}

	@Test
	def void should_ProduceError_If_URI_is_Invalid() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: 'contains space ref.yaml#/info/foo'
			      responses:
			        '200':
			          description: OK
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val errors = validator(#{}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertTrue(errors.contains(
			new SwaggerError(9, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
		))
	}

	@Test
	def void should_warn_on_simple_reference() {
		val content = '''
			swagger: '2.0'
			info:
			  version: 0.0.0
			  title: Simple API
			paths:
			  /foo/{bar}:
			    get:
			      parameters:
			        - $ref: Valid
			      responses:
			        '200':
			          description: OK
			definitions:
			  Valid:
			    type: string
		'''

		document.set(content)
		val baseURI = new URI(null, null, null)
		val resolvedURI = new URI(null, null, "/definitions/Valid")
		val errors = validator(#{resolvedURI -> document.asJson}).validate(baseURI, document.model)

		assertEquals(1, errors.size())
		assertEquals(Messages.warning_simple_reference, errors.get(0).message)
	}

	def asJson(String string) {
		new ObjectMapper(new YAMLFactory).readTree(string)
	}

}
