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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Point;
import org.junit.Before;
import org.junit.Test;

import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.editor.SwaggerDocument;

public class SwaggerContentAssistProcessorTest {

	private IDocument document;
	private IContentAssistProcessor processor;
	private ITextViewer viewer;
	private ISelectionProvider selectionProvider;
	private ITextSelection selection;
	private ContextTypeRegistry registry;
	private TemplateStore templateStore;

	@Before
	public void setUp() {
		document = new SwaggerDocument();
		registry = mock(ContextTypeRegistry.class);
		templateStore = mock(TemplateStore.class);
		viewer = mock(ITextViewer.class);
		selectionProvider = mock(ISelectionProvider.class);
		selection = mock(ITextSelection.class);
		
		processor = new SwaggerContentAssistProcessor() {
			protected ContextTypeRegistry getContextTypeRegistry() {
				return registry;
			};
			protected TemplateStore geTemplateStore() {
				return templateStore;
			};
		};
	}

	@Test
	public void shouldProvideEndOfWord() {
		String yaml = "swa";
		int offset = 3;

		when(registry.getContextType(com.reprezen.swagedit.templates.SwaggerContextType.RootContextType.CONTEXT_ID)).thenReturn(null);
		when(templateStore.getTemplates()).thenReturn(new Template[0]);
		when(viewer.getDocument()).thenReturn(document);
		when(viewer.getSelectedRange()).thenReturn(new Point(0, 0));
		when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
		when(selectionProvider.getSelection()).thenReturn(selection);
		when(selection.getOffset()).thenReturn(3);
		document.set(yaml);

		ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, offset);

		assertEquals(1, proposals.length);
		
		ICompletionProposal proposal = proposals[0];
		proposal.apply(document);

		assertEquals("swagger:", document.get());
	}

}