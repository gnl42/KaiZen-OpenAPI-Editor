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

	val processor = new OpenApi3ContentAssistProcessor(null) {
		override protected initTextMessages(Model model) { new ArrayList }

		override protected getContextTypeRegistry() { null }

		override protected getTemplateStore() { null }
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
			hasItems("_key_:")
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
			hasItems("_key_:")
		)
	}

}
