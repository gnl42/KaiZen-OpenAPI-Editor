package com.reprezen.swagedit.assist

import com.reprezen.swagedit.core.assist.JsonExampleProposalProvider
import com.reprezen.swagedit.core.model.Model
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.schema.SwaggerSchema
import com.reprezen.swagedit.tests.utils.PointerHelpers
import org.junit.Before
import org.junit.Test

class SwaggerExampleProposalProviderTest {

	extension PointerHelpers = new PointerHelpers

	val schema = new SwaggerSchema	
	var JsonExampleProposalProvider provider
	var Model model
	
	@Before
	def void setUp() {
		model = Model.empty(schema)
		provider = new SwaggerExampleProposalProvider
	}

	def void testGetExampleProposals_ResponseObject() {
		val text = '''
			paths:
			  /pets/{petId}:
			    get:		
			      responses:
			        '200':
			          schema:
			            $ref: 
			          description: OK
				content:
					application/json: 
						schema:
							$ref: "#/components/schemas/Pet"
						example:
							
			definitions:
			  Valid:
			    type: string
		'''
		val document = new SwaggerDocument
		document.set(text)
		
		val pointer = "/paths/~1pets/get/responses/200/content/application~1json/example"
		val proposals = provider.getProposals(pointer.ptr, document, null)
		
	}

	
}
