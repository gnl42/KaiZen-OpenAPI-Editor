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
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class StyledTemplateProposal extends TemplateProposal implements ICompletionProposalExtension6 {

	private StyledString styledString;

	public StyledTemplateProposal(Template template, TemplateContext context, IRegion region, Image image, StyledString styledString, int relevance) {
		super(template, context, region, image, relevance);
		this.styledString = styledString;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return styledString;
	}

}
