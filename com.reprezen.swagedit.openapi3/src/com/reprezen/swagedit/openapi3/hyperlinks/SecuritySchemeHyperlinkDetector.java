package com.reprezen.swagedit.openapi3.hyperlinks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.fasterxml.jackson.core.JsonPointer;
import com.reprezen.swagedit.core.editor.JsonDocument;
import com.reprezen.swagedit.core.hyperlinks.AbstractJsonHyperlinkDetector;
import com.reprezen.swagedit.core.hyperlinks.SwaggerHyperlink;
import com.reprezen.swagedit.core.model.AbstractNode;
import com.reprezen.swagedit.core.model.Model;

public class SecuritySchemeHyperlinkDetector extends AbstractJsonHyperlinkDetector {

    protected static final String REGEX = ".*/security/\\d+/(\\w+)";
    protected static final Pattern PATTERN = Pattern.compile(REGEX);

    @Override
    protected boolean canDetect(JsonPointer pointer) {
        return pointer != null && pointer.toString().matches(REGEX);
    }

    @Override
    protected IHyperlink[] doDetect(JsonDocument doc, ITextViewer viewer, HyperlinkInfo info, JsonPointer pointer) {
        Matcher matcher = PATTERN.matcher(pointer.toString());
        String link = matcher.find() ? matcher.group(1) : null;

        if (link != null) {
            Model model = doc.getModel();
            AbstractNode securityScheme = model.find("/components/securitySchemes/" + link);

            if (securityScheme != null) {
                IRegion target = doc.getRegion(securityScheme.getPointer());
                if (target != null) {
                    return new IHyperlink[] { new SwaggerHyperlink(info.text, viewer, info.region, target) };
                }
            }
        }
        return null;
    }

}
