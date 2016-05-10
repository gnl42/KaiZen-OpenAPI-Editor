/*******************************************************************************
 * Copyright (c) 2016 ModelSolv, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.tests;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.editor.SwaggerHyperlink;
import com.reprezen.swagedit.editor.SwaggerHyperlinkDetector;

public class SwaggerHyperlinkDetectorTest {

	private SwaggerHyperlinkDetector detector = new SwaggerHyperlinkDetector();
	private ITextViewer viewer;
	private ITextSelection selection;

	@Before
	public void setUp() {
		viewer = mock(ITextViewer.class);
		ISelectionProvider provider = mock(ISelectionProvider.class);
		selection = mock(ITextSelection.class);

		when(viewer.getSelectionProvider()).thenReturn(provider);
		when(provider.getSelection()).thenReturn(selection);
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
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  name: ".length(), 3);
		IRegion targetRegion = new Region(document.getLineOffset(4), 17);

		assertThat(Arrays.asList(hyperlinks), hasItem(
				new SwaggerHyperlink("foo", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void testShouldCreateHyperLink_FromPathParameter_ToParameterDefinition() throws Exception {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);
		when(selection.getStartLine()).thenReturn(1);
		when(selection.getOffset()).thenReturn(11);

		String text = 
				"paths:\n" +
				"  /{id}:\n" +
				"    get:\n" +
				"      parameters:\n" +
				"        - name: id\n" +
				"          type: number\n"+
				"          required: true\n" +
				"          in: path\n";

		document.set(text);
		// region that includes `name: foo`
		IRegion region = new Region(document.getLineOffset(1), document.getLineLength(1));
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1), "  /{id}:".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 86);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("  /{id}:", viewer, linkRegion, targetRegion)));
	}

}
