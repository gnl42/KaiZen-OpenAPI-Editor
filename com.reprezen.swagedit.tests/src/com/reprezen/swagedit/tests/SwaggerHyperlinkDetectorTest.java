package com.reprezen.swagedit.tests;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.editor.SwaggerHyperlink;
import com.reprezen.swagedit.editor.SwaggerHyperlinkDetector;

public class SwaggerHyperlinkDetectorTest {

	private SwaggerHyperlinkDetector detector = new SwaggerHyperlinkDetector();
	private ITextViewer viewer;

	@Before
	public void setUp() {
		viewer = mock(ITextViewer.class);
	}

	@Test
	public void testShouldCreateHyperLink_ToDefinition() throws BadLocationException {
		String text = 
				"tags:\n" + 
				"  name: foo\n" + 
				"definitions:\n" + 
				"  foo:\n" + 
				"    type: object\n";

		SwaggerDocument document = new SwaggerDocument();
		document.set(text);
		// region that includes `name: foo`
		IRegion region = new Region(document.getLineOffset(1), document.getLineLength(1));

		when(viewer.getDocument()).thenReturn(document);
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(3), document.getLineLength(3));

		assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("foo", viewer, linkRegion)));
	}
}
