package com.reprezen.swagedit.openapi3.editor;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;

import com.reprezen.swagedit.common.editor.JsonEditor;
import com.reprezen.swagedit.common.editor.JsonSourceViewerConfiguration;
import com.reprezen.swagedit.openapi3.Activator;

public class OpenApi3Editor extends JsonEditor {

	public static final String ID = "com.reprezen.swagedit.openapi3.editor";

    public OpenApi3Editor() {
        super(new OpenApi3DocumentProvider());
    }
    
    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new JsonSourceViewerConfiguration(Activator.getDefault().getPreferenceStore());
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
    }

}
