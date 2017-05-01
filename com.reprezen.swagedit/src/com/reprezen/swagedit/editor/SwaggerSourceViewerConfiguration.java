package com.reprezen.swagedit.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;

import com.reprezen.swagedit.assist.SwaggerContentAssistProcessor;
import com.reprezen.swagedit.common.editor.JsonSourceViewerConfiguration;
import com.reprezen.swagedit.core.assist.JsonContentAssistProcessor;

public class SwaggerSourceViewerConfiguration extends JsonSourceViewerConfiguration {

	public SwaggerSourceViewerConfiguration(IPreferenceStore store) {
		super(store);
	}

	@Override
	protected JsonContentAssistProcessor createContentAssistProcessor(ContentAssistant ca) {
		return new SwaggerContentAssistProcessor(ca);
	}

}
