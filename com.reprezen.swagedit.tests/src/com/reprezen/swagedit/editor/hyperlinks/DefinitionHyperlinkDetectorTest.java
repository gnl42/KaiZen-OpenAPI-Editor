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
import com.reprezen.swagedit.editor.hyperlinks.DefinitionHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.SwaggerHyperlink;

public class DefinitionHyperlinkDetectorTest {

    private DefinitionHyperlinkDetector detector = new DefinitionHyperlinkDetector();
    private ITextViewer viewer;

    @Before
    public void setUp() {
        viewer = mock(ITextViewer.class);
    }

    @Test
    public void testShouldCreateHyperLink_ToDefinition() throws BadLocationException {
        SwaggerDocument document = new SwaggerDocument();
        when(viewer.getDocument()).thenReturn(document);

        String text = "tags:\n" + "  - foo\n" + "definitions:\n" + "  foo:\n" + "    type: object\n";

        document.set(text);
        // region that includes `- foo`
        IRegion region = new Region("tags:\n  - fo".length(), 1);
        IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

        // expected region
        IRegion linkRegion = new Region(document.getLineOffset(1) + "  - ".length(), 3);
        IRegion targetRegion = new Region(document.getLineOffset(4), 17);

        assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("foo", viewer, linkRegion, targetRegion)));
    }

    @Test
    public void testShouldCreateHyperLink_FromRequired_ToProperty() throws BadLocationException {
        SwaggerDocument document = new SwaggerDocument();
        when(viewer.getDocument()).thenReturn(document);

        String text = "NewPet:\n" + "  required:\n" + "    - name\n" + "  properties:\n" + "    name:\n"
                + "      type: string\n";

        document.set(text);
        // region that includes `- name`
        IRegion region = new Region("NewPet:\nrequired:\n    - nam".length(), 1);
        IHyperlink[] hyperlinks = detector.detectHyperlinks(viewer, region, false);

        // expected region
        IRegion linkRegion = new Region(document.getLineOffset(2) + "    - ".length(), "name".length());
        IRegion targetRegion = new Region(document.getLineOffset(5), 19);

        assertThat(Arrays.asList(hyperlinks), hasItem(new SwaggerHyperlink("name", viewer, linkRegion, targetRegion)));
    }
}
