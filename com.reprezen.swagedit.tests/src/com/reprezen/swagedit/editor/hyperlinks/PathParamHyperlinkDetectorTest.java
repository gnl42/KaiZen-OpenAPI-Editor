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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.editor.hyperlinks.PathParamHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.SwaggerHyperlink;

public class PathParamHyperlinkDetectorTest {

	private PathParamHyperlinkDetector detector = new PathParamHyperlinkDetector();
	private ITextViewer viewer;

	@Before
	public void setUp() {
		viewer = mock(ITextViewer.class);
	}

	@Test
	public void testShouldCreateHyperLink_FromPathParameter_ToParameterDefinition() throws Exception {
		SwaggerDocument document = new SwaggerDocument();
		when(viewer.getDocument()).thenReturn(document);

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
		IRegion region = new Region("paths:\n  /{i".length(), 1);
		IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

		// expected region
		IRegion linkRegion = new Region(document.getLineOffset(1) + "  /".length(), "{id}".length());
		IRegion targetRegion = new Region(document.getLineOffset(4), 86);

		assertThat(Arrays.asList(hyperlinks), 
				hasItem(new SwaggerHyperlink("id", viewer, linkRegion, targetRegion)));
	}

	@Test
	public void test_match_paramter() {
		String text = "/path/{id}";

		Matcher matcher = PathParamHyperlinkDetector.PARAMETER_PATTERN.matcher(text);
		Set<String> groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("id"));
	}

	@Test
	public void test_match_second_paramter() {
		String text = "/path/{id}/other/{foo}";

		Matcher matcher = PathParamHyperlinkDetector.PARAMETER_PATTERN.matcher(text);
		Set<String> groups = new HashSet<>();
		while(matcher.find()) {
			groups.add(matcher.group(1));
		}

		assertThat(groups, hasItems("id", "foo"));
	}

}
