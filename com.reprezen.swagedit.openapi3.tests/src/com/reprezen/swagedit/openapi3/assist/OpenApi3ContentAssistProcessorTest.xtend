package com.reprezen.swagedit.openapi3.assist

import com.reprezen.swagedit.core.assist.StyledCompletionProposal
import com.reprezen.swagedit.openapi3.editor.OpenApi3Document
import java.util.ArrayList
import org.junit.Test

import static com.reprezen.swagedit.openapi3.utils.Cursors.*
import static org.hamcrest.core.IsCollectionContaining.*
import static org.junit.Assert.*
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema
import com.reprezen.swagedit.core.model.Model

class OpenApi3ContentAssistProcessorTest {

	val processor = new OpenApi3ContentAssistProcessor(null, new OpenApi3Schema) {
		override protected initTextMessages(Model model) { new ArrayList }

		override protected getContextTypeRegistry() { null }

		override protected getTemplateStore() { null }

		override protected getContextTypeId(Model model, String path) { null }
	}

	@Test
	def void testCallbacksInOperation_ShouldReturnKey() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      callbacks:
			        <1>
		''', document)

		val proposals = test.apply(processor, "1")		
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(callback name):")
		)
	}

	@Test
	def void testCallbacksInComponents_ShouldReturnKey() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  callbacks:
			    <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], 
			hasItems("(callback name):")
		)
	}

	@Test
	def void testSchemaFormat_ForString() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: string
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("byte", "binary", "date", "date-time", "password", "")
		)
	}

	@Test
	def void testSchemaFormat_ForInteger() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: integer
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("int32", "int64")
		)
	}

	@Test
	def void testSchemaFormat_ForNumber() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: number
			          format: <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("float", "double")
		)
	}

	@Test
	def void testSchemaFormat_ForOthers() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			components:
			  schemas:
			    Pet:
			      properties:
			        name:
			          type: boolean
			          format: <1>
			        name2:
			          type: object
			          format: <2>
			        name2:
			          type: array
			          format: <3>
			        name3:
			          type: "null"
			          format: <4>
			        name4:
			          format: <5>
		''', document)

		var proposals = test.apply(processor, "1")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "2")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "3")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "4")
		assertThat(proposals.map[(it as StyledCompletionProposal).replacementString], hasItems())

		proposals = test.apply(processor, "5")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("int32", "int64", "float", "double", "byte", "binary", "date", "date-time", "password", "")
		)
	}

	@Test
	def void testResponseStatusCode() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      responses:
			        <1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("100:", "200:", "300:", "400:", "500:", "default:", "x-")
		)
	}
	
	@Test
	def void testResponseStatusCodeWithPrefix() {
		val document = new OpenApi3Document(new OpenApi3Schema)
		val test = setUpContentAssistTest('''
			paths:
			  /foo:
			    get:
			      responses:
			         1<1>
		''', document)

		val proposals = test.apply(processor, "1")
		assertThat(
			proposals.map[(it as StyledCompletionProposal).replacementString],
			hasItems("100:", "101:", "102:")
		)
	}

}
