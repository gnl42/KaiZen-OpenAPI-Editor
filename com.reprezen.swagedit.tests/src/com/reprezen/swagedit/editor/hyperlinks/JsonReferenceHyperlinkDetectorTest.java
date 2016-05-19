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
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.editor.SwaggerDocument;

public class JsonReferenceHyperlinkDetectorTest {

	private JsonReferenceHyperlinkDetector detector = new JsonReferenceHyperlinkDetector();
	private ITextViewer viewer;

	@Before
	public void setUp() {
		viewer = mock(ITextViewer.class);
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
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		assertNotNull(hyperlinks);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(), "'#/definitions/User'".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 0);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("/definitions/User", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void test_match_json_reference() {
		// single quotes
		String text = "'#/definitions/User'";

		Matcher matcher = JsonReferenceHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		List<String> groups = new ArrayList<>();
		if(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));

		// double quotes
		text = "\"#/definitions/User\"";

		matcher = JsonReferenceHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		groups = new ArrayList<>();
		if(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));

		// no quotes
		text = "#/definitions/User";

		matcher = JsonReferenceHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		groups = new ArrayList<>();
		if(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));
	}

}
