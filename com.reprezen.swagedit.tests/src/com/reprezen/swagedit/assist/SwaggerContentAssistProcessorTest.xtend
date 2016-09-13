package com.reprezen.swagedit.assist

import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.mocks.Mocks
import java.util.ArrayList
import org.junit.Test

import static com.reprezen.swagedit.tests.utils.Cursors.*
import static org.junit.Assert.*

class SwaggerContentAssistProcessorTest {

	val viewer = Mocks.mockTextViewer
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
			document,
			viewer
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

		assertEquals(expected.size, proposals.length);
		assertTrue(proposals.forall[it|expected.contains((it as StyledCompletionProposal).replacementString)])
	}

	@Test
	def shouldProvideEndOfWord() {
		val document = new SwaggerDocument
		val test = setUpContentAssistTest(
			'''swa<1>''',
			document,
			viewer
		)

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
		''', document, viewer)
		
		val proposals = test.apply(processor, "1")
		println(proposals.map[(it as StyledCompletionProposal).replacementString])
	}
}
