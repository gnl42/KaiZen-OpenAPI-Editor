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
package com.reprezen.swagedit.editor;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.junit.Before;
import org.junit.Test;

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
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);
		when(selection.getStartLine()).thenReturn(1);
		when(selection.getOffset()).thenReturn(11);

		String text = 
				"tags:\n" + 
				"  - foo\n" + 
				"definitions:\n" + 
				"  foo:\n" + 
				"    type: object\n";

		document.set(text);
		// region that includes `- foo`
		IRegion region = new Region(document.getLineOffset(1), document.getLineLength(1));
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  - ".length(), 3);
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
		// region that includes `/{id}:`
		IRegion region = new Region(document.getLineOffset(1), document.getLineLength(1));
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  /".length(), "{id}".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 86);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("id", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void testShouldCreateHyperlink_ForJsonReference() throws BadLocationException {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);
		when(selection.getStartLine()).thenReturn(1);
		when(selection.getOffset()).thenReturn(20);

		String text = 
				"schema:\n" +
				"  $ref: '#/definitions/User'\n" +
				"definitions:\n" +
				"  User:\n" +
				"    type: object";

		document.set(text);

		// region that includes `$ref: '#/definitions/User'`
		IRegion region = new Region(document.getLineOffset(1), document.getLineLength(1) - 2);
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		assertNotNull(hyperlinks);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  $ref: ".length(), "'#/definitions/User'".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 0);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("/definitions/User", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void test_match_paramter() {
		String text = "/path/{id}";

		Matcher matcher = SwaggerHyperlinkDetector.PARAMETER_PATTERN.matcher(text);
		Set<String> groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("id"));
	}

	@Test
	public void test_match_second_paramter() {
		String text = "/path/{id}/other/{foo}";

		Matcher matcher = SwaggerHyperlinkDetector.PARAMETER_PATTERN.matcher(text);
		Set<String> groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("id", "foo"));
	}

	@Test
	public void test_match_json_reference() {
		// single quotes
		String text = "'#/definitions/User'";

		Matcher matcher = SwaggerHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		Set<String> groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));

		// double quotes
		text = "\"#/definitions/User\"";

		matcher = SwaggerHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));

		// no quotes
		text = "#/definitions/User";

		matcher = SwaggerHyperlinkDetector.LOCAL_REF_PATTERN.matcher(text);
		groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("/definitions/User"));
	}

}
