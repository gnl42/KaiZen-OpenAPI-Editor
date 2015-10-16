package com.reprezen.swagedit.tests

import com.reprezen.swagedit.completions.SwaggerCompletionProcessor
import com.reprezen.swagedit.completions.SwaggerProposal
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.validation.Schema
import java.util.Arrays
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CompletionProposalTest {

	val schema = new Schema()
	var IDocument document
	var IContentAssistProcessor processor
	var ITextViewer viewer

	@Before
	def void setUp() {
		document = new SwaggerDocument()
		processor = new SwaggerCompletionProcessor()
		viewer = mock(ITextViewer)
	}

	@Test
	def void test() {
		val proposal = new SwaggerProposal.Builder(schema.getTree()).build()
		System.out.println(proposal);		
	}

	@Test
	def void shouldProvideEnumCompletion() throws BadLocationException {
		val currentText = "swagger: "
		val offset = 2

		when(viewer.getDocument()).thenReturn(document)
		document.set(currentText)

		val ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset)
		System.out.println(Arrays.asList(proposals))
	}

}