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

import static org.eclipse.ui.IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.Messages;
import com.reprezen.swagedit.assist.JsonReferenceProposalProvider.ContextType;
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
		super(ca, new JsonProposalProvider(new MediaTypeContentAssistExt()));
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

	@Override
	protected String[] initTextMessages() {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		String bindingKey = bindingService.getBestActiveBindingFormattedFor(EDIT_CONTENT_ASSIST);

		ContextType contextType = ContextType.get(getCurrentPath() != null ? getCurrentPath().toString() : "");
		String context = contextType != null ? contextType.label() : "";

		return new String[] { //
				String.format(Messages.content_assist_proposal_project, bindingKey, context),
				String.format(Messages.content_assist_proposal_workspace, bindingKey, context),
				String.format(Messages.content_assist_proposal_local, bindingKey, context) };
	}

}