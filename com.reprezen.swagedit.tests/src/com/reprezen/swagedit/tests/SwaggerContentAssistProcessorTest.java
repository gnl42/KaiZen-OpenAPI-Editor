package com.reprezen.swagedit.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Point;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.assist.SwaggerProposal;
import com.reprezen.swagedit.assist.SwaggerProposal.ObjectProposal;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.SwaggerSchema;

public class SwaggerContentAssistProcessorTest {

	private SwaggerSchema schema = new SwaggerSchema();
	private IDocument document;
	private IContentAssistProcessor processor;
	private ITextViewer viewer;

	@Before
	public void setUp() {
		document = new SwaggerDocument();
		processor = new SwaggerContentAssistProcessor();
		viewer = mock(ITextViewer.class);
	}

	@Test
	public void shouldProvideAllKeywordsWhenDocIsEmpty() throws BadLocationException {
		String yaml = "";
		int offset = 0;

		when(viewer.getDocument()).thenReturn(document);
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0));
		document.set(yaml);

		ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset);

		assertEquals(schema.getKeywords(true).size(), proposals.length);
	}

	@Test
	public void shouldProvideEndOfWord() {
		String yaml = "swa";
		int offset = 3;
		
		when(viewer.getDocument()).thenReturn(document);
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0));
		document.set(yaml);

		ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset);

		assertEquals(1, proposals.length);
		
		ICompletionProposal proposal = proposals[0];
		proposal.apply(document);

		assertEquals("swagger", document.get());
	}

	@Test
	public void test() {		
		ObjectProposal proposal = new SwaggerSchema().get();
		SwaggerProposal value = proposal.getProperties().get("swagger");
		
		System.out.println(value);
		assertNotNull(value);
	}

}