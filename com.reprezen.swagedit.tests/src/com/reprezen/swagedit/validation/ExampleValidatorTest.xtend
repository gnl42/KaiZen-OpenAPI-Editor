package com.reprezen.swagedit.validation

import com.reprezen.swagedit.core.validation.ExampleValidator
import com.reprezen.swagedit.editor.SwaggerDocument
import java.net.URI
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.junit.Assert.*

class ExampleValidatorTest {

	@Test
	def void testSingleSchemaValidExample() {
		val text = '''
			swagger: '2.0'
			info:
			  version: '2.0.0'
			  title:  Some API
			paths: 
			  /resource:
			    get:
			      responses:
			        '200':
			          description: OK
			          schema:
			            type: string
			          examples:
			            hello: "world"
		'''

		val document = new SwaggerDocument()
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(0, result.size)
	}

	@Test
	def void testSingleSchema_ExampleInvalid() {
		val text = '''
			swagger: '2.0'
			info:
			  version: '2.0.0'
			  title:  Some API
			paths: 
			  /resource:
			    get:
			      responses:
			        '200':
			          description: OK
			          schema:
			            type: string
			          examples:
			            hello: 1
		'''

		val document = new SwaggerDocument
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(1, result.size)
		
		val error = result.get(0)
		assertEquals(14, error.line)
		assertEquals(IMarker.SEVERITY_ERROR, error.level)
	}
	
	@Test
	def void testSingleSchema_ExampleWithObjectSchema() {
		val text = '''
			swagger: '2.0'
			info:
			  version: '2.0.0'
			  title:  Some API
			paths: 
			  /resource:
			    get:
			      responses:
			        '200':
			          description: OK
			          schema:
			            $ref: "#/definitions/Foo"
			          examples:
			            foo: {
			              "id": 1
			            }
			definitions:
			  Foo:
			    type: object
			    required:
			      - id
			    properties:
			      id:
			        type: integer
		'''

		val document = new SwaggerDocument
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(0, result.size)
	}

	@Test
	def void testSingleSchema_ExampleMissingRequiredKey() {
		val text = '''
			swagger: '2.0'
			info:
			  version: '2.0.0'
			  title:  Some API
			paths: 
			  /resource:
			    get:
			      responses:
			        '200':
			          description: OK
			          schema:
			            $ref: "#/definitions/Foo"
			          examples:
			            foo: {
			              "name": "foo"
			            }
			definitions:
			  Foo:
			    type: object
			    required:
			      - id
			    properties:
			      id:
			        type: integer
		'''

		val document = new SwaggerDocument
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(1, result.size)
		
		val error = result.get(0)
		assertEquals(14, error.line)
		assertEquals(IMarker.SEVERITY_ERROR, error.level)
	}

}
