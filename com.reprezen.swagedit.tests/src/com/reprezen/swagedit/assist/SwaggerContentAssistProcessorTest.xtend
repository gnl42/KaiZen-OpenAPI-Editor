package com.reprezen.swagedit.assist

import com.reprezen.swagedit.editor.SwaggerDocument
import java.util.ArrayList
import org.junit.Test

import static com.reprezen.swagedit.tests.utils.Cursors.*
import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*

class SwaggerContentAssistProcessorTest {

	val processor = new SwaggerContentAssistProcessor() {
		override protected initTextMessages() {
			new ArrayList
		}

		override protected getContextTypeRegistry() {
			null
		}

		override protected geTemplateStore() {
			null
		}
	}

	@Test
	def shouldProvideAllRoot_OnEmptyDocument() {
		val document = new SwaggerDocument
		val test = setUpContentAssistTest(
			'''<1>''',
			document
		)

		val proposals = test.apply(processor, "1")
		val expected = #[
			"swagger:",
			"info:",
			"host:",
			"basePath:",
			"schemes:",
			"consumes:",
			"produces:",
			"paths:",
			"definitions:",
			"parameters:",
			"responses:",
			"security:",
			"securityDefinitions:",
			"tags:",
			"externalDocs:",
			"x-:"
		]

		assertEquals(expected.size, proposals.length)
		assertTrue(proposals.forall[it|expected.contains((it as StyledCompletionProposal).replacementString)])
	}

	@Test
	def shouldProvideEndOfWord() {
		val document = new SwaggerDocument
		val test = setUpContentAssistTest('''swa<1>''', document)

		val proposals = test.apply(processor, "1")
		assertEquals(1, proposals.length);

		val proposal = proposals.get(0)
		proposal.apply(document)
		assertEquals("swagger:", document.get())
	}

	@Test
	def void test() {
		val document = new SwaggerDocument
		val test = setUpContentAssistTest('''
			swagger: "2.0"
			info:
			  version: 1.0.0
			  title: Swagger Petstore
			  license:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")		
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				"name:",
				"url:",
				"x-:"
			))
	}

	@Test
	def void test2() {
		val document = new SwaggerDocument
		val test = setUpContentAssistTest('''
			paths:
			  /pets:    
			    get:
			      summary: List all pets
			      operationId: listPets
			      tags:
			        - pets
			        - ds
			      parameters:
			        <1>
			        - name: <2>
			          <3>
			        - name: limit
			          in: <4>
			          <5>
		''', document)

		var proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				"-"
			))

		proposals = test.apply(processor, "2")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				""
			))

		proposals = test.apply(processor, "3")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				"uniqueItems:",
				"format:",
				"maxItems:",
				"$ref:",
				"schema:",
				"maximum:",
				"required:",
				"collectionFormat:",
				"allowEmptyValue:",
				"minLength:",
				"maxLength:"			
			))
		
		proposals = test.apply(processor, "4")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				"body", 
				"query", 
				"path", 
				"header", 
				"formData"
			))

		proposals = test.apply(processor, "5")	
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems(
				"uniqueItems:",
				"format:",
				"maxItems:",
				"$ref:",
				"schema:",
				"maximum:",
				"required:",
				"collectionFormat:",
				"allowEmptyValue:",
				"minLength:",
				"maxLength:"			
			))
	}

	@Test
	def void test3() {
		val test = setUpContentAssistTest('''
		 definitions:
		  Foo:
		    <1>
		  Product:
		    <2>required:
		      - name  
		    properties:
		      name:
		        type: string
		      description:
		        type: <3>
		''', new SwaggerDocument)

		var proposals = test.apply(processor, "1")
		var values = proposals.map[(it as StyledCompletionProposal).replacementString]
		assertEquals(30, values.size)
		assertThat(values, hasItems(
			"$ref:", 
			"format:", 
			"title:", 
			"description:", 
			"multipleOf:", 
			"maximum:", 
			"exclusiveMaximum:", 
			"minimum:", 
			"exclusiveMinimum:", 
			"maxLength:", 
			"minLength:", 
			"pattern:", 
			"maxItems:", 
			"minItems:", 
			"uniqueItems:", 
			"maxProperties:",
			"minProperties:", 
			"required:", 
			"enum:", 
			"additionalProperties:", 
			"type:", 
			"items:", 
			"allOf:", 
			"properties:", 
			"discriminator:", 
			"readOnly:", 
			"xml:", 
			"externalDocs:", 
			"example:", 
			"x-:"))

		proposals = test.apply(processor, "2")
		values = proposals.map[(it as StyledCompletionProposal).replacementString]
		// same without required and properties
		assertEquals(28, values.size)
		assertThat(values, hasItems(
			"$ref:", 
			"format:", 
			"title:", 
			"description:", 
			"multipleOf:", 
			"maximum:", 
			"exclusiveMaximum:", 
			"minimum:", 
			"exclusiveMinimum:", 
			"maxLength:", 
			"minLength:", 
			"pattern:", 
			"maxItems:", 
			"minItems:", 
			"uniqueItems:", 
			"maxProperties:",
			"minProperties:",  
			"enum:", 
			"additionalProperties:", 
			"type:", 
			"items:", 
			"allOf:",  
			"discriminator:", 
			"readOnly:", 
			"xml:", 
			"externalDocs:", 
			"example:", 
			"x-:"))
		
		proposals = test.apply(processor, "3")
		values = proposals.map[(it as StyledCompletionProposal).replacementString]
		assertEquals(7, values.size)
		assertThat(values, hasItems(
			"array",
			"boolean",
			"integer",
			"\"null\"",
			"number",
			"object",
			"string"))
	}
}
