package com.reprezen.swagedit.tests

import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor
import com.reprezen.swagedit.editor.SwaggerDocument
import com.reprezen.swagedit.validation.SwaggerSchema
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.IContentAssistProcessor
import org.eclipse.swt.graphics.Point
import org.junit.Before
import org.junit.Test

import static org.assertj.core.api.Assertions.*
import static org.mockito.Mockito.*

class SwaggerContentAssistProcessorTest {

	val schema = new SwaggerSchema()
	var IDocument document
	var IContentAssistProcessor processor
	var ITextViewer viewer

	@Before
	def void setUp() {
		document = new SwaggerDocument()
		processor = new SwaggerContentAssistProcessor()
		viewer = mock(ITextViewer)
	}

	@Test
	def void shouldProvideAllKeywordsWhenDocIsEmpty() throws BadLocationException {
		val yaml = ''''''
		val offset = 0

		when(viewer.getDocument()).thenReturn(document)
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0))
		document.set(yaml)

		val ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset)

		assertThat(proposals).hasSize(schema.keywords.length)
	}

	@Test
	def void shouldProvideEndOfWord() {
		val yaml = '''swa'''
		val offset = 3
		
		when(viewer.getDocument()).thenReturn(document)
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0))
		document.set(yaml)

		val ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset)

		assertThat(proposals).hasSize(1)
		
		val proposal = proposals.get(0)
		proposal.apply(document)

		assertThat(document.get()).isEqualTo("swagger");
	}
	
}