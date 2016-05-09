package com.reprezen.swagedit.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.databind.JsonNode;

public class SwaggerHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer viewer, IRegion region, boolean canShowMultipleHyperlinks) {
		final SwaggerDocument document = (SwaggerDocument) viewer.getDocument();

		String candidate;
		IRegion lineInfo;
		try {
			lineInfo = document.getLineInformationOfOffset(region.getOffset());
			candidate = document.get(lineInfo.getOffset(), lineInfo.getLength());

			int lastSpace = candidate.lastIndexOf(" ");
			if (lastSpace > -1) {
				candidate = candidate.substring(lastSpace + 1, candidate.length());
			}
		} catch (BadLocationException e) {
			return null;
		}

		final String linkPath = findDefinition(document.asJson(), candidate);
		final IRegion linkRegion = document.getRegion(linkPath);

		if (linkRegion != null) {
			return new IHyperlink[] { new SwaggerHyperlink(candidate, viewer, linkRegion) };
		}

		return null;
	}

	private String findDefinition(JsonNode asJson, String candidate) {
		JsonNode definitions = asJson.get("definitions");
		if (definitions != null && definitions.isObject()) {
			if (definitions.get(candidate) != null) {
				return ":definitions:" + candidate;
			}
		}

		return null;
	}

}
