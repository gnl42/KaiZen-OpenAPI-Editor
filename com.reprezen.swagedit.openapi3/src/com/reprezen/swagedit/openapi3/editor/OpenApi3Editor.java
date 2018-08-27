/*******************************************************************************
 *  Copyright (c) 2016 ModelSolv, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *     ModelSolv, Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package com.reprezen.swagedit.openapi3.editor;

import static com.reprezen.swagedit.openapi3.preferences.OpenApi3PreferenceConstants.ADVANCED_VALIDATION;

import java.util.Map;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.reprezen.swagedit.core.assist.JsonContentAssistProcessor;
import com.reprezen.swagedit.core.editor.JsonEditor;
import com.reprezen.swagedit.core.editor.JsonSourceViewerConfiguration;
import com.reprezen.swagedit.core.hyperlinks.DefinitionHyperlinkDetector;
import com.reprezen.swagedit.core.hyperlinks.PathParamHyperlinkDetector;
import com.reprezen.swagedit.core.schema.CompositeSchema;
import com.reprezen.swagedit.core.validation.Validator;
import com.reprezen.swagedit.openapi3.Activator;
import com.reprezen.swagedit.openapi3.assist.OpenApi3ContentAssistProcessor;
import com.reprezen.swagedit.openapi3.hyperlinks.LinkOperationHyperlinkDetector;
import com.reprezen.swagedit.openapi3.hyperlinks.LinkOperationRefHyperlinkDetector;
import com.reprezen.swagedit.openapi3.hyperlinks.OpenApi3ReferenceHyperlinkDetector;
import com.reprezen.swagedit.openapi3.hyperlinks.SecuritySchemeHyperlinkDetector;
import com.reprezen.swagedit.openapi3.schema.OpenApi3Schema;
import com.reprezen.swagedit.openapi3.validation.OpenApi3Validator;

public class OpenApi3Editor extends JsonEditor {

    public static final String ID = "com.reprezen.swagedit.openapi3.editor";

    private OpenApi3Validator validator;

    private final IPropertyChangeListener advancedValidationListener = event -> {
        if (validator != null) {
            if (ADVANCED_VALIDATION.equals(event.getProperty())) {
                validator.setAdvancedValidation(getPreferenceStore().getBoolean(ADVANCED_VALIDATION));
            }
        }
    };

    public OpenApi3Editor() {
        super(new OpenApi3DocumentProvider(), //
                // ZEN-4361 Missing marker location indicators (Overview Ruler) next to editor scrollbar in KZOE
                new ChainedPreferenceStore(new IPreferenceStore[] { //
                        Activator.getDefault().getPreferenceStore(), //
                        // Preferences store for EditorsPlugin has settings to show/hide the rules and markers
                        EditorsPlugin.getDefault().getPreferenceStore() }));
    }

    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new OpenApi3SourceViewerConfiguration();
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
    }

    @Override
    public void dispose() {
        // preference store is removed in AbstractTextEditor.dispose()
        getPreferenceStore().removePropertyChangeListener(advancedValidationListener);
        super.dispose();
    }

    public static class OpenApi3SourceViewerConfiguration extends JsonSourceViewerConfiguration {

        public OpenApi3SourceViewerConfiguration() {
            super(Activator.getDefault().getPreferenceStore());
        }

        @Override
        protected JsonContentAssistProcessor createContentAssistProcessor(ContentAssistant ca) {
            return new OpenApi3ContentAssistProcessor(ca);
        }

        @Override
        public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
            return new IHyperlinkDetector[] { new URLHyperlinkDetector(), //
                    new PathParamHyperlinkDetector(), //
                    new DefinitionHyperlinkDetector(), //
                    new OpenApi3ReferenceHyperlinkDetector(), //
                    new SecuritySchemeHyperlinkDetector(), //
                    new LinkOperationHyperlinkDetector(), //
                    new LinkOperationRefHyperlinkDetector() };
        }

        @Override
        protected CompositeSchema getSchema() {
            return Activator.getDefault().getSchema();
        }

        @Override
        protected IInformationControlCreator getOutlineInformationControlCreator() {
            return getOutlineInformationControlCreator(OpenApi3ContentDescriber.CONTENT_TYPE_ID);
        }

    }

    @Override
    protected Validator createValidator() {
        if (validator == null) {
            Map<String, JsonNode> preloadedSchemas = Maps.newHashMap();
            JsonNode schema = Activator.getDefault().getSchema().getRootType().asJson();
            preloadedSchemas.put(OpenApi3Schema.URL, schema);

            validator = new OpenApi3Validator(preloadedSchemas);
            validator.setAdvancedValidation(getPreferenceStore().getBoolean(ADVANCED_VALIDATION));

            getPreferenceStore().addPropertyChangeListener(advancedValidationListener);
        }

        return validator;
    }
}
