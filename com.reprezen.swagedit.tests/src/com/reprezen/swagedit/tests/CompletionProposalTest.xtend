package com.reprezen.swagedit.tests

import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import java.util.Arrays
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.jface.text.IDocument
import com.reprezen.swagedit.completions.SwaggerProposal
import com.reprezen.swagedit.editor.SwaggerDocument
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.contentassist.ICompletionProposal
import com.reprezen.swagedit.validation.Schema
import org.eclipse.jface.text.ITextViewer
import com.reprezen.swagedit.completions.SwaggerCompletionProcessor
import org.junit.Before

class CompletionProposalTest {

	val schema = new Schema()
	var IDocument document
	var IContentAssistProcessor processor
	var ITextViewer viewer

	@Before
	def setUp() {
		document = new SwaggerDocument()
		processor = new SwaggerCompletionProcessor()
		viewer = mock(ITextViewer)
	}

	@Test
	def test() {
		val proposal = new SwaggerProposal.Builder(schema.getTree()).build()
		System.out.println(proposal);		
	}

	@Test
	def shouldProvideEnumCompletion() throws BadLocationException {
		val currentText = "swagger: "
		val offset = 2

		when(viewer.getDocument()).thenReturn(document)
		document.set(currentText)

		val ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset)
		System.out.println(Arrays.asList(proposals))
	}

}