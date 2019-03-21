package com.reprezen.swagedit.openapi3.validation

import com.reprezen.swagedit.core.validation.ExampleValidator
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.net.URI
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.junit.Assert.*

class ExampleValidatorTest {

	@Test
	def void testSingleSchemaValidExample() {
		val text = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: Test
			paths:
			  /pets:
			    get:       
			      responses:   
			        200:
			          description: Ok
			          content:
			            application/json:    
			              schema:
			                $ref: "#/components/schemas/Pet"
			              example: {
			                "id": 1,
			                "name": Foo
			              }
			components:
			  schemas: 
			    Pet:
			      type: object
			      required:
			        - id
			      properties:
			        id:
			          type: integer
			        name:
			          type: string
		'''

		val document = new OpenApi3Document(new OpenApi3Schema)
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(0, result.size)
	}
	
	@Test
	def void testSingleSchema_ExampleWithMissingRequired() {
		val text = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: Test
			paths:
			  /pets:
			    get:
			      responses:
			        200:
			          description: Ok
			          content:
			            application/json:    
			              schema:
			                $ref: "#/components/schemas/Pet"
			              examples:
			                json:
			                  value:
			                    {
			                      "name": Foo
			                    }
			components:
			  schemas: 
			    Pet:
			      type: object
			      required:
			        - id
			      properties:
			        id:
			          type: integer
			        name:
			          type: string
		'''

		val document = new OpenApi3Document(new OpenApi3Schema)
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(1, result.size)
		
		val error = result.get(0)
		assertEquals(16, error.line)
		assertEquals(IMarker.SEVERITY_ERROR, error.level)
	}
	
	@Test
	def void testSingleSchema_ExampleInParameters() {
		val text = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: Test
			paths:
			  /pets:
			    get:
			      parameters:
			        - name: limit
			          in: query
			          schema:
			            $ref: "#/components/schemas/Pet"
			          example: {
			            "id": 1
			          }
			      responses:
			        200:
			          description: Ok
			components:
			  schemas: 
			    Pet:
			      type: object
			      required:
			        - id
			      properties:
			        id:
			          type: integer
			        name:
			          type: string
		'''

		val document = new OpenApi3Document(new OpenApi3Schema)
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(0, result.size)
	}

	@Test
	def void testSingleSchema_ExampleInParametersMissingRequiredKey() {
		val text = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: Test
			paths:
			  /pets:
			    get:
			      parameters:
			        - name: limit
			          in: query
			          schema:
			            $ref: "#/components/schemas/Pet"
			          example: {
			            "name": hello
			          }
			      responses:
			        200:
			          description: Ok
			components:
			  schemas: 
			    Pet:
			      type: object
			      required:
			        - id
			      properties:
			        id:
			          type: integer
			        name:
			          type: string
		'''

		val document = new OpenApi3Document(new OpenApi3Schema)
		document.set(text)

		val validator = new ExampleValidator(new URI("file://test.yaml"), document)

		val result = document.model.allNodes.map [
			validator.validate(it)
		].flatten.toSet

		assertEquals(1, result.size)
		
		val error = result.get(0)
		assertEquals(13, error.line)
		assertEquals(IMarker.SEVERITY_ERROR, error.level)
	}

}
