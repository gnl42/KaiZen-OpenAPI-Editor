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

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.dadacoalition.yedit.editor.scanner.YAMLScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.editor.hyperlinks.DefinitionHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.JsonReferenceHyperlinkDetector;
import com.reprezen.swagedit.editor.hyperlinks.PathParamHyperlinkDetector;

public class SwaggerSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	private SwaggerEditor editor;
	private YAMLScanner scanner;

	public SwaggerSourceViewerConfiguration() {
		super();
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant ca = new ContentAssistant();
		SwaggerContentAssistProcessor processor = new SwaggerContentAssistProcessor(ca);

		ca.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		ca.enableAutoInsert(false);
		ca.enableAutoActivation(true);
		ca.setAutoActivationDelay(100);
		ca.enableColoredLabels(true);
		ca.setShowEmptyList(true);
		ca.setRepeatedInvocationMode(true);
		ca.addCompletionListener(processor);
		ca.setStatusLineVisible(true);

		return ca;
	}

	@Override
	protected YAMLScanner getScanner() {
		if (scanner == null) {
			scanner = new SwaggerScanner(colorManager, Activator.getDefault().getPreferenceStore());
		}
		return scanner;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		SwaggerReconcilingStrategy strategy = new SwaggerReconcilingStrategy();
		strategy.setEditor(editor);
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		return reconciler;
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return new IHyperlinkDetector[] { 
			new URLHyperlinkDetector(), 
			new JsonReferenceHyperlinkDetector(),
			new PathParamHyperlinkDetector(),
			new DefinitionHyperlinkDetector()
		};
	}

	public void setEditor(SwaggerEditor editor) {
		this.editor = editor;
	}

}
