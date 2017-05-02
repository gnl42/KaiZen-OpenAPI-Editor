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
package com.reprezen.swagedit.assist;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.assist.ext.MediaTypeContentAssistExt;
import com.reprezen.swagedit.core.assist.JsonContentAssistProcessor;
import com.reprezen.swagedit.core.assist.JsonProposalProvider;
import com.reprezen.swagedit.templates.SwaggerContextType;
import com.reprezen.swagedit.templates.SwaggerTemplateContext;

/**
 * This class provides basic content assist based on keywords used by the
 * swagger schema.
 */
public class SwaggerContentAssistProcessor extends JsonContentAssistProcessor {
	
	public SwaggerContentAssistProcessor(ContentAssistant ca) {
		super(ca, new JsonProposalProvider(new MediaTypeContentAssistExt()), new SwaggerReferenceProposalProvider());
	}

	@Override
	protected TemplateStore getTemplateStore() {
		return Activator.getDefault().getTemplateStore();
	}

	@Override
	protected ContextTypeRegistry getContextTypeRegistry() {
		return Activator.getDefault().getContextTypeRegistry();
	}

	@Override
	protected String getContextTypeId(String path) {
		return SwaggerContextType.getContextType(path);
	}

	@Override
	protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region,
			int relevance) {
		if (context instanceof DocumentTemplateContext) {
			context = new SwaggerTemplateContext((DocumentTemplateContext) context);
		}
		return super.createProposal(template, context, region, relevance);
	}

}