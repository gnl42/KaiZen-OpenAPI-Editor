package com.reprezen.swagedit.tests;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.completions.SwaggerCompletionProcessor;
import com.reprezen.swagedit.completions.SwaggerProposal;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.validation.Schema;

public class SwaggerCompletionProposalTest {

	private final Schema schema = new Schema();
	private IDocument document;
	private IContentAssistProcessor processor;
	private ITextViewer viewer;

	@Before
	public void setUp() {
		document = new SwaggerDocument();
		processor = new SwaggerCompletionProcessor();
		viewer = mock(ITextViewer.class);
	}

	@Test
	public void test() {
		SwaggerProposal proposal = new SwaggerProposal.Builder(schema.getTree()).build();
		System.out.println(proposal);		
	}

	@Test
	public void shouldProvideEnumCompletion() throws BadLocationException {
		final String currentText = "swagger: ";
		final int offset = 2;

		when(viewer.getDocument()).thenReturn(document);
		document.set(currentText);

		ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset);
		System.out.println(Arrays.asList(proposals));
	}

}
