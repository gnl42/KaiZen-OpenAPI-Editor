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
package com.reprezen.swagedit.editor.hyperlinks;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.mocks.Mocks;

public class JsonReferenceHyperlinkDetectorTest {

	private ITextViewer viewer;
	private URI uri;

	protected JsonReferenceHyperlinkDetector detector(JsonNode document) {
		return Mocks.mockHyperlinkDetector(uri, document);
	}

	@Before
	public void setUp() throws URISyntaxException {
		viewer = mock(ITextViewer.class);
		uri = new URI(null, null, null);
	}

	@Test
	public void testShouldCreateHyperlink_ForJsonReference() throws BadLocationException {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);

		String text = 
				"schema:\n" +
				"  $ref: '#/definitions/User'\n" +
				"definitions:\n" +
				"  User:\n" +
				"    type: object";

		document.set(text);

		// region that includes `$ref: '#/definitions/User'`
		IRegion region = new Region("schema:\n  $ref: '#/definitions".length(), 1);
		IHyperlink[] hyperlinks = detector(document.asJson())
				.detectHyperlinks(viewer, region, false);

		assertNotNull(hyperlinks);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(), "'#/definitions/User'".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 0);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("/definitions/User", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void testShould_Not_CreateHyperlink_For_Invalid_JsonReference() throws BadLocationException {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);

		String text = 
				"schema:\n" +
				"  $ref: '#/definitions/Invalid'\n" +
				"definitions:\n" +
				"  User:\n" +
				"    type: object";

		document.set(text);

		// region that includes `$ref: '#/definitions/User'`
		IRegion region = new Region("schema:\n  $ref: '#/definitions".length(), 1);
		IHyperlink[] hyperlinks = detector(document.asJson())
				.detectHyperlinks(viewer, region, false);

		assertNull(hyperlinks);
	}

	@Test
	public void testShouldCreateHyperlink_ForPathReference() throws BadLocationException {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);

		String text = 
				"schema:\n" +
				"  $ref: '#/paths/~1foo~1{bar}'\n" +
				"paths:\n" +
				"  /foo/{bar}:\n" +
				"    get: ";

		document.set(text);

		// region that includes `$ref: '#/paths/~1foo~1{bar}'`
		IRegion region = new Region("schema:\n  $ref: '#/paths/~1foo".length(), 1);
		IHyperlink[] hyperlinks = detector(document.asJson())
				.detectHyperlinks(viewer, region, false);

		assertNotNull(hyperlinks);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(), "'#/paths/~1foo~1{bar}'".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 0);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("/paths/~1foo~1{bar}", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void testShould_Not_CreateHyperlink_For_Invalid_PathReference() throws BadLocationException {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);

		String text = 
				"schema:\n" +
				"  $ref: '#/paths/~1foo'\n" +
				"paths:\n" +
				"  /foo/{bar}:\n" +
				"    get: ";

		document.set(text);

		// region that includes `$ref: '#/paths/~1foo~1{bar}'`
		IRegion region = new Region("schema:\n  $ref: '#/paths/~1foo".length(), 1);
		IHyperlink[] hyperlinks = detector(document.asJson())
				.detectHyperlinks(viewer, region, false);

		assertNull(hyperlinks);
	}

}
