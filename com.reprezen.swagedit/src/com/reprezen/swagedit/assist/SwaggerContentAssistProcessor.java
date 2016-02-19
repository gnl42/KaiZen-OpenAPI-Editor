package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.reprezen.swagedit.Activator;
import com.reprezen.swagedit.Activator.Icons;
import com.reprezen.swagedit.editor.SwaggerDocument;
import com.reprezen.swagedit.templates.SwaggerContextType;
import com.reprezen.swagedit.templates.SwaggerTemplateContext;

/**
 * This class provides basic content assist based on keywords used by the
 * swagger schema.
 */
public class SwaggerContentAssistProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

	private final SwaggerProposalProvider proposalProvider = new SwaggerProposalProvider();
	private String currentPath = null;

	public SwaggerContentAssistProcessor() {}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		if (!(viewer.getDocument() instanceof SwaggerDocument)) {
			return super.computeCompletionProposals(viewer, documentOffset);
		}

		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();
		final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
		int line = 0, lineOffset = 0, column = 0;
		try {
			line = document.getLineOfOffset(documentOffset);
			lineOffset = document.getLineOffset(line);
			column = selection.getOffset() - lineOffset;
		} catch (BadLocationException e) {}

		final String prefix = extractPrefix(viewer, documentOffset);
		// we have to remove the length of
		// the prefix to obtain the correct
		// column to resolve the path
		if (!prefix.isEmpty()) {
			column -= prefix.length();
		}

		currentPath = document.getPath(line, column);

		// compute template proposals
		final ICompletionProposal[] templateProposals = super.computeCompletionProposals(viewer, documentOffset);
		final List<ICompletionProposal> proposals = new ArrayList<>();

		proposals.addAll(proposalProvider.getCompletionProposals(currentPath, document, prefix, documentOffset));

		if (templateProposals != null && templateProposals.length > 0) {
			proposals.addAll(Lists.newArrayList(templateProposals));
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	@Override
	protected String extractPrefix(ITextViewer viewer, int offset) {
		int i= offset;
		IDocument document= viewer.getDocument();
		if (i > document.getLength())
			return ""; //$NON-NLS-1$

		try {
			while (i > 0) {
				char ch= document.getChar(i - 1);
				if (Character.isWhitespace(ch))
					break;
				i--;
			}

			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {};
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	@Override
	protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region,
			int relevance) {
		if (context instanceof DocumentTemplateContext) {
			context = new SwaggerTemplateContext((DocumentTemplateContext) context);
		}
		return new StyledTemplateProposal(template, context, region, getImage(template), getTemplateLabel(template),
				relevance);
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return geTemplateStore().getTemplates();
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		String contextType = SwaggerContextType.getContentType(currentPath);
		System.out.println("contextType " + contextType);
		return getContextTypeRegistry().getContextType(contextType);
	}

	@Override
	protected Image getImage(Template template) {
		return Activator.getDefault().getImage(Icons.template_item);
	}

	protected TemplateStore geTemplateStore() {
		return Activator.getDefault().getTemplateStore();
	}

	protected ContextTypeRegistry getContextTypeRegistry() {
		return Activator.getDefault().getContextTypeRegistry();
	}

	protected StyledString getTemplateLabel(Template template) {
		Styler nameStyle = new StyledString.Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = new Color(Display.getCurrent(), new RGB(80, 80, 255));
			}
		};
		Styler descriptionStyle = new StyledString.Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
			}
		};

		return new StyledString(template.getName(), nameStyle)
				.append(": ", descriptionStyle)
				.append(template.getDescription(), descriptionStyle);
	}

}