package com.reprezen.swagedit.openapi3.validation

import com.reprezen.swagedit.core.validation.Messages
import com.reprezen.swagedit.core.validation.SwaggerError
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import java.net.URI
import org.eclipse.core.resources.IMarker
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

class ValidatorTest {

	val validator = ValidationHelper.validator()
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

	@Test
	def void testValidationShouldFail_ForInvalidPointers() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			  
			paths: {}
			components: 
			  schemas:
			    MyType1:
			      type: object
			      properties:
			        property:
			          $ref: "INVALID"
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)

		assertEquals(1, errors.size())
		assertThat(
			errors,
			hasItems(
				new SwaggerError(13, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
			)
		)
	}

	@Test
	def void testValidationShouldFail_refNotJson() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			  
			paths: {}
			components: 
			  schemas:
			    MyType1:
			      type: object
			      properties:
			        property:
			          $ref: "https://www.reprezen.com/#"
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		// Update with #353 Validation of external $ref property values should show error on unexpected object type"
		assertEquals(1, errors.size())
		assertThat(
			errors,
			hasItems(				
				new SwaggerError(13, IMarker.SEVERITY_WARNING, Messages.error_missing_reference)
			)
		)
	}

//	@Test
	def void testValidationShouldFail_pathInNotJson() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			  
			paths: {}
			components: 
			  schemas:
			  
			    MyType1:
			      properties:
			        property:
			          $ref: "https://www.reprezen.com/#components/schemas/Pet"
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		// Update with #353 Validation of external $ref property values should show error on unexpected object type"
		assertEquals(2, errors.size())
		assertThat(
			errors,
			hasItems(
				new SwaggerError(13, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference_type),
				new SwaggerError(13, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference)
			)
		)
	}

	@Test
	def void testValidationShouldPass_LinksOperationIdIsOperation() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      responses:
			        200:
			          description: Ok
			components: 
			  links:
			    myLink:
			      operationId: opId
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(0, errors.size())
	}

	@Test
	def void testValidationShouldFail_LinksOperationIdNotOperation() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      responses:
			        200:
			          description: Ok
			components: 
			  links:
			    myLink:
			      operationId: fail
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(1, errors.size())
	}

	@Test
	def void testValidationShouldPass_LinksOperationRefIsOperation() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      responses:
			        200:
			          description: Ok
			components: 
			  links:
			    myLink:
			      operationRef: "#/paths/~1/get/"
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(0, errors.size())
	}

	@Test
	def void testValidationShouldFail_LinksOperationRefNotOperation() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      responses:
			        200:
			          description: Ok
			components: 
			  links:
			    myLink:
			      operationRef: "#/paths/~1"
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(1, errors.size())
		assertThat(
			errors,
			hasItems(
				new SwaggerError(15, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference_type)
			)
		)
	}

	@Test
	def void testValidationShouldPass_SecuritySchemes() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      security:
			        - open: []
			      responses:
			        200:
			          description: Ok
			components: 
			  securitySchemes:
			    open:
			      type: basic
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(0, errors.size())
	}

	@Test
	def void testValidationShouldFail_SecuritySchemes() {
		val content = '''
			openapi: "3.0.0"
			info:
			  title: Broken links Object
			  version: "1.0.0"
			paths:
			  /:
			    get:
			      operationId: opId
			      security:
			        - foo: []
			      responses:
			        200:
			          description: Ok
			components: 
			  securitySchemes:
			    open:
			      type: basic
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)
		assertEquals(1, errors.size())
		assertThat(
			errors,
			hasItems(
				new SwaggerError(10, IMarker.SEVERITY_ERROR, Messages.error_invalid_reference_type)
			)
		)
	}

	@Test
	def void testValidationParameterIn_ShouldFail() {
		val content = '''
			openapi: 3.0.0
			info: 
			  title: Example
			  version: 1.0.0
			paths:
			  /:
			    post:
			      parameters:
			        - name: foo
			          in: headerzzz
			          required: true
			          schema:
			            type: string
			      responses:
			        '200':
			          description: Ok
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)

		assertEquals(1, errors.size())
		assertThat(
			errors,
			hasItems(
				new SwaggerError(10, IMarker.SEVERITY_ERROR, Messages.error_invalid_parameter_location)
			)
		)
	}

	@Test
	def void testValidationParameterIn() {
		val content = '''
			openapi: 3.0.0
			info: 
			  title: Example
			  version: 1.0.0
			paths:
			  /:
			    post:
			      parameters:
			        - name: foo
			          in: query
			          required: true
			          schema:
			            type: string
			      responses:
			        '200':
			          description: Ok
		'''

		document.set(content)
		val errors = validator.validate(document, null as URI)

		assertEquals(0, errors.size())
	}

	@Test
	def void testValidateMissingRequiredProperties() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths: {}
		components:
		  schemas:
		    Foo:
		      type: object
		      properties:
		        bar:
		          type: string
		      required:
		        - baz
		'''

		document.set(content)
		document.onChange()

		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())
		assertEquals(String.format(Messages.error_required_properties, "baz"), errors.get(0).message)
	}
	
	@Test
	def void testValidateInlineSchemas() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo:
		    get:
		      description: ok
		      responses:
		        '200':
		          description: OK
		          content:
		            application/json:
		              schema:
		                properties:
		                  bar:
		                    type: string
		'''

		document.set(content)
		document.onChange()

		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())
		assertEquals(Messages.error_object_type_missing, errors.get(0).message)
	}

	@Test
	def void testArrayWithItemsIsInvalid() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      responses:
		        '200':
		          description: OK
		components:
		  schemas:
		    Pets:
		      type: array
		      items:
		        - type: string		 
		'''

		document.set(content)
		document.onChange()

		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())
		assertTrue(errors.map[message].forall[it.equals(Messages.error_array_items_should_be_object)])
		assertThat(errors.map[line], hasItems(15))
	}

	@Test
	def void testObjectWithPropertyNamedProperties_ShouldBeValid() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      responses:
		        '200':
		          description: OK
		components:
		  schemas:
		    Pets:
		      type: object
		      properties:
		        properties:
		          type: object
		          properties:
		            name: 
		              type: string
		'''

		document.set(content)
		document.onChange()

		val errors = validator.validate(document, null as URI)		
		assertEquals(0, errors.size())
	}
	
	@Test
	def void validateOAuthSecuritySchemes() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      security:
		        - oauth:
		            - write:pets
		            - read:pets
		      responses:
		        '200':
		          description: OK
		components:
		  securitySchemes:
		    oauth:
		      type: oauth2
		      flows: 
		        implicit:
		          authorizationUrl: https://example.com/api/oauth/dialog
		          scopes:
		            write:pets: modify pets in your account
		            read:pets: read your pets
		'''

		document.set(content)

		val errors = validator.validate(document, null as URI)		
		assertEquals(0, errors.size())
	}
	
	@Test
	def void validateOAuthSecuritySchemes_WithWrongScope() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      security:
		        - oauth:
		            - write:pets
		            - foo
		      responses:
		        '200':
		          description: OK
		components:
		  securitySchemes:
		    oauth:
		      type: oauth2
		      flows: 
		        implicit:
		          authorizationUrl: https://example.com/api/oauth/dialog
		          scopes:
		            write:pets: modify pets in your account
		            read:pets: read your pets
		'''

		document.set(content)

		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())
		assertTrue(errors.map[message].forall[it.equals(Messages.error_invalid_scope_reference)])
		assertThat(errors.map[line], hasItems(11))
	}

	@Test
	def void validateNonOAuthSecuritySchemes() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      security:
		        - oauth: []
		      responses:
		        '200':
		          description: OK
		components:
		  securitySchemes:
		    oauth:
		      type: http
		'''

		document.set(content)

		val errors = validator.validate(document, null as URI)		
		assertEquals(0, errors.size())
	}

	@Test
	def void validateNonOAuthSecuritySchemes_WithWrongScope() {
		val content = '''
		openapi: '3.0.0'
		info:
		  version: 0.0.0
		  title: Simple API
		paths:
		  /foo/{bar}:
		    get:
		      security:
		        - basic:
		          - foo
		      responses:
		        '200':
		          description: OK
		components:
		  securitySchemes:
		    basic:
		      type: http
		'''

		document.set(content)

		val errors = validator.validate(document, null as URI)		
		assertEquals(1, errors.size())		
		assertTrue(errors.map[message].forall[it.equals(Messages.error_scope_should_be_empty)])
		assertThat(errors.map[line], hasItems(9))
	}
}
