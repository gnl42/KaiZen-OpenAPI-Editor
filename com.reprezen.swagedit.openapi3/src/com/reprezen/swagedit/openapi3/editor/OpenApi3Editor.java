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

import java.util.Map;

import org.dadacoalition.yedit.editor.YEditSourceViewerConfiguration;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;

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
import com.reprezen.swagedit.openapi3.validation.OpenApi3Validator;

public class OpenApi3Editor extends JsonEditor {

    public static final String ID = "com.reprezen.swagedit.openapi3.editor";

    public OpenApi3Editor() {
        super(new OpenApi3DocumentProvider(), Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected YEditSourceViewerConfiguration createSourceViewerConfiguration() {
        sourceViewerConfiguration = new OpenApi3SourceViewerConfiguration();
        sourceViewerConfiguration.setEditor(this);
        return sourceViewerConfiguration;
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
        Map<String, JsonNode> preloadedSchemas = Maps.newHashMap();
        JsonNode schema = Activator.getDefault().getSchema().getRootType().asJson();
        preloadedSchemas.put("http://openapis.org/v3/schema.json", schema);

        return new OpenApi3Validator(preloadedSchemas);
    }
}
