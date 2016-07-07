package com.reprezen.swagedit.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.reprezen.swagedit.editor.SwaggerDocument;

public abstract class AbstractProposalProvider {

	protected final ObjectMapper mapper = new ObjectMapper();

	protected final Styler typeStyler = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = new Color(Display.getCurrent(), new RGB(120, 120, 120));
		}
	};

	/**
	 * Returns a list of completion proposals that are created from a single
	 * proposal object.
	 * 
	 * @param path
	 * @param document
	 * @param prefix
	 * @param documentOffset
	 * @return list of completion proposals
	 */
	public Collection<? extends ICompletionProposal> getCompletionProposals(String path, SwaggerDocument document,
			String prefix, int documentOffset, int cycle) {

		final List<ICompletionProposal> result = new ArrayList<>();
		final Iterable<JsonNode> proposals = createProposals(path, document, cycle);

		prefix = Strings.emptyToNull(prefix);

		for (JsonNode proposal : proposals) {
			String value = proposal.get("value").asText();
			String label = proposal.get("label").asText();
			String type = proposal.has("type") ? proposal.get("type").asText() : null;

			StyledString styledString = new StyledString(label);
			if (type != null) {
				styledString.append(": ", typeStyler).append(type, typeStyler);
			}

			if (prefix != null) {
				if (value.startsWith(prefix)) {
					value = value.substring(prefix.length(), value.length());
					result.add(new StyledCompletionProposal(value, styledString, documentOffset, 0, value.length()));
				}
			} else {
				result.add(new StyledCompletionProposal(value, styledString, documentOffset, 0, value.length()));
			}
		}

		return result;
	}

	protected abstract Iterable<JsonNode> createProposals(String path, SwaggerDocument document, int cycle);

}
