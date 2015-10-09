package com.reprezen.swagedit.editor;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

public class SwaggerSourceViewerConfiguration extends YEditSourceViewerConfiguration {

	private SwaggerEditor editor;

	public SwaggerSourceViewerConfiguration() {
		super();
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant ca = new ContentAssistant();

//		IContentAssistProcessor cap = new YEditCompletionProcessor() {
//			@Override
//			protected Template[] getTemplates(String contextTypeId) {
//				Template[] templates = super.getTemplates(contextTypeId);
//				System.out.println(contextTypeId + " " + templates.length);
//				for (Template t: templates) {
//					System.out.println(t);
//				}
//				return templates;
//			}
//		};
		
		IContentAssistProcessor cap = new SwaggerCompletionProcessor();
		ca.setContentAssistProcessor(cap, IDocument.DEFAULT_CONTENT_TYPE);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));	    

		ca.enableAutoInsert(true);

		return ca;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		SwaggerReconcilingStrategy strategy = new SwaggerReconcilingStrategy();
		strategy.setEditor(editor);
		MonoReconciler reconciler = new MonoReconciler(strategy,false);
		return reconciler;
	}

	public void setEditor(SwaggerEditor editor) {
		this.editor = editor;
	}

}
